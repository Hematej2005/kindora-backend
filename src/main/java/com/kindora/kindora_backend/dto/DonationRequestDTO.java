package com.kindora.kindora_backend.dto;

import java.util.List;

public class DonationRequestDTO {
    private String type;                  // FOOD | CLOTHES | ANIMAL
    private List<String> subTypes;        // e.g. ["TIFFIN","SNACKS"]
    private String description;
    private Integer totalCount;           // for clothes
    private List<String> clothGroups;     // ["CHILD","ADULT"]
    private String animalSuitableFor;     // CSV or text e.g. "dogs,cats"
    private String animalFoodType;        // CSV or text

    private String pinCode;
    private String street;
    private String district;
    private String state;
    private String landmark;

    private String availableFrom;         // ISO or "HH:mm"
    private String availableTo;

    private Double lat;
    private Double lng;

    private List<ItemDTO> items;

    public static class ItemDTO {
        private String itemName;
        private String quantity;
        private Integer servesFor;
        private String itemCondition; // GOOD/USED/WORN
        private String ageGroup;      // CHILD/ADULT/OLD

        public String getItemName(){ return itemName; } public void setItemName(String itemName){ this.itemName = itemName; }
        public String getQuantity(){ return quantity; } public void setQuantity(String quantity){ this.quantity = quantity; }
        public Integer getServesFor(){ return servesFor; } public void setServesFor(Integer servesFor){ this.servesFor = servesFor; }
        public String getItemCondition(){ return itemCondition; } public void setItemCondition(String itemCondition){ this.itemCondition = itemCondition; }
        public String getAgeGroup(){ return ageGroup; } public void setAgeGroup(String ageGroup){ this.ageGroup = ageGroup; }
    }

    // getters / setters (generate or paste)
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
    public String getAvailableFrom(){ return availableFrom; } public void setAvailableFrom(String availableFrom){ this.availableFrom = availableFrom; }
    public String getAvailableTo(){ return availableTo; } public void setAvailableTo(String availableTo){ this.availableTo = availableTo; }
    public Double getLat(){ return lat; } public void setLat(Double lat){ this.lat = lat; }
    public Double getLng(){ return lng; } public void setLng(Double lng){ this.lng = lng; }
    public List<ItemDTO> getItems(){ return items; } public void setItems(List<ItemDTO> items){ this.items = items; }
}
