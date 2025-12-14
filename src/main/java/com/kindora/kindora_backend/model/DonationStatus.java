package com.kindora.kindora_backend.model;

/**
 * Donation status. Matches SQL ENUM('AVAILABLE','ASSIGNED','PICKED','DELIVERED')
 */
public enum DonationStatus {
    AVAILABLE,
    ASSIGNED,
    PICKED,
    DELIVERED
}
