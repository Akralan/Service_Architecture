package fr.insa.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feedback {
    @JsonProperty("id")
    private int id;  // Pour la réception
    
    @JsonProperty("userId")
    private int userId;  // Pour la réception
    
    @JsonProperty("demandId")
    private int demandId;
    
    @JsonProperty("content")
    private String content;

    // Getters et Setters
    public int getDemand_ID() {  // Garde l'ancien nom pour compatibilité
        return demandId;
    }

    public void setDemand_ID(int demandId) {
        this.demandId = demandId;
    }

    public int getUser_ID() {  // Garde l'ancien nom pour compatibilité
        return userId;
    }

    public void setUser_ID(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Demand{" +
                "id=" + id +
                ", userId=" + userId +
                ", helperId=" + demandId +
                ", name='" + content + '\'' +
                '}';
    }
}
