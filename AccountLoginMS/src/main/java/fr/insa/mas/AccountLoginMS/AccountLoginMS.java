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
        System.out.println("Tentative de connexion pour l'email : " + account.getEmail());

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connexion à la base de données réussie.");

            String sql = "SELECT ID, Password, Type FROM Users WHERE Email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, account.getEmail());
                System.out.println("Requête SQL préparée : " + sql);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String hashedPassword = resultSet.getString("Password");
                        int userId = resultSet.getInt("ID");
                        String userType = resultSet.getString("Type");

                        if (BCrypt.checkpw(account.getPassword(), hashedPassword)) {
                            System.out.println("Connexion réussie pour l'utilisateur ID : " + userId + " Type : " + userType);
                            response.put("code", 0);         // Connexion réussie
                            response.put("User_ID", userId); 
                            response.put("Type", userType); 
                            return response;
                        } else {
                            System.out.println("Mot de passe incorrect pour l'email : " + account.getEmail());
                            response.put("code", 1);        // Mot de passe incorrect
                            response.put("User_ID", -1);
                            return response;
                        }
                    } else {
                        System.out.println("Utilisateur introuvable pour l'email : " + account.getEmail());
                        response.put("code", 2);            // Utilisateur introuvable
                        response.put("User_ID", -1);
                        return response;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur de base de données lors de la connexion de l'email : " + account.getEmail() + 
                               ". Message : " + e.getMessage());
            response.put("code", 3);        // Erreur de base de données
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