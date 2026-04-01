package com.lendflow.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.lendflow.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final NotificationService notificationService;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("admin") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<String> login(@RequestParam("idToken") String idToken,
                                        @RequestParam(value = "fcmToken", required = false) String fcmToken,
                                        HttpSession session) {
        try {
            // Verify the Firebase ID token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            
            log.info("User {} ({}) authenticated via Firebase", uid, email);
            
            // Mark session as admin
            session.setAttribute("admin", true);
            session.setAttribute("adminEmail", email);
            
            // If they provided a push notification registration token, save it for future alerts
            if (fcmToken != null && !fcmToken.isEmpty()) {
                notificationService.registerFcmToken(fcmToken);
            }
            
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            log.error("Failed to authenticate Firebase token", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }
}
