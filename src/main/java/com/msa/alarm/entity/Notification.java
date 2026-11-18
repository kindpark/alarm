package com.msa.alarm.entity;

import com.google.errorprone.annotations.InlineMeValidationDisabled;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name="notification_db")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recipientId;
    private String type;
    private Long actorId;
    private Long postId;
    @Column(name="event_id", unique=true, nullable = false)
    private String eventId;
    private Boolean isRead;
    private Instant createdAt;

    public void markRead(){
        this.isRead = true;
    }

    public Notification(
            Long recipientId,
            String type,
            Long actorId,
            Long postId,
            String eventId,
            Instant createdAt
    ){
        this.recipientId = recipientId;
        this.type = type;
        this.actorId = actorId;
        this.postId = postId;
        this.eventId = eventId;
        this.createdAt = createdAt;
    }
}
