package com.lendflow.controller;

import com.lendflow.entity.RecoveryAction;
import com.lendflow.service.IRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/recovery")
@RequiredArgsConstructor
public class RecoveryController {

    private final IRecoveryService recoveryService;

    @PostMapping("/add")
    public String addRecoveryAction(@RequestParam Long loanId, @ModelAttribute RecoveryAction action) {
        recoveryService.addRecoveryAction(loanId, action);
        return "redirect:/loans/" + loanId;
    }
}

