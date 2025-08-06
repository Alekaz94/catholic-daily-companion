package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.password FROM User u WHERE u.id = :id")
    String findPasswordHashById(UUID id);
}
