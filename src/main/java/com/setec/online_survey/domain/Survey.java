package com.setec.online_survey.domain;

import com.setec.online_survey.config.jpa.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Table(name = "surveys")
@Entity
public class Survey extends Auditable<String>  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 255)
    @Size(max = 255)
    private String title;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(nullable = false, unique = true, length = 255)
    private String surveyUrl;

    private String qrCodeUrl;


    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime closeDate;

    private Boolean isPublic =false;

    private Boolean isClosed =false;

    private  SurveyType surveyType;

    private Integer maxResponses;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResponseSession> responseSessions;

    @OneToOne(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Report report;
}