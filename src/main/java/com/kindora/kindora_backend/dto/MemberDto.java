package com.kindora.kindora_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class MemberDto {
    private String fullName;
    private Integer age;
    private String gender;
    private String distance;       // corresponds to max_distance
    private List<LocationDto> locations;
    private List<TimeDto> times;
    private List<String> types;
}
