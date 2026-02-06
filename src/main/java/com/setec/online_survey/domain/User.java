package com.setec.online_survey.domain;

import com.setec.online_survey.config.jpa.Auditable;
import com.setec.online_survey.util.StringUuidConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_users_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Entity
public class User extends Auditable<String> {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Size(max = 100)
 private String password;

 @Column(nullable = false, unique = true, length = 255)
 @Email
 @Size(max = 255)
 private String email;

 private LocalDate dateOfBirth;

 @Convert(converter = StringUuidConverter.class)
 @Column(name = "uuid", unique = true, nullable = false, columnDefinition = "RAW(16)")
 private String uuid;

 @Column(length = 100)
 private String firstName;

 @Column(length = 100)
 private String lastName;

 private String userProfile;

 private Boolean emailVerified = false;

 private LocalDate tokenDate = null;

 private String provider;

 // --- Account Status Flags ---
 private Boolean isAccountNonExpired = true;
 private Boolean isAccountNonLocked = true;
 private Boolean isCredentialsNonExpired = true;
 private Boolean isDeleted = false;

 // --- Role Enum ---
 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 50)
 private UserRole role = UserRole.ROLE_USER;

 // --- Relationships ---
 @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
 private Set<Survey> createdSurveys;

 @OneToMany(mappedBy = "respondent", cascade = CascadeType.ALL, orphanRemoval = true)
 private Set<ResponseSession> submittedResponses;

 @PrePersist
 public void ensureUuid() {
  if (this.uuid == null) {
   this.uuid = UUID.randomUUID().toString();
  }
 }
}