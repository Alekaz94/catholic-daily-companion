/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.dto.AdminUserListDto;
import com.alexandros.dailycompanion.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Page<User> findAllByEmailContainingIgnoreCase(String email, Pageable pageable);

    @Query("SELECT u.password FROM User u WHERE u.id = :id")
    String findPasswordHashById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query(value = """
        INSERT INTO users (id, email, first_name, role, created_at, updated_at)
        VALUES (:id, :email, :firstName, :role, :createdAt, :updatedAt)
        ON CONFLICT (email) DO NOTHING
        """, nativeQuery = true)
    void insertIfNotExists(@Param("id") UUID id,
                           @Param("email") String email,
                           @Param("firstName") String firstName,
                           @Param("role") String role,
                           @Param("createdAt") LocalDate createdAt,
                           @Param("updatedAt") LocalDate updatedAt);

    @Query(value = "SELECT new com.alexandros.dailycompanion.dto.AdminUserListDto(u.id, u.email, u.role) FROM User u")
    Page<AdminUserListDto> findAllUsersForAdmin(Pageable pageable);

    @Query("SELECT new com.alexandros.dailycompanion.dto.AdminUserListDto(u.id, u.email, u.role) " +
            "FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<AdminUserListDto> searchUsersForAdmin(@Param("email") String email, Pageable pageable);
}
