package com.downloadvideo.adapters.out.persistence.entity;

import com.downloadvideo.domain.model.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class UserEntity {
    @Id
    @Column(name="user_id")
    private Long id;
    @Column(name="user_username")
    private String username;

    public UserEntity(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public UserEntity(Long id) {
        this.id = id;
    }

    @Column(name="user_register",
            insertable = false,
            updatable = false)
    private OffsetDateTime register;
    @Column(name="user_count_download")
    private int user_count_download;
    @Column(name="user_role")
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Column(name="user_blocked")
    private boolean blocked;

}
