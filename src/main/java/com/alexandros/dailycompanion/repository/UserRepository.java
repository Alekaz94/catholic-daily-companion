package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Page<User> findAllByEmailContainingIgnoreCase(String email, Pageable pageable);

    @Query("SELECT u.password FROM User u WHERE u.id = :id")
    String findPasswordHashById(UUID id);

}
