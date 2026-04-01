package com.lendflow.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.lendflow.entity.Loan;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${admin.notification.email}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // We can store the FCM token of the currently logged-in admin here in memory (or in DB).
    // For a single-admin system, storing one volatile token is fine.
    private String currentAdminFcmToken = null;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void initFirebase() {
        try {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase-adminsdk.json");
            if (serviceAccount == null) {
                log.warn("firebase-adminsdk.json not found in classpath. Push notifications will be disabled.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized successfully");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }

    public void registerFcmToken(String token) {
        this.currentAdminFcmToken = token;
        log.info("Registered new FCM Token for Admin push notifications.");
    }

    public void sendLoanIssuedNotification(Loan loan) {
        String title = "New Loan Issued: " + loan.getLoanReference();
        String body = String.format("Money issued to %s. Principal: ₹%s. Repayment due on %s. Total to receive: ₹%s.",
                loan.getBorrower().getName(),
                loan.getPrincipalAmount().setScale(0, RoundingMode.HALF_UP),
                loan.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                loan.getTotalPayable().setScale(0, RoundingMode.HALF_UP));
        
        String borrowerEmail = loan.getBorrower().getEmail();
        if (borrowerEmail != null && !borrowerEmail.isEmpty()) {
            sendEmail(borrowerEmail, title, body);
        } else {
            log.warn("Borrower {} does not have an email address. Skipping email.", loan.getBorrower().getName());
        }

        // 2. Send Push Notification via FCM (to the Admin)
        sendPushNotification(title, body);
    }

    public void sendDueReminderNotification(Loan loan) {
        String title = "Upcoming Due Date Reminder: " + loan.getLoanReference();
        String body = String.format("Reminder: You have a repayment due tomorrow (%s) from %s for ₹%s.",
                loan.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                loan.getBorrower().getName(),
                loan.getTotalPayable().setScale(0, RoundingMode.HALF_UP));
                
        String borrowerEmail = loan.getBorrower().getEmail();
        if (borrowerEmail != null && !borrowerEmail.isEmpty()) {
            sendEmail(borrowerEmail, title, body);
        } else {
            log.warn("Borrower {} does not have an email address. Skipping email.", loan.getBorrower().getName());
        }
                
        // 2. Send Push Notification via FCM (to the Admin)
        sendPushNotification(title, body);
    }

    private void sendEmail(String toEmail, String subject, String text) {
        if ("kalihrishikesh@gmail.com".equals(fromEmail) == false && "your.email@gmail.com".equals(fromEmail)) {
            log.warn("Skipping email notification, SMTP not fully configured.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            if (adminEmail != null && !adminEmail.isEmpty()) {
                message.setReplyTo(adminEmail);
            }
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent to Borrower: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to Borrower {}: {}", toEmail, e.getMessage());
        }
    }

    private void sendPushNotification(String title, String body) {
        if (currentAdminFcmToken == null) {
            log.warn("Skipping Push Notification: No FCM token registered (Admin not logged in or disabled notifications).");
            return;
        }

        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(currentAdminFcmToken)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push Notification sent successfully. Message ID: {}", response);
        } catch (Exception e) {
            log.error("Failed to send Push Notification: {}", e.getMessage());
        }
    }
}
