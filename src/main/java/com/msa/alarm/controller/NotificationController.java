package com.msa.alarm.controller;

import com.msa.alarm.common.BusinessException;
import com.msa.alarm.common.ErrorCode;
import com.msa.alarm.dto.NotificationPageResponse;
import com.msa.alarm.dto.UnreadCountResponse;
import com.msa.alarm.service.NotificationService;
import com.msa.alarm.sse.SseHub;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** /api/v1/notifications (API_SPEC §13.2). 모든 메서드에 로그인 필요 (SecurityConfig) */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;
    private final SseHub hub;

    // GET /api/v1/notifications?cursor=&size=
    @GetMapping
    public NotificationPageResponse getPage(@AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return service.getPage(userId, cursor, size);
    }

    // GET /api/v1/notifications/unread-count
    @GetMapping("/unread-count")
    public UnreadCountResponse countUnread(@AuthenticationPrincipal Long userId) {
        return service.countUnread(userId);
    }

    // POST /api/v1/notifications/read-all → 204
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal Long userId) {
        service.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/notifications/{id}/read → 204, 남의 알림이면 404
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal Long userId, @PathVariable long id) {
        if (!service.markRead(userId, id)) {
            throw new BusinessException(ErrorCode.UNKNOWN_NOTIFICATION);
        }
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/notifications/subscribe — SSE (API_SPEC §13.3)
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long userId, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no"); // nginx 버퍼링 방지
        return hub.subscribe(userId);
    }
}
