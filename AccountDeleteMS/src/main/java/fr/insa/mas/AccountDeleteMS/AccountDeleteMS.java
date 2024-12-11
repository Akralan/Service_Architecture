package fr.insa.mas.AccountDeleteMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.sql.*;

@SpringBootApplication
@RestController
@RequestMapping("/accounts")
public class AccountDeleteMS {

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
        SpringApplication.run(AccountDeleteMS.class, args);
    }

    @DeleteMapping("/delete/{accountId}")
    public String deleteAccount(@PathVariable int accountId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM Users WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, accountId);
                int rowsDeleted = statement.executeUpdate();
                return rowsDeleted > 0 ? "Account deleted successfully." : "Failed to delete account.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }
}