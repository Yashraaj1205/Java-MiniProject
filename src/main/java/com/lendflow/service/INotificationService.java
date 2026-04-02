package com.lendflow.service;

import com.lendflow.entity.Loan;

public interface INotificationService {
    void registerFcmToken(String token);
    void sendLoanIssuedNotification(Loan loan);
    void sendDueReminderNotification(Loan loan);
}

