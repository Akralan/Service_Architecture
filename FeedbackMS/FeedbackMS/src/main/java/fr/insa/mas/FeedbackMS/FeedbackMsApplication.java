package fr.insa.mas.FeedbackMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@SpringBootApplication
@RestController
@RequestMapping("/comments")
public class FeedbackMsApplication {

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
        SpringApplication.run(FeedbackMsApplication.class, args);
    }

    // Ajouter un commentaire
    @PostMapping("/add")
    public String addComment(@RequestBody FeedbackRequest request) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
			// Vérifier que l'utilisateur existe et peut commenter
            String checkUserSql = "SELECT Type FROM Users WHERE ID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkUserSql)) {
                checkStatement.setInt(1, request.getUserId());
                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (!rs.next()) {
                        return "User not found.";
                    }
                }
            }

            // Vérifier que la demande existe
            String checkDemandSql = "SELECT ID FROM Demands WHERE ID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkDemandSql)) {
                checkStatement.setInt(1, request.getDemandId());
                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (!rs.next()) {
                        return "Demand not found.";
                    }
                }
            }

            // Insérer le commentaire
            String sql = "INSERT INTO Comments (Demand_ID, User_ID, Content, Creation_date) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, request.getDemandId());
                statement.setInt(2, request.getUserId());
                statement.setString(3, request.getContent());
                statement.executeUpdate();
                return "Comment added successfully.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error adding comment: " + e.getMessage();
        }
    }

    // Récupérer tous les commentaires d'une demande
    @GetMapping("/demand/{demandId}")
    public List<Feedback> getCommentsByDemand(@PathVariable int demandId) {
        List<Feedback> comments = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT c.*, u.Type as UserType FROM Comments c " +
                        "JOIN Users u ON c.User_ID = u.ID " +
                        "WHERE c.Demand_ID = ? " +
                        "ORDER BY c.Creation_date DESC";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, demandId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                    	Feedback comment = new Feedback();
                        comment.setId(rs.getInt("ID"));
                        comment.setDemandId(rs.getInt("Demand_ID"));
                        comment.setUserId(rs.getInt("User_ID"));
                        comment.setContent(rs.getString("Content"));
                        comment.setCreationDate(rs.getTimestamp("Creation_date"));
                        comments.add(comment);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // Modifier un commentaire
    @PutMapping("/update")
    public String updateComment(@RequestBody FeedbackUpdateRequest request) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Vérifier que l'utilisateur est propriétaire du commentaire
            String checkOwnerSql = "SELECT User_ID FROM Comments WHERE ID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkOwnerSql)) {
                checkStatement.setInt(1, request.getFeedbackId());
                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (!rs.next()) {
                        return "Comment not found.";
                    }
                    if (rs.getInt("User_ID") != request.getUserId()) {
                        return "You can only modify your own comments.";
                    }
                }
            }

            // Mettre à jour le commentaire
            String sql = "UPDATE Comments SET Content = ? WHERE ID = ? AND User_ID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, request.getContent());
                statement.setInt(2, request.getFeedbackId());
                statement.setInt(3, request.getUserId());
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0 ? "Comment updated successfully." : "No changes made.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error updating comment: " + e.getMessage();
        }
    }

    // Supprimer un commentaire
    @DeleteMapping("/{commentId}/user/{userId}")
    public String deleteComment(@PathVariable int commentId, @PathVariable int userId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Vérifier que l'utilisateur est propriétaire du commentaire
            String checkOwnerSql = "SELECT User_ID FROM Comments WHERE ID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkOwnerSql)) {
                checkStatement.setInt(1, commentId);
                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (!rs.next()) {
                        return "Comment not found.";
                    }
                    if (rs.getInt("User_ID") != userId) {
                        return "You can only delete your own comments.";
                    }
                }
            }

            // Supprimer le commentaire
            String sql = "DELETE FROM Comments WHERE ID = ? AND User_ID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, commentId);
                statement.setInt(2, userId);
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0 ? "Comment deleted successfully." : "No comment deleted.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error deleting comment: " + e.getMessage();
        }
    }
}


class Feedback {
    private int id;
    private int demandId;
    private int userId;
    private String content;
    private Date creationDate;

    // Getters
    public int getId() {
        return id;
    }

    public int getDemandId() {
        return demandId;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setDemandId(int demandId) {
        this.demandId = demandId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}

class FeedbackRequest {
    private int userId;
    private int demandId;
    private String content;

    // Getters
    public int getUserId() {
        return userId;
    }

    public int getDemandId() {
        return demandId;
    }

    public String getContent() {
        return content;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDemandId(int demandId) {
        this.demandId = demandId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

class FeedbackUpdateRequest {
    private int feedbackId;
    private int userId;
    private String content;

    // Getters
    public int getFeedbackId() {
        return feedbackId;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    // Setters
    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
