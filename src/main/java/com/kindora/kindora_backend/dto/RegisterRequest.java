package com.kindora.kindora_backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;      // required
    private String email;         // required
    private String password;      // required
    private String role;          // optional, default DONOR
    private String phoneNumber;   // optional
}
