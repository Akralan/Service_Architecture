package fr.insa.client.controller;

import fr.insa.client.model.Account;
import fr.insa.client.model.AccountLogin;
import fr.insa.client.service.AccountService;
import fr.insa.client.service.DemandService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class AccountController {
    
    private final AccountService accountService;
    private final DemandService demandService;

    @Autowired
    public AccountController(AccountService accountService, DemandService demandService) {
        this.accountService = accountService;
        this.demandService = demandService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("loginRequest", new AccountLogin());
        return "index";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute Account account, Model model) {
        try {
            System.out.println("=== Controller receiving register request ===");
            System.out.println("Account data received: " + account);
            
            var result = accountService.createAccount(account);
            String message;
            
            Integer code = result.get("code");
            if (code == null) {
                message = "Unexpected response from registration service";
            } else {
                switch (code) {
                    case 0:
                        message = "Account created successfully!";
                        break;
                    case 1:
                        message = "Email already exists!";
                        break;
                    case -1:
                        message = "Error connecting to registration service";
                        break;
                    default:
                        message = "Error creating account!";
                }
            }
            
            System.out.println("Registration result message: " + message);
            
            model.addAttribute("message", message);
            model.addAttribute("account", new Account());
            model.addAttribute("loginRequest", new AccountLogin());
            return "index";
        } catch (Exception e) {
            System.err.println("=== Controller error during registration ===");
            System.err.println(e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("message", "Error during registration: " + e.getMessage());
            model.addAttribute("account", new Account());
            model.addAttribute("loginRequest", new AccountLogin());
            return "index";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        try {
            AccountLogin accountLogin = new AccountLogin();
            accountLogin.setEmail(email);
            accountLogin.setPassword(password);
            
            Map<String, Object> response = accountService.login(accountLogin);
            System.out.println("Login response: " + response);
            
            if (response != null && response.get("User_ID") != null) {
                session.setAttribute("userId", response.get("User_ID"));
                session.setAttribute("userType", response.get("Type"));
                
                String userType = (String) response.get("Type");
                System.out.println("User type: " + userType);
                
                if ("Client".equals(userType)) {
                    return "redirect:/dashboard/client";
                } else if ("Helper".equals(userType)) {
                    return "redirect:/dashboard/helper";
                }
            }
            return "redirect:/?error=Invalid login response";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/dashboard/client")
    public String clientDashboard(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        model.addAttribute("demands", demandService.getAllValidatedDemands(userId));
        model.addAttribute("userType", session.getAttribute("userType"));
        return "client-dashboard";
    }

    @GetMapping("/dashboard/helper")
    public String helperDashboard(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (session.getAttribute("userId") == null) {
            return "redirect:/";
        }
        model.addAttribute("demands", demandService.getAllValidatedDemands(userId));
        model.addAttribute("userType", session.getAttribute("userType"));
        return "helper-dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable int id, Model model) {
        String result = accountService.deleteAccount(id);
        model.addAttribute("message", result);
        return "index";
    }
}
