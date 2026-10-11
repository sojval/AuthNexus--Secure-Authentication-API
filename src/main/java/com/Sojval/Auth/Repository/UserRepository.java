package com.Sojval.Auth.Repository;

import com.Sojval.Auth.Dtos.UserDto;
import com.Sojval.Auth.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    User findByEmail(String email);

    UserDto getUserById(Long id);

    void findById(UUID userUUID);
}
