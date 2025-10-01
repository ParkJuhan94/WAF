package dev.waf.console.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    public User(String email, String name, String profileImage, String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.role = UserRole.FREE_USER;
        this.provider = provider;
        this.providerId = providerId;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateProfile(String name, String profileImage) {
        this.name = name;
        this.profileImage = profileImage;
    }
}