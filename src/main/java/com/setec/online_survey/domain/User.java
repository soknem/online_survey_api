package com.setec.online_survey.domain;

import com.setec.online_survey.config.jpa.Auditable;
import com.setec.online_survey.domain.UserRole; // Import the new Enum
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Table(name = "users")
@Entity
public class User extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   // @Column(nullable = false, unique = true, length = 100)
    @Size(max = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    @Email
    @Size(max = 255)
    private String email;

    private LocalDate dateOfBirth;

    private String uuid;

    @Column(length = 100)
    private String firstName;

    private String userProfile;

    @Column(length = 100)
    private String lastName;

    private Boolean emailVerified;

    private LocalDate tokenDate =null;

    private String provider;

    private Boolean isAccountNonExpired = true;
    private Boolean isAccountNonLocked = true;
    private Boolean isCredentialsNonExpired = true;
    private Boolean isDeleted = false;

    // --- Role is now an Enum ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role = UserRole.ROLE_USER; // Default role
    // ---------------------------

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Survey> createdSurveys;

    @OneToMany(mappedBy = "respondent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResponseSession> submittedResponses;
}