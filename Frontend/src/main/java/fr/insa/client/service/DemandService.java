package fr.insa.client.service;

import fr.insa.client.model.Demand;
import fr.insa.client.model.ChooseRequest;
import fr.insa.client.model.Feedback;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DemandService {
    private static final String DEMAND_SERVICE_URL = "http://localhost:8088/demands";
    private static final String FEEDBACK_SERVICE_URL = "http://localhost:8089/feedback";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DemandService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Demand> getDemandsByUser(int userId) {
        try {
            String url = DEMAND_SERVICE_URL + "/get/" + userId;
            return Arrays.asList(restTemplate.getForObject(url, Demand[].class));
        } catch (Exception e) {
            System.err.println("Error getting demands: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Demand> getAllValidatedDemands(int userId) {
        try {
            String url = DEMAND_SERVICE_URL + "/get-all/" + userId;
            // Log de la réponse brute
            String rawResponse = restTemplate.getForObject(url, String.class);
            System.out.println("Raw JSON response: " + rawResponse);
            
            Demand[] demands = restTemplate.getForObject(url, Demand[].class);
            if (demands != null && demands.length > 0) {
                System.out.println("First demand from get-all: " + demands[0].toString());
            }
            return demands != null ? Arrays.asList(demands) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error fetching validated demands: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public String createDemand(Demand demand) {
        try {
            String url = DEMAND_SERVICE_URL + "/add";
            return restTemplate.postForObject(url, demand, String.class);
        } catch (Exception e) {
            System.err.println("Error creating demand: " + e.getMessage());
            return "Error creating demand: " + e.getMessage();
        }
    }

    // FEEDBACK METHODS
    public String createFeedback(Feedback feedback) {
        try {
            String url = FEEDBACK_SERVICE_URL + "/add";
            return restTemplate.postForObject(url, feedback, String.class);
        } catch (Exception e) {
            System.err.println("Error creating feedback: " + e.getMessage());
            return "Error creating feedback: " + e.getMessage();
        }
    }

    public List<Feedback> getFeedbackfromDemand(int demandId) {
        try {
            String url = FEEDBACK_SERVICE_URL + "/demand/" + demandId;
            return Arrays.asList(restTemplate.getForObject(url, Feedback[].class));
        } catch (Exception e) {
            System.err.println("Error getting feedbacks: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    //__________________

    public String chooseDemand(int demandId, int helperId) {
        System.out.println("Attempting to choose demand with ID: " + demandId + " for helper: " + helperId);
        
        ChooseRequest request = new ChooseRequest();
        request.setDemand_ID(demandId);
        request.setHelper_ID(helperId);
        
        try {
            System.out.println("Sending choose request: " + objectMapper.writeValueAsString(request));
            restTemplate.put(DEMAND_SERVICE_URL + "/choose", request);
            System.out.println("Choose request sent successfully");
            return "Demand chosen successfully";
            
        } catch (Exception e) {
            System.err.println("Error choosing demand: " + e.getMessage());
            e.printStackTrace();
            return "Error choosing demand: " + e.getMessage();
        }
    }

    public String finishDemand(int demandId) {
        try {
            String url = DEMAND_SERVICE_URL + "/finish/" + demandId;
            System.out.println("\n=== Finishing demand ===");
            System.out.println("Full URL: " + url);
            
            // On fait juste un PUT
            restTemplate.put(url, null);
            System.out.println("Demand finished successfully");
            
            return "Demand finished successfully";
        } catch (Exception e) {
            System.out.println("\n=== Error finishing demand ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return "Error finishing demand: " + e.getMessage();
        }
    }
}
