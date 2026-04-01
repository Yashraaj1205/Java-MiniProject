package com.lendflow.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityException(DataIntegrityViolationException e, RedirectAttributes redirectAttributes) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("uk_borrowers_phone") || msg.contains("duplicate key value violates unique constraint")) {
            redirectAttributes.addFlashAttribute("duplicateError", "This phone number is already registered to another borrower!");
            return "redirect:/borrowers";
        }
        
        // Generic fallback for other unique constraints
        redirectAttributes.addFlashAttribute("error", "A database constraint was violated. Please check your data.");
        return "redirect:/dashboard";
    }
}
