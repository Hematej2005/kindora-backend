package com.kindora.kindora_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_time")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MemberTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "time_from", nullable = false)
    private String fromTime; // stored as TIME in DB, but JSON/text here

    @Column(name = "time_to", nullable = false)
    private String toTime;
}
