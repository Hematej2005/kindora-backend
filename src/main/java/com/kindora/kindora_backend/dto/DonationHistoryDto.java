package com.kindora.kindora_backend.dto;

import lombok.Data;

@Data
public class DonationHistoryDto {
    private Long id;
    private String type;        // FOOD | CLOTHES | ANIMAL
    private String createdAt;   // ISO / readable string
    private String status;      // AVAILABLE | ASSIGNED | PICKED | DELIVERED
    private VolunteerDto volunteer; // may be null
    private String proofUrl;    // may be null
}
