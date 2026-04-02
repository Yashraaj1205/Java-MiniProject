package com.lendflow.config;

import com.lendflow.exception.ResourceNotFoundException;
import com.lendflow.exception.ValidationException;
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
        
        redirectAttributes.addFlashAttribute("error", "A database constraint was violated. Please check your data.");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        return "redirect:/dashboard";
    }
}

