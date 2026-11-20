package com.msa.alarm.service;

import com.msa.alarm.config.RabbitMQConfig;
import com.msa.alarm.dto.NotificationPageResponse;
import com.msa.alarm.dto.NotificationResponseDto;
import com.msa.alarm.dto.UnreadCountResponse;
import com.msa.alarm.entity.Notification;
import com.msa.alarm.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    /** 알림을 만드는 이벤트 (API_SPEC §9.1 큐 바인딩과 같음) */
    public static final List<String> NOTIFIABLE_TYPES =
            RabbitMQConfig.ROUTING_KEYS;
    private final NotificationRepository notificationRepository;
    // 알림 페이지 띄우기
    @Transactional(readOnly = true)
    public NotificationPageResponse getPage(
            Long userId,
            Long cursor,
             int size
    ){
        int pageSize = Math.min(Math.max(size, 1), 10);
        Limit limit = Limit.of(pageSize + 1);
        // 이번 페이지의 ID를 꺼낸다.
        List<Notification> fetchedIds = (cursor==null)
                ? notificationRepository.findByRecipientIdOrderByIdDesc(userId, limit)
                : notificationRepository.findByRecipientIdAndIdLessThanOrderByIdDesc(userId, cursor, limit);
        // hasNext 판별 및 초과분 제거
        boolean hasNext = fetchedIds.size() > pageSize;
        List<Notification> targetIds = hasNext ? fetchedIds.subList(0, pageSize) : fetchedIds;
        // 타겟 아이디에 알림이 없다면 빈걸 리턴
        if(targetIds.isEmpty()){
            return new NotificationPageResponse(List.of(),null);
        }
        // dto 변환
        List<NotificationResponseDto> items = targetIds.stream()
                .map(NotificationResponseDto::from)
                .toList();
        // nextCursor 계산 및 반환
        Long nextCursor = hasNext ? targetIds.get(targetIds.size() - 1).getId() : null;

        return new NotificationPageResponse(items, nextCursor);
    }

    // 읽지않은 알림 페이지 비동기 실행
    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread(Long userId){
        return new UnreadCountResponse(notificationRepository.countByRecipientIdAndIsReadFalse(userId));
    }

    // 읽은걸 체크하는걸 비동기 실행
    @Transactional
    public void markAllRead(Long userId){
        notificationRepository.markAllRead(userId);
    }

    // 읽은걸 체크
    @Transactional
    public boolean markRead(Long userId, Long notificationId){
        return notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .map(n->{
                    n.markRead();
                    return true;
                })
                .orElse(false);
    }
    @Transactional
    public void deletedUsersAlarm(Long userId){
        notificationRepository.deletedAlarm(userId);
    }
}
