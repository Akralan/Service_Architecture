package fr.insa.mas.DemandMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;

@SpringBootApplication
@RestController
@RequestMapping("/demands")
public class DemandMsApplication {

	private static final String DB_URL = "jdbc:mysql://srv-bdens.insa-toulouse.fr:3306/projet_gei_039";
    private static final String DB_USER = "projet_gei_039";
    private static final String DB_PASSWORD = "auteD5ro";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(DemandMsApplication.class, args);
    }

    @PostMapping("/add")
    public String addDemand(@RequestBody DemandRequest request) {

        System.out.println("Tentative d'ajout d'une demande pour l'utilisateur ID : " + request.getUserId());

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connexion à la base de données réussie.");

            // Vérification du type d'utilisateur
            String checkUserTypeSql = "SELECT Type FROM Users WHERE ID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkUserTypeSql)) {
                checkStatement.setInt(1, request.getUserId());
                System.out.println("Requête de vérification du type d'utilisateur préparée.");

                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (rs.next()) {
                        String userType = rs.getString("Type");
                        System.out.println("Type d'utilisateur trouvé : " + userType);

                        if (!"Client".equalsIgnoreCase(userType)) {
                            System.out.println("Échec : seul un utilisateur de type 'Client' peut créer des demandes.");
                            return "Only clients can create demands.";
                        }
                    } else {
                        System.out.println("Utilisateur introuvable pour l'ID : " + request.getUserId());
                        return "User not found.";
                    }
                }
            }

            // Insertion de la demande
            String sql = "INSERT INTO Demands (User_ID, Name, Description, Priority) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, request.getUserId());
                statement.setString(2, request.getName());
                statement.setString(3, request.getDescription());
                statement.setString(4, request.getPriority());
                System.out.println("Requête d'insertion préparée : " + sql);

                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("Demande ajoutée avec succès pour l'utilisateur ID : " + request.getUserId());
                    return "Demand added successfully.";
                } else {
                    System.out.println("Échec de l'ajout de la demande.");
                    return "Failed to add demand.";
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur de base de données lors de l'ajout de la demande. Message : " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }

    @DeleteMapping("/delete/{demandId}")
    public String deleteDemand(@PathVariable int demandId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM Demands WHERE ID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, demandId);
                int rowsDeleted = statement.executeUpdate();
                return rowsDeleted > 0 ? "Demand deleted successfully." : "Failed to delete demand.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    @PutMapping("/update")
    public String updateDemand(@RequestBody DemandUpdateRequest request) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE Demands SET Name = ?, Description = ?, Priority = ?, State = 'Waiting' WHERE ID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, request.getName());
                statement.setString(2, request.getDescription());
                statement.setString(3, request.getPriority());
                statement.setInt(4, request.getDemandId());
                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0 ? "Demand updated successfully." : "Failed to update demand.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    @PutMapping("/validate/{demandId}")
    public String validateDemand(@PathVariable int demandId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE Demands SET State = 'Validated' WHERE ID = ? AND State = 'Waiting'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, demandId);
                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0 ? "Demand validated successfully." : "Demand cannot be validated as it is not in 'Waiting' state.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }
    
    @PutMapping("/choose")
    public String chooseDemand(@RequestBody Map<String, Integer> request) {

        int demandId = request.get("Demand_ID");
        int helperId = request.get("Helper_ID");

        System.out.println("Tentative de choix de la demande ID : " + demandId + " par le Helper ID : " + helperId);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connexion à la base de données réussie.");

            String sql = "UPDATE Demands SET State = 'Ongoing', Helper_ID = ? WHERE ID = ? AND State = 'Validated'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, helperId);
                statement.setInt(2, demandId);
                System.out.println("Requête SQL préparée : " + sql);

                int rowsUpdated = statement.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Demande choisie avec succès pour l'ID : " + demandId + " par le Helper ID : " + helperId);
                    return "Demand chosen successfully.";
                } else {
                    System.out.println("Échec du choix de la demande ID : " + demandId + ". Etat incorrect.");
                    return "Demand cannot be chosen as it is not in 'Validated' state.";
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur de base de données lors du choix de la demande ID : " + demandId + ". Message : " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }

    @PutMapping("/finish/{demandId}")
    public String finishDemand(@PathVariable int demandId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE Demands SET State = 'Done' WHERE ID = ? AND State = 'Ongoing'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, demandId);
                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0 ? "Demand finished successfully." : "Demand cannot be finished as it is not in 'Ongoing' state.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }
    
    @GetMapping("/get/{userId}")
    public List<Demand> getDemandsByUser(@PathVariable int userId) {

        System.out.println("Tentative de récupération des demandes pour l'utilisateur ID : " + userId);

        List<Demand> demands = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connexion à la base de données réussie.");

            String sql = "SELECT ID, User_ID, Creation_date, Name, Description, State, Priority FROM Demands WHERE User_ID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                System.out.println("Requête SQL préparée : " + sql);

                try (ResultSet rs = statement.executeQuery()) {
                    boolean hasResults = false;

                    while (rs.next()) {
                        hasResults = true;
                        Demand demand = new Demand();
                        demand.setId(rs.getInt("ID"));
                        demand.setUserId(rs.getInt("User_ID"));
                        // demand.setCreationDate(rs.getDate("Creation_date")); // Uncomment if needed
                        demand.setName(rs.getString("Name"));
                        demand.setDescription(rs.getString("Description"));
                        demand.setState(rs.getString("State"));
                        demand.setPriority(rs.getString("Priority"));
                        
                        demands.add(demand);
                        System.out.println("Demande récupérée : ID=" + demand.getId() + ", Nom=" + demand.getName() 
                            + ", Etat=" + demand.getState() + ", Priorité=" + demand.getPriority());
                    }

                    if (!hasResults) {
                        System.out.println("Aucune demande trouvée pour l'utilisateur ID : " + userId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur de base de données lors de la récupération des demandes pour l'utilisateur ID : " 
                    + userId + ". Message : " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }

        System.out.println("Nombre total de demandes récupérées pour l'utilisateur ID " + userId + " : " + demands.size());
        return demands;
    }
    
    @GetMapping("/get-all")
    public List<Demand> getDemandsByUser() {
        List<Demand> demands = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT ID, User_ID, Creation_date, Name, Description, State, Priority FROM Demands WHERE State = 'Validated'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Demand demand = new Demand();
                        demand.setId(rs.getInt("ID"));
                        demand.setUserId(rs.getInt("User_ID"));
                        //demand.setCreationDate(rs.getDate("Creation_date"));
                        demand.setName(rs.getString("Name"));
                        demand.setDescription(rs.getString("Description"));
                        demand.setState(rs.getString("State"));
                        demand.setPriority(rs.getString("Priority"));
                        demands.add(demand);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return demands;
    }
}

class DemandRequest {
    private int userId;
    private String name;
    private String description;
    private String priority;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}

class DemandUpdateRequest {
    private int demandId;
    private String name;
    private String description;
    private String priority;

    public int getDemandId() { return demandId; }
    public void setDemandId(int demandId) { this.demandId = demandId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}

class Demand {
    private int id;
    private int userId;
    private Date creationDate;
    private String name;
    private String description;
    private String state;
    private String priority;

    // Getters and Setters
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getUserId() {return userId;}
    public void setUserId(int userId) {this.userId = userId;}

    public Date getCreationDate() {return creationDate;}
    public void setCreationDate(Date creationDate) {this.creationDate = creationDate;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public String getState() {return state;}
    public void setState(String state) {this.state = state;}

    public String getPriority() {return priority;}
    public void setPriority(String priority) {this.priority = priority;}
}

