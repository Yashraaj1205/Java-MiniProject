package com.lendflow.controller;

import com.lendflow.entity.AppConfig;
import com.lendflow.service.IAppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final IAppConfigService appConfigService;

    @GetMapping
    public String showSettings(Model model) {
        model.addAttribute("settings", appConfigService.getSettings());
        return "settings";
    }

    @PostMapping("/update")
    public String updateSettings(@ModelAttribute AppConfig settings,
                               @RequestParam(value = "qrCodeFile", required = false) MultipartFile qrCodeFile,
                               RedirectAttributes redirectAttributes) {
        appConfigService.updateSettings(settings, qrCodeFile);
        redirectAttributes.addFlashAttribute("success", "Settings updated successfully!");
        return "redirect:/admin/settings";
    }
}
