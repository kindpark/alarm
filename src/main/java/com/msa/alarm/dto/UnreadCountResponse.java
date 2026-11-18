package com.msa.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 안 읽은 알림 개수 응답 { "count": 3 } (API_SPEC §13.2)
// 숫자 하나지만 객체로 감싼다. 나중에 필드가 늘어나도 프론트가 깨지지 않는다
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {
    private long count;
}
