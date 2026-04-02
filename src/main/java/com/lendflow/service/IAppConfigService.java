package com.lendflow.service;

import com.lendflow.entity.AppConfig;
import org.springframework.web.multipart.MultipartFile;

public interface IAppConfigService {
    AppConfig getSettings();
    AppConfig updateSettings(AppConfig settings, MultipartFile qrCodeFile);
}
