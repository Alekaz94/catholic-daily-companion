package com.alexandros.dailycompanion.repository;

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
import java.util.List;
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
}
