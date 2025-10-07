package com.alexandros.dailycompanion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue
    private UUID id;
    private String category;
    @Column(length = 2000)
    private String message;
    private String email;
    private LocalDateTime submittedAt;

    private boolean isFixed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
