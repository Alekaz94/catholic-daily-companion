package com.alexandros.dailycompanion.repository;

import com.alexandros.dailycompanion.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

}
