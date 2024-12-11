package fr.insa.mas.AccountLoginMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/accounts")
public class AccountLoginMS {

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
        SpringApplication.run(AccountLoginMS.class, args);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AccountLogin account) {
        Map<String, Object> response = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT ID, Password FROM Users WHERE Email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, account.getEmail());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String hashedPassword = resultSet.getString("Password");
                        int userId = resultSet.getInt("ID");
                        if (BCrypt.checkpw(account.getPassword(), hashedPassword)) {
                            response.put("code", 0);
                            response.put("User_ID", userId);
                            return response;
                        } else {
                            response.put("code", 1);
                            response.put("User_ID", -1);
                            return response;
                        }
                    } else {
                        response.put("code", 2);
                        response.put("User_ID", -1);
                        return response;
                    }
                }
            }
        } catch (SQLException e) {
            response.put("code", 3);
            response.put("User_ID", -1);
            response.put("error", "Database error: " + e.getMessage());
            return response;
        }
    }
}

class AccountLogin {
    private String email;
    private String password;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}