package fr.insa.client.controller;

import fr.insa.client.model.Demand;
import fr.insa.client.model.ChooseRequest;
import fr.insa.client.model.Feedback;
import fr.insa.client.service.DemandService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class DemandController {

    @Autowired
    private DemandService demandService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        
        System.out.println("Dashboard accessed - UserId: " + userId + ", UserType: " + userType);
        
        if (userId == null) {
            System.out.println("No userId in session, redirecting to login");
            return "redirect:/";
        }

        List<Demand> demands;
        if ("Client".equals(userType)) {
            System.out.println("Getting demands for client userId: " + userId);
            demands = demandService.getDemandsByUser(userId);
        } else {
            System.out.println("Getting all demands for helper");
            demands = demandService.getAllValidatedDemands(userId);
        }
        
        System.out.println("Found " + (demands != null ? demands.size() : 0) + " demands");
        model.addAttribute("demands", demands);
        model.addAttribute("userType", userType);
        model.addAttribute("userId", userId);
        
        return userType.equals("Client") ? "client-dashboard" : "helper-dashboard";
    }

    @PostMapping("/api/demands/add")
    @ResponseBody
    public String createDemand(@RequestBody Demand demand, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "User not authenticated";
        }
        demand.setUser_ID(userId);
        return demandService.createDemand(demand);
    }

    // FEEDBACK METHODS
    @PostMapping("/api/feedback/add")
    @ResponseBody
    public String createFeedback(@RequestBody Feedback feedback, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "User not authenticated";
        }
        feedback.setUser_ID(userId);
        return demandService.createFeedback(feedback);
    }

    @GetMapping("/api/feedback/demand/{demandId}")
    @ResponseBody
    public List<Feedback> getFeedbackfromDemand(@PathVariable int demandId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return new ArrayList<>();
        }
        return demandService.getFeedbackfromDemand(demandId);
    }
    //__________________

    @PutMapping("/api/demands/choose")
    @ResponseBody
    public String chooseDemand(@RequestBody ChooseRequest request, HttpSession session) {
        System.out.println("Received choose demand request");
        System.out.println("Request body - Demand_ID: " + request.getDemand_ID() + ", Helper_ID: " + request.getHelper_ID());
        
        Integer userId = (Integer) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        
        System.out.println("Session info - userId: " + userId + ", userType: " + userType);
        
        if (userId == null) {
            System.out.println("Error: User not authenticated");
            return "User not authenticated";
        }
        if (!"Helper".equals(userType)) {
            System.out.println("Error: Invalid user type - " + userType);
            return "Only helpers can choose demands";
        }
        
        // Utiliser l'ID de la session directement
        return demandService.chooseDemand(request.getDemand_ID(), userId);
    }

    @PutMapping("/api/demands/finish/{demandId}")
    @ResponseBody
    public String finishDemand(@PathVariable int demandId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        
        System.out.println("\n=== Finish demand request ===");
        System.out.println("DemandId: " + demandId);
        System.out.println("UserId: " + userId);
        System.out.println("UserType: " + userType);
        
        if (userId == null) {
            System.out.println("Error: User not authenticated");
            return "User not authenticated";
        }
        
        if (!"Client".equals(userType)) {
            System.out.println("Error: Only clients can finish demands");
            return "Only clients can finish demands";
        }
        
        return demandService.finishDemand(demandId);
    }

    @GetMapping("/api/demands/get-all")
    @ResponseBody
    public List<Demand> getAllDemands(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            return new ArrayList<>();
        }

        if ("Client".equals(userType)) {
            return demandService.getDemandsByUser(userId);
        } else {
            return demandService.getAllValidatedDemands(userId);
        }
    }

    @GetMapping("/api/demands/refresh")
    @ResponseBody
    public List<Demand> refreshDemands(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        String userType = (String) session.getAttribute("userType");
        
        System.out.println("Refreshing demands - UserId: " + userId + ", UserType: " + userType);
        
        if (userId == null) {
            return Collections.emptyList();
        }

        if ("Client".equals(userType)) {
            return demandService.getDemandsByUser(userId);
        } else {
            return demandService.getAllValidatedDemands(userId);
        }
    }
}
