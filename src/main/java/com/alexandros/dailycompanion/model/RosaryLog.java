package com.alexandros.dailycompanion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rosary_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "date"})
})
@Entity
public class RosaryLog {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate date;
    private boolean completed;

    public RosaryLog(User user, LocalDate date, boolean completed) {
        this.user = user;
        this.date = date;
        this.completed = completed;
    }
}
