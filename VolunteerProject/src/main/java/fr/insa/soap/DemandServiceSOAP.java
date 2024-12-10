package fr.insa.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.xml.ws.Endpoint;
import java.sql.*;

@WebService
public class DemandServiceSOAP {

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

    @WebMethod
    public String addDemand(int userId, String name, String description, String priority) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO Demands (User_ID, Name, Description, Priority) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, description);
                preparedStatement.setString(4, priority);
                int rowsInserted = preparedStatement.executeUpdate();
                return rowsInserted > 0 ? "Demand added successfully." : "Failed to add demand.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    @WebMethod
    public String deleteDemand(int demandId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM Demands WHERE ID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, demandId);
                int rowsDeleted = preparedStatement.executeUpdate();
                return rowsDeleted > 0 ? "Demand deleted successfully." : "Failed to delete demand.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    @WebMethod
    public String updateDemand(int demandId, String name, String description, String priority) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE Demands SET Name = ?, Description = ?, Priority = ?, State = 'Waiting' WHERE ID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, description);
                preparedStatement.setString(3, priority);
                preparedStatement.setInt(4, demandId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Demand updated successfully." : "Failed to update demand.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    @WebMethod
    public String validateDemand(int demandId, boolean isValid, String reason) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            if (isValid) {
                String sql = "UPDATE Demands SET State = 'Validated' WHERE ID = ? AND State = 'Waiting'";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, demandId);
                    int rowsUpdated = preparedStatement.executeUpdate();
                    return rowsUpdated > 0 ? "Demand validated successfully." : "Demand cannot be validated as it is not in 'Waiting' state.";
                }
            } else {
                String deleteSql = "DELETE FROM Demands WHERE ID = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
                    preparedStatement.setInt(1, demandId);
                    int rowsDeleted = preparedStatement.executeUpdate();
                    return rowsDeleted > 0 ? "Demand rejected and deleted. Reason: " + reason : "Failed to reject demand.";
                }
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    @WebMethod
    public String chooseDemand(int demandId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String stateCheckQuery = "SELECT State FROM Demands WHERE ID = ?";
            try (PreparedStatement stateCheckStmt = connection.prepareStatement(stateCheckQuery)) {
                stateCheckStmt.setInt(1, demandId);
                try (ResultSet rs = stateCheckStmt.executeQuery()) {
                    if (rs.next()) {
                        String currentState = rs.getString("State");
                        if (!"Validated".equals(currentState)) {
                            return "Demand must be in 'Validated' state before moving to 'Ongoing'.";
                        }
                    } else {
                        return "Demand not found.";
                    }
                }
            }

            String sql = "UPDATE Demands SET State = 'Ongoing' WHERE ID = ? AND State = 'Validated'";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, demandId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Demand chosen successfully." : "Demand cannot be chosen as it is not in 'Validated' state.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    @WebMethod
    public String finishDemand(int demandId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String stateCheckQuery = "SELECT State FROM Demands WHERE ID = ?";
            try (PreparedStatement stateCheckStmt = connection.prepareStatement(stateCheckQuery)) {
                stateCheckStmt.setInt(1, demandId);
                try (ResultSet rs = stateCheckStmt.executeQuery()) {
                    if (rs.next()) {
                        String currentState = rs.getString("State");
                        if (!"Ongoing".equals(currentState)) {
                            return "Demand must be in 'Ongoing' state before moving to 'Done'.";
                        }
                    } else {
                        return "Demand not found.";
                    }
                }
            }

            String sql = "UPDATE Demands SET State = 'Done' WHERE ID = ? AND State = 'Ongoing'";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, demandId);
                int rowsUpdated = preparedStatement.executeUpdate();
                return rowsUpdated > 0 ? "Demand finished successfully." : "Demand cannot be finished as it is not in 'Ongoing' state.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String url = "http://localhost:8083/DemandServiceSOAP";
        Endpoint.publish(url, new DemandServiceSOAP());
        System.out.println("Service running at " + url);
    }
}
