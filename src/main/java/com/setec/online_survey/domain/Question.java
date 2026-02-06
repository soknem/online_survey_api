    package com.setec.online_survey.domain;

    // ...
    import com.setec.online_survey.config.jpa.Auditable;
    import com.setec.online_survey.util.StringUuidConverter;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotNull;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import java.util.HashSet;
    import java.util.Set;
    import java.util.UUID;

    @Setter @Getter @NoArgsConstructor
    @Entity
    @Table(name = "questions", indexes = {
            @Index(name = "idx_question_uuid", columnList = "uuid", unique = true),
            @Index(name = "idx_question_survey_order", columnList = "survey_id, orderIndex")
    })
    public class Question extends Auditable<String> {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Convert(converter = StringUuidConverter.class)
        @Column(name = "uuid", unique = true, nullable = false, columnDefinition = "RAW(16)")
        private String uuid;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "survey_id", nullable = false)
        private Survey survey;

        @Column(length = 4000, nullable = false)
        private String questionText;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 50)
        private QuestionType questionType = QuestionType.MULTIPLE_CHOICE;

        @Column(nullable = false)
        private Integer orderIndex;

        private Boolean isRequired = false;

        @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("orderIndex ASC")
        private Set<Option> options;

        @PrePersist
        public void ensureUuid() {
            if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        }
    }