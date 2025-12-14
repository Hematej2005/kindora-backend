package com.kindora.kindora_backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "donation_items")
public class DonationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donation_id", nullable = false)
    private Long donationId;

    @Column(name = "item_name", length = 255)
    private String itemName;

    // keep as string to support "4 containers" or numeric values
    @Column(length = 100)
    private String quantity;

    @Column(name = "serves_for")
    private Integer servesFor;

    @Column(name = "item_condition", length = 20)
    private String itemCondition;   // GOOD / USED / WORN

    @Column(name = "age_group", length = 20)
    private String ageGroup;        // CHILD / ADULT / OLD

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }

    // getters & setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public Long getDonationId(){ return donationId; }
    public void setDonationId(Long donationId){ this.donationId = donationId; }

    public String getItemName(){ return itemName; }
    public void setItemName(String itemName){ this.itemName = itemName; }

    public String getQuantity(){ return quantity; }
    public void setQuantity(String quantity){ this.quantity = quantity; }

    public Integer getServesFor(){ return servesFor; }
    public void setServesFor(Integer servesFor){ this.servesFor = servesFor; }

    public String getItemCondition(){ return itemCondition; }
    public void setItemCondition(String itemCondition){ this.itemCondition = itemCondition; }

    public String getAgeGroup(){ return ageGroup; }
    public void setAgeGroup(String ageGroup){ this.ageGroup = ageGroup; }

    public Instant getCreatedAt(){ return createdAt; }
    public void setCreatedAt(Instant createdAt){ this.createdAt = createdAt; }
}
