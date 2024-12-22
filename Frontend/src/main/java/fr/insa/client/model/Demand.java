package fr.insa.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Demand {
    @JsonProperty("id")
    private int id;  // Pour la réception
    
    @JsonProperty("userId")
    private int userId;  // Pour la réception
    
    @JsonProperty("helperId")
    private int helperId;
    
    @JsonProperty("name")
    private String name;
    
    private String description;
    private String state;
    private String priority;

    // Getters et Setters
    public int getDemand_ID() {  // Garde l'ancien nom pour compatibilité
        return id;
    }

    public void setDemand_ID(int id) {
        this.id = id;
    }

    public int getUser_ID() {  // Garde l'ancien nom pour compatibilité
        return userId;
    }

    public void setUser_ID(int userId) {
        this.userId = userId;
    }

    public int getHelper_ID() {
        return helperId;
    }

    public void setHelper_ID(int helperId) {
        this.helperId = helperId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Demand{" +
                "id=" + id +
                ", userId=" + userId +
                ", helperId=" + helperId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", state='" + state + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}
