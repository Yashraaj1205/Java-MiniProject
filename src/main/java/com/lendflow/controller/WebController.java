package com.lendflow.controller;

import com.lendflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("summary", dashboardService.getSummary());
        model.addAttribute("recentLoans", dashboardService.getRecentLoans());
        return "dashboard";
    }




    @GetMapping("/settings")
    public String settings(Model model) {
        return "settings"; // To be implemented
    }
}
