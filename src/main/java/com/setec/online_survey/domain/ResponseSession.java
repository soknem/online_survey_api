package com.setec.online_survey.domain;

import com.setec.online_survey.config.jpa.Auditable;
import com.setec.online_survey.util.StringUuidConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "response_sessions", indexes = {
        @Index(name = "idx_session_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_session_survey", columnList = "survey_id")
})
public class ResponseSession extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = StringUuidConverter.class)
    @Column(name = "uuid", unique = true, nullable = false, columnDefinition = "RAW(16)")
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // respondent_id is nullable for anonymous submissions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id")
    private User respondent;

    @Column(name = "ip_address", length = 45) // Used for anonymous one-time check
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime submitTime;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answers;

    // --- Restored Security/Tracking Fields ---
    private String fingerprint;
    private String browserUuid;
    private String userAgent;

    @PrePersist
    public void ensureUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }
}