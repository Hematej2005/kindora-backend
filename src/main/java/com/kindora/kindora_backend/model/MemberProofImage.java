package com.kindora.kindora_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "member_proof_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberProofImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donation_id", nullable = false)
    private Long donationId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt = Instant.now();
}
