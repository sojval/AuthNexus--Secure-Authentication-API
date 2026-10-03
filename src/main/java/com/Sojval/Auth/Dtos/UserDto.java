package com.Sojval.Auth.Dtos;

import com.Sojval.Auth.Entity.Provider;
import com.Sojval.Auth.Entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String image;
    private boolean enabled=true;
    private Instant created = Instant.now();
    private Instant updated = Instant.now();

    private Provider provider =  Provider.LOCAL;

    private Set<RoleDto> roles = new HashSet<>();
}
