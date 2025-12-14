package com.kindora.kindora_backend.dto;

public class DonationUpdateRequest {
    private Long id;
    private String status;
    private String proofBase64;

    public DonationUpdateRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProofBase64() { return proofBase64; }
    public void setProofBase64(String proofBase64) { this.proofBase64 = proofBase64; }
}
