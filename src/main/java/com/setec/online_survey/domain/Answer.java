package com.setec.online_survey.domain;

// ...
import com.setec.online_survey.config.jpa.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
@Setter @Getter @NoArgsConstructor
@Entity
@Table(name = "answers", indexes = {
        @Index(name = "idx_answer_session", columnList = "session_id"),
        @Index(name = "idx_answer_question", columnList = "question_id")
})
public class Answer extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ResponseSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(length = 4000)
    private String answerText;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "answer_selected_options",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private Set<Option> options;
}