package com.kindora.kindora_backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "sub_types", length = 255)
    private String subTypes;

    @Column(length = 1000)
    private String description;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "cloth_groups", length = 255)
    private String clothGroups;

    @Column(name = "animal_suitable_for", length = 500)
    private String animalSuitableFor;

    @Column(name = "animal_food_type", length = 500)
    private String animalFoodType;

    @Column(name = "pin_code", length = 20)
    private String pinCode;

    @Column(length = 120)
    private String state;

    @Column(length = 120)
    private String district;

    @Column(length = 255)
    private String street;

    @Column(length = 255)
    private String landmark;

    private Double lat;
    private Double lng;

    @Column(name = "available_from", nullable = false)
    private Instant availableFrom;

    @Column(name = "available_to", nullable = false)
    private Instant availableTo;

    @Column(name = "assigned_member_id")
    private Long assignedMemberId;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) this.status = "AVAILABLE";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- getters & setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubTypes() { return subTypes; }
    public void setSubTypes(String subTypes) { this.subTypes = subTypes; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public String getClothGroups() { return clothGroups; }
    public void setClothGroups(String clothGroups) { this.clothGroups = clothGroups; }

    public String getAnimalSuitableFor() { return animalSuitableFor; }
    public void setAnimalSuitableFor(String animalSuitableFor) { this.animalSuitableFor = animalSuitableFor; }

    public String getAnimalFoodType() { return animalFoodType; }
    public void setAnimalFoodType(String animalFoodType) { this.animalFoodType = animalFoodType; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Instant getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(Instant availableFrom) { this.availableFrom = availableFrom; }

    public Instant getAvailableTo() { return availableTo; }
    public void setAvailableTo(Instant availableTo) { this.availableTo = availableTo; }

    public Long getAssignedMemberId() { return assignedMemberId; }
    public void setAssignedMemberId(Long assignedMemberId) { this.assignedMemberId = assignedMemberId; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
