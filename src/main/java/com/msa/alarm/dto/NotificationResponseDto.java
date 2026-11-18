package com.msa.alarm.dto;

import com.msa.alarm.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

// 알림 한 개 응답 (API_SPEC §13.2). 목록 API 와 SSE 가 같은 모양을 쓴다.
// { "id", "type", "actorId", "postId", "createdAt", "read" }
// 문구("OO님이 답글을 남겼습니다")는 넣지 않는다. 닉네임은 바뀔 수 있으므로 프론트가 type 과 actorId 로 그때그때 만든다.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long id;
    private String type;
    private Long actorId;
    // 팔로우 알림은 글이 없으므로 null
    private Long postId;
    // Instant 는 "2026-11-17T09:30:00Z" 처럼 UTC 로 나간다 (스펙 형식과 같음)
    private Instant createdAt;
    // 스펙의 JSON 키는 "read". boolean read 에 Lombok 은 isRead() 를 만들고, Jackson 은 이를 "read" 로 내보낸다.
    // (엔티티처럼 Boolean isRead 로 두면 getIsRead() 가 생겨 키가 "isRead" 가 된다)
    private boolean read;

    public static NotificationResponseDto from(Notification entity) {
        return NotificationResponseDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .actorId(entity.getActorId())
                .postId(entity.getPostId())
                .createdAt(entity.getCreatedAt())
                // 엔티티의 isRead 는 Boolean 이라 null 일 수 있다. null 이면 안 읽은 것으로 본다
                .read(Boolean.TRUE.equals(entity.getIsRead()))
                .build();
    }
}
