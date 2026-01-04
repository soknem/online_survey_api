    package com.setec.online_survey.domain;

    // ...
    import com.setec.online_survey.config.jpa.Auditable;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotNull;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    import java.util.HashSet;
    import java.util.Set;

    @Setter
    @Getter
    @NoArgsConstructor
    @Table(name = "questions")
    @Entity
    public class Question extends Auditable<String>  {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

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
        @NotNull
        private Integer orderIndex;

        @Column(nullable = false)
        private Boolean isRequired = false;

        // ... relationships with Option and Answer
        @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private Set<Option> options;

        public void addOption(Option option) {
            if (this.options == null) this.options = new HashSet<>();
            this.options.add(option);
            option.setQuestion(this);
        }
    }