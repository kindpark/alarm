package com.msa.alarm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationPageResponse {
    private List<NotificationResponseDto> notifications;
    private Long nextCursor;
}
