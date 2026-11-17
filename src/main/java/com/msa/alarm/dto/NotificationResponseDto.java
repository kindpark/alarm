package com.msa.alarm.dto;

//읽었냐? 안읽었냐에 역점?

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class NotificationResponseDto {
    private boolean isRead;
}
