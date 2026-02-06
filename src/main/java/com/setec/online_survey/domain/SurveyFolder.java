package com.setec.online_survey.domain;

import com.setec.online_survey.util.StringUuidConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter @Getter @NoArgsConstructor
@Entity
@Table(name = "survey_folders", indexes = {
        @Index(name = "idx_folder_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_folder_owner", columnList = "user_id")
})
public class SurveyFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = StringUuidConverter.class)
    @Column(name = "uuid", unique = true, nullable = false, columnDefinition = "RAW(16)")
    private String uuid;

    @Column(nullable = false, length = 100)
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    private FolderType folderType = FolderType.CUSTOM;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY)
    private List<Survey> surveys;

    @PrePersist
    public void ensureUuid() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }
}