package com.msa.alarm.service;

import com.msa.alarm.dto.EventMessage;
import com.msa.alarm.dto.NotificationResponseDto;
import com.msa.alarm.entity.Notification;
import com.msa.alarm.repository.NotificationRepository;
import com.msa.alarm.sse.SseHub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final SseHub hub;

    @RabbitListener(queues = "${rabbitmq.queue.name}") // 실제 큐 이름 또는 프로퍼티 지정 필요
    public void handleEvent(EventMessage evt) {
        if (!NotificationService.NOTIFIABLE_TYPES.contains(evt.getType()) || evt.getRecipientId() == null) {
            return;
        }
        if(Objects.equals(evt.getActorId(), evt.getRecipientId())){
            return;
        }
        if (evt.getEventId() == null || evt.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId가 없는 이벤트");
        }

        Long recipientId = evt.getRecipientId();
        Instant createdAt = (evt.getOccurredAt() != null ?
                evt.getOccurredAt() : OffsetDateTime.now(ZoneOffset.UTC)).toInstant();

        Notification saved;
        try {
            saved = notificationRepository.saveAndFlush(
                    new Notification(recipientId, evt.getType(), evt.getActorId(), evt.getPostId(), evt.getEventId(), createdAt)
            );
        } catch (DataIntegrityViolationException e) {
            log.info("중복 이벤트 무시 eventId={}", evt.getEventId());
            return; // 중복 시 아래 SSE 발송을 타지 않도록 반드시 return
        }

        Integer delivered = hub.sendToUser(recipientId, "notification", NotificationResponseDto.from(saved));
        log.info("알림 생성 id={} type={} recipient={} sse={}", saved.getId(), saved.getType(), recipientId, delivered);
    }
}
