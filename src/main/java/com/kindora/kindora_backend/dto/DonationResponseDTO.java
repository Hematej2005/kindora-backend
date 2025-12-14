package com.kindora.kindora_backend.dto;

import java.time.Instant;
import java.util.List;

public class DonationResponseDTO {
    private Long id;
    private Long donorId;
    private String type;
    private List<String> subTypes;
    private String description;
    private Integer totalCount;
    private List<String> clothGroups;
    private String animalSuitableFor;
    private String animalFoodType;

    private String pinCode;
    private String street;
    private String district;
    private String state;
    private String landmark;

    private Double lat;
    private Double lng;

    private Instant availableFrom;
    private Instant availableTo;
    private String status;
    private Instant createdAt;

    private List<DonationItemDTO> items;
    private List<String> images;

    public static class DonationItemDTO {
        private Long id;
        private String itemName;
        private String quantity;
        private Integer servesFor;
        private String itemCondition;
        private String ageGroup;
        // getters/setters
        public Long getId(){ return id; } public void setId(Long id){ this.id = id; }
        public String getItemName(){ return itemName; } public void setItemName(String itemName){ this.itemName = itemName; }
        public String getQuantity(){ return quantity; } public void setQuantity(String quantity){ this.quantity = quantity; }
        public Integer getServesFor(){ return servesFor; } public void setServesFor(Integer servesFor){ this.servesFor = servesFor; }
        public String getItemCondition(){ return itemCondition; } public void setItemCondition(String itemCondition){ this.itemCondition = itemCondition; }
        public String getAgeGroup(){ return ageGroup; } public void setAgeGroup(String ageGroup){ this.ageGroup = ageGroup; }
    }

    // getters / setters (generate)
    public Long getId(){ return id; } public void setId(Long id){ this.id = id; }
    public Long getDonorId(){ return donorId; } public void setDonorId(Long donorId){ this.donorId = donorId; }
    public String getType(){ return type; } public void setType(String type){ this.type = type; }
    public List<String> getSubTypes(){ return subTypes; } public void setSubTypes(List<String> subTypes){ this.subTypes = subTypes; }
    public String getDescription(){ return description; } public void setDescription(String description){ this.description = description; }
    public Integer getTotalCount(){ return totalCount; } public void setTotalCount(Integer totalCount){ this.totalCount = totalCount; }
    public List<String> getClothGroups(){ return clothGroups; } public void setClothGroups(List<String> clothGroups){ this.clothGroups = clothGroups; }
    public String getAnimalSuitableFor(){ return animalSuitableFor; } public void setAnimalSuitableFor(String animalSuitableFor){ this.animalSuitableFor = animalSuitableFor; }
    public String getAnimalFoodType(){ return animalFoodType; } public void setAnimalFoodType(String animalFoodType){ this.animalFoodType = animalFoodType; }
    public String getPinCode(){ return pinCode; } public void setPinCode(String pinCode){ this.pinCode = pinCode; }
    public String getStreet(){ return street; } public void setStreet(String street){ this.street = street; }
    public String getDistrict(){ return district; } public void setDistrict(String district){ this.district = district; }
    public String getState(){ return state; } public void setState(String state){ this.state = state; }
    public String getLandmark(){ return landmark; } public void setLandmark(String landmark){ this.landmark = landmark; }
    public Double getLat(){ return lat; } public void setLat(Double lat){ this.lat = lat; }
    public Double getLng(){ return lng; } public void setLng(Double lng){ this.lng = lng; }
    public Instant getAvailableFrom(){ return availableFrom; } public void setAvailableFrom(Instant availableFrom){ this.availableFrom = availableFrom; }
    public Instant getAvailableTo(){ return availableTo; } public void setAvailableTo(Instant availableTo){ this.availableTo = availableTo; }
    public String getStatus(){ return status; } public void setStatus(String status){ this.status = status; }
    public Instant getCreatedAt(){ return createdAt; } public void setCreatedAt(Instant createdAt){ this.createdAt = createdAt; }
    public List<DonationItemDTO> getItems(){ return items; } public void setItems(List<DonationItemDTO> items){ this.items = items; }
    public List<String> getImages(){ return images; } public void setImages(List<String> images){ this.images = images; }
}
