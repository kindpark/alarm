package com.msa.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 알림 목록 응답 { "notifications": [...], "nextCursor": 30 } (API_SPEC §13.2)
// nextCursor 를 NotificationResponseDto 에 넣지 않는 이유는 PostPageResponseDto 와 같다.
// SSE 로 알림 하나를 보낼 때 의미 없는 nextCursor 가 붙어 나가기 때문. 역할이 다르면 클래스를 나눈다.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageResponse {
    private List<NotificationResponseDto> notifications;
    // null 이면 마지막 페이지
    private Long nextCursor;
}
