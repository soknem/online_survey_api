package com.setec.online_survey.domain;

import com.setec.online_survey.config.jpa.Auditable;
import com.setec.online_survey.util.StringUuidConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Setter @Getter @NoArgsConstructor
@Entity
@Table(name = "surveys", indexes = {
        @Index(name = "idx_survey_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_survey_folder", columnList = "folder_id"),
        @Index(name = "idx_survey_url", columnList = "surveyUrl", unique = true)
})
public class Survey extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = StringUuidConverter.class)
    @Column(name = "uuid", unique = true, nullable = false, columnDefinition = "RAW(16)")
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(unique = true, length = 255)
    private String surveyUrl;
    private String qrCodeUrl;
    private LocalDateTime startDate;
    private LocalDateTime closeDate;

    @Column(precision = 1)
    private Boolean isPublic = false;

    @Column(precision = 1)
    private Boolean isClosed = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SurveyType surveyType = SurveyType.HYBRID;

    private Integer maxResponses;
    private String image;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResponseSession> responseSessions;

    @OneToOne(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private SurveyFolder folder;

    @PrePersist
    public void ensureUuid() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }
}