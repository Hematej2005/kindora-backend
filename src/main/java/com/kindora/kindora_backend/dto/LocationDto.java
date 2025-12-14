package com.kindora.kindora_backend.dto;

import lombok.Data;

@Data
public class LocationDto {
    private String pincode;
    private String state;
    private String street;
    private String district;
    private String landmark;
    private Double lat;
    private Double lng;
}
