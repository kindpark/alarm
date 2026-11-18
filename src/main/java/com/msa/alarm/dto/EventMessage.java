package com.msa.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

// RabbitMQ 로 들어오는 이벤트 (API_SPEC §9.2). 모든 이벤트가 같은 봉투 모양을 쓴다.
// User-Service 의 EventMessage 와 필드 이름이 같아야 JSON 이 그대로 들어온다.
// 알림에 필요 없는 필드는 null 로 온다 (예: 팔로우는 postId 가 null)
@Getter
// Jackson 이 JSON 을 객체로 바꿀 때 빈 객체를 먼저 만들고 필드를 채우므로 기본 생성자가 필요하다
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {
    // 이벤트마다 새로 만드는 UUID 문자열. 중복 제거용
    private String eventId;
    // follow.created / reply.created / like.created / repost.created ...
    private String type;
    // "2026-11-17T09:30:00Z" 처럼 UTC 로 온다
    private OffsetDateTime occurredAt;
    // 행동한 사람
    private Long actorId;
    // 알림 받을 사람. 피드 전용 이벤트는 null
    private Long recipientId;
    private Long postId;
    // 답글일 때 부모 글
    private Long parentId;
}
