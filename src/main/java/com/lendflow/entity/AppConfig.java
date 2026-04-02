package com.lendflow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "app_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AppConfig extends BaseEntity {
    private String upiId;
    private String qrCodePath;
    private String ownerName;
}
