package com.downloadvideo.adapters.out.persistence.repository;

import com.downloadvideo.adapters.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    @Query(value = """
        select user_id
        from users
        where user_id = :id
        and user_blocked = false
    """,nativeQuery = true)
    Long findUserAndCheckBlocked(
            @Param("id") Long id
    );
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE users
            SET user_count_download = user_count_download + 1
            WHERE user_id = :id
            """,nativeQuery = true)
    void addCountDownloads(@Param("id")Long id);

    @Query(value= """
            SELECT user_id FROM users
            WHERE user_role='ADMIN'
            """, nativeQuery = true)
    List<Long> getIdAllAdmins();

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE users
            SET user_blocked = :block
            WHERE user_id = :id
            """,nativeQuery = true)
    void blockUserById(@Param("id")Long id, @Param("block")boolean block);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE users
            SET user_blocked = :block
            WHERE user_username = :username
            """,nativeQuery = true)
    void blockUserByUsername(@Param("username")String username, @Param("block")boolean block);

    @Query(value = """
            SELECT * FROM users
            WHERE user_username=:username
            """,nativeQuery = true)
    UserEntity getUser(@Param("username")String username);

    @Query(value = """
            SELECT COUNT(user_id) FROM users
            """,nativeQuery = true)
    Long getCountUser();

    @Query(value = """
            SELECT 1 FROM users
            WHERE user_id=:id
            AND user_role='ADMIN'
            """,nativeQuery = true)
    Integer isAdmin(@Param("id") Long id);


}
