package fr.insa.mas.DemandMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String checkUserTypeSql = "SELECT Type FROM Users WHERE ID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkUserTypeSql)) {
                checkStatement.setInt(1, request.getUserId());
                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (rs.next()) {
                        String userType = rs.getString("Type");
                        if (!"Client".equalsIgnoreCase(userType)) {
                            return "Only clients can create demands.";
                        }
                    } else {
                        return "User not found.";
                    }
                }
            }

            String sql = "INSERT INTO Demands (User_ID, Name, Description, Priority) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, request.getUserId());
                statement.setString(2, request.getName());
                statement.setString(3, request.getDescription());
                statement.setString(4, request.getPriority());
                int rowsInserted = statement.executeUpdate();
                return rowsInserted > 0 ? "Demand added successfully." : "Failed to add demand.";
            }
        } catch (SQLException e) {
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

    @PutMapping("/choose/{demandId}")
    public String chooseDemand(@PathVariable int demandId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE Demands SET State = 'Ongoing' WHERE ID = ? AND State = 'Validated'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, demandId);
                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0 ? "Demand chosen successfully." : "Demand cannot be chosen as it is not in 'Validated' state.";
            }
        } catch (SQLException e) {
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
        List<Demand> demands = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT ID, User_ID, Creation_date, Name, Description, State, Priority FROM Demands WHERE User_ID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
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
    
    @GetMapping("/get-all/")
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

