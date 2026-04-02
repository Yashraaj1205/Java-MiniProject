package com.lendflow.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.lendflow.entity.Loan;
import com.lendflow.entity.AppConfig;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationServiceImpl implements INotificationService {

    private final JavaMailSender mailSender;
    private final IAppConfigService appConfigService;

    @Value("${admin.notification.email}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private String currentAdminFcmToken = null;

    public NotificationServiceImpl(JavaMailSender mailSender, IAppConfigService appConfigService) {
        this.mailSender = mailSender;
        this.appConfigService = appConfigService;
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

    @Override
    public void registerFcmToken(String token) {
        this.currentAdminFcmToken = token;
        log.info("Registered new FCM Token for Admin push notifications.");
    }

    @Override
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
        }

        sendPushNotification(title, body);
    }

    @Override
    public void sendDueReminderNotification(Loan loan) {
        String title = "Upcoming Due Date Reminder: " + loan.getLoanReference();
        String body = String.format("Reminder: You have a repayment due tomorrow (%s) from %s for ₹%s.",
                loan.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                loan.getBorrower().getName(),
                loan.getTotalPayable().setScale(0, RoundingMode.HALF_UP));
                
        String borrowerEmail = loan.getBorrower().getEmail();
        if (borrowerEmail != null && !borrowerEmail.isEmpty()) {
            sendEmail(borrowerEmail, title, body);
        }
                
        sendPushNotification(title, body);
    }

    private void sendEmail(String toEmail, String subject, String text) {
        if ("kalihrishikesh@gmail.com".equals(fromEmail) == false && "your.email@gmail.com".equals(fromEmail)) {
            return;
        }

        try {
            AppConfig settings = appConfigService.getSettings();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            String htmlMsg = "<div style='font-family: sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;'>" +
                    "<div style='background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%); padding: 30px; text-align: center; color: white;'>" +
                    "<h1 style='margin: 0; font-size: 24px;'>LendFlow</h1>" +
                    "<p style='margin-top: 10px; opacity: 0.9;'>Smart Loan Management</p>" +
                    "</div>" +
                    "<div style='padding: 30px; color: #1e293b; line-height: 1.6;'>" +
                    "<p style='white-space: pre-wrap;'>" + text + "</p>" +
                    "</div>" +
                    "<div style='padding: 0 30px 30px;'>" +
                    "<div style='background-color: #f8fafc; border-radius: 10px; padding: 20px; border: 1px solid #f1f5f9;'>" +
                    "<h3 style='margin-top: 0; color: #8b5cf6;'>Repayment Details</h3>" +
                    "<p style='margin-bottom: 5px;'><strong>UPI ID:</strong> " + settings.getUpiId() + "</p>";

            if (settings.getQrCodePath() != null) {
                htmlMsg += "<p><strong>Scan QR to Pay:</strong></p>" +
                           "<img src='cid:qrCode' style='max-width: 150px; border-radius: 8px;' />";
            }

            htmlMsg += "</div></div>" +
                    "<div style='background-color: #f1f5f9; padding: 20px; text-align: center; color: #94a3b8; font-size: 12px;'>" +
                    "&copy; " + java.time.Year.now().getValue() + " LendFlow. All rights reserved." +
                    "</div></div>";

            helper.setText(htmlMsg, true);

            if (settings.getQrCodePath() != null) {
                String fullPath = settings.getQrCodePath().replaceFirst("^/", "");
                java.io.File file = new java.io.File(fullPath);
                if (file.exists()) {
                    helper.addInline("qrCode", file);
                }
            }

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage());
        }
    }

    private void sendPushNotification(String title, String body) {
        if (currentAdminFcmToken == null) {
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

            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.error("Failed to send Push Notification: {}", e.getMessage());
        }
    }
}

