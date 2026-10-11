package com.Sojval.Auth.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        indexes = {
                @Index(name="refresh_tokens_jti_idx",columnList = "jti",unique = true),
                @Index(name="refresh_tokens_user_id_idx",columnList = "user_id")
        }
)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String jti;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    private User user;
    private Instant created ;
    private Instant expires ;
    private boolean revoked;
    private String replacedByToken;

    public RefreshToken(
            String jti,
            User user,
            Instant created,
            Instant expires,
            boolean revoked
    ) {
        this.jti = jti;
        this.user = user;
        this.created = created;
        this.expires = expires;
        this.revoked = revoked;
    }
}
