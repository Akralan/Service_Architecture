package fr.insa.mas.AccountCreationMS;

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
public class AccountCreationMS {

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
        SpringApplication.run(AccountCreationMS.class, args);
    }

    @PostMapping("/create")
    public Map<String, Integer> createAccount(@RequestBody Account account) {
        Map<String, Integer> response = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String hashedPassword = BCrypt.hashpw(account.getPassword(), BCrypt.gensalt());
            String sql = "INSERT INTO Users (Name, Surname, Email, Password, Birthdate, Type, Sex) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, account.getName());
                statement.setString(2, account.getSurname());
                statement.setString(3, account.getEmail());
                statement.setString(4, hashedPassword);
                statement.setString(5, account.getBirthdate());
                statement.setString(6, account.getType());
                statement.setString(7, account.getSex());
                int rowsInserted = statement.executeUpdate();
                response.put("code", rowsInserted > 0 ? 0 : -1); // 0 si compte créé, -1 pour une erreur
                return response;
            }
        } catch (SQLException e) {
            // Vérifier si l'erreur est liée à la contrainte UNIQUE sur l'email
            if (e.getSQLState().equals("23000")) {
                response.put("code", 1);
            } else {
                response.put("code", -1);
            }
            return response;
        }
    }
}

class Account {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String birthdate;
    private String type;
    private String sex;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
}
