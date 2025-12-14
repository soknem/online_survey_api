package com.setec.online_survey.features.user;

import com.setec.online_survey.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByEmailAndEmailVerifiedFalseAndAccountNonLockedTrue(String email);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndEmailVerifiedTrue(String email);

    Optional<User> findByEmail(String Email);
}
