package com.kindora.kindora_backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "donation_images")
public class DonationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donation_id", nullable = false)
    private Long donationId;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist(){ this.createdAt = Instant.now(); }

    // getters/setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public Long getDonationId(){ return donationId; }
    public void setDonationId(Long donationId){ this.donationId = donationId; }

    public String getImageUrl(){ return imageUrl; }
    public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }

    public Instant getCreatedAt(){ return createdAt; }
    public void setCreatedAt(Instant createdAt){ this.createdAt = createdAt; }
}
