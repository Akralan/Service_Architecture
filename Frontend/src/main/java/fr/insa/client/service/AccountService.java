package fr.insa.client.service;

import fr.insa.client.model.Account;
import fr.insa.client.model.AccountLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {
    private final RestTemplate restTemplate;
    private static final String CREATE_URL = "http://localhost:8085/accounts/create";
    private static final String LOGIN_URL = "http://localhost:8086/accounts/login";
    private static final String DELETE_URL = "http://localhost:8087/accounts/delete/{id}";

    @Autowired
    public AccountService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Integer> createAccount(Account account) {
        try {
            System.out.println("=== Starting account creation request ===");
            System.out.println("URL: " + CREATE_URL);
            System.out.println("Request data: " + account);
            
            Map<String, Integer> response = restTemplate.postForObject(CREATE_URL, account, Map.class);
            
            System.out.println("Response received: " + response);
            System.out.println("=== End account creation request ===");
            return response;
        } catch (Exception e) {
            System.err.println("=== Error during account creation ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            Map<String, Integer> errorResponse = new HashMap<>();
            errorResponse.put("code", -1);
            return errorResponse;
        }
    }

    public Map<String, Object> login(AccountLogin accountLogin) {
        try {
            System.out.println("=== Starting login request ===");
            System.out.println("URL: " + LOGIN_URL);
            System.out.println("Request data: email=" + accountLogin.getEmail() + ", password=" + accountLogin.getPassword());
            
            Map<String, Object> response = restTemplate.postForObject(LOGIN_URL, accountLogin, Map.class);
            
            System.out.println("Response received: " + response);
            if (response != null) {
                System.out.println("Response details:");
                System.out.println("- ID: " + response.get("id"));
                System.out.println("- Type: " + response.get("Type"));
                System.out.println("- Available keys: " + response.keySet());
            }
            System.out.println("=== End login request ===");
            return response;
        } catch (Exception e) {
            System.err.println("=== Error during login ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", -1);
            errorResponse.put("message", "Error connecting to login service: " + e.getMessage());
            return errorResponse;
        }
    }

    public String deleteAccount(int accountId) {
        ResponseEntity<String> response = restTemplate.exchange(
            DELETE_URL, 
            HttpMethod.DELETE,
            null,
            String.class,
            accountId
        );
        return response.getBody();
    }
}
