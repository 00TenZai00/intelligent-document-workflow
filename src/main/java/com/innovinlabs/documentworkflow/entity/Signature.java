package com.innovinlabs.documentworkflow.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "signatures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signer_id", nullable = false)
    private User signer;

    @Column(name = "document_hash", nullable = false, length = 128)
    private String documentHash;

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt = Instant.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}

