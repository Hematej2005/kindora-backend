package com.kindora.kindora_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_type")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MemberType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String type; // e.g. food, clothes, animal
}
