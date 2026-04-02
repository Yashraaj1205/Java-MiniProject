package com.lendflow.service;

import com.lendflow.entity.AppConfig;
import com.lendflow.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppConfigServiceImpl implements IAppConfigService {

    private final AppConfigRepository appConfigRepository;
    private final String UPLOAD_DIR = "uploads/qr/";

    @Override
    public AppConfig getSettings() {
        return appConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> appConfigRepository.save(AppConfig.builder()
                        .ownerName("LendFlow Owner")
                        .upiId("your-upi@id")
                        .build()));
    }

    @Override
    @Transactional
    public AppConfig updateSettings(AppConfig settings, MultipartFile qrCodeFile) {
        AppConfig existing = getSettings();
        existing.setUpiId(settings.getUpiId());
        existing.setOwnerName(settings.getOwnerName());

        if (qrCodeFile != null && !qrCodeFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + qrCodeFile.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(qrCodeFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                existing.setQrCodePath("/" + UPLOAD_DIR + fileName);
            } catch (IOException e) {
                log.error("Failed to upload QR code: {}", e.getMessage());
            }
        }

        return appConfigRepository.save(existing);
    }
}
