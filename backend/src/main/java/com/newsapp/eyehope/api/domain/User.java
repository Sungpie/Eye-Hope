package com.newsapp.eyehope.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column
    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "password_hash")
    private String passwordHash;
}
