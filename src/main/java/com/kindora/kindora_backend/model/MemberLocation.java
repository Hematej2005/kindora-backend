package com.kindora.kindora_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_location")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MemberLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String pincode;
    private String state;
    private String street;

    @Column(nullable = false)
    private String district;

    private String landmark;

    // store as DECIMAL in DB; Double here
    private Double lat;
    private Double lng;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;
}
