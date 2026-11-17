package com.msa.alarm.service;

import com.msa.alarm.common.BusinessException;
import com.msa.alarm.common.ErrorCode;
import com.msa.alarm.dto.EventMessage;
import com.msa.alarm.dto.NotificationPageResponse;
import com.msa.alarm.dto.NotificationResponseDto;
import com.msa.alarm.dto.UnreadCountResponse;
import com.msa.alarm.entity.Notification;
import com.msa.alarm.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    /** 알림을 만드는 이벤트 (API_SPEC §9.1 큐 바인딩과 같음) */
    public static final List<String> NOTIFIABLE_TYPES =
            List.of("follow.created", "reply.created", "like.created", "repost.created");
    private final NotificationRepository notificationRepository;

    // 알림 페이지 띄우기
    @Transactional(readOnly = true)
    public NotificationPageResponse getPage(
            Long userId,
            Long cursor,
             int size
    ){
        int pageSize = Math.min(Math.max(size, 1), 10);
        // 이번 페이지의 ID를 꺼낸다.
        List<Long> fetchedIds = notificationRepository.(userId, cursor);
        // hasNext 판별 및 초과분 제거
        boolean hasNext = fetchedIds.size() > pageSize;
        List<Long> targetIds = hasNext ? fetchedIds.subList(0, pageSize) : fetchedIds;
        // 타겟 아이디에 알림이 없다면 빈걸 리턴
        if(targetIds.isEmpty()){
            return new NotificationPageResponse(List.of(),null);
        }

        // 알림을 가져온 뒤 캐시에서 꺼낸 순서대로 재정렬
        Map<Long, Notification> postMap = notificationRepository.findAllById(targetIds).stream()
                .collect(Collectors.toMap(Notification::getId, Function.identity()));

        // 삭제된 알림은 postMap에 없으니 자연스럽게 필터링
        List<Notification> orderedPosts = targetIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();

        // 좋아요 여부 일괄 조회 (N+1 방지)
        Set<Long> likedPostIds = notificationRepository.find(targetIds, userId);

        // dto 변환
        List<NotificationResponseDto> items = orderedPosts.stream()
                .map(notification -> NotificationResponseDto.from(notification))
                .toList();
        // nextCursor 계산 및 반환
        Long nextCursor = hasNext ? targetIds.get(targetIds.size() - 1) : null;

        return new NotificationPageResponse(items, nextCursor);
    }

    // 알림 발송 관련
    @RabbitListener
    @Transactional
    @Async
    public boolean handleEvent(EventMessage evt){

    }
    // 읽지않은 알림 페이지 비동기 실행
    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread(Long userId){
        return new UnreadCountResponse(notificationRepository.countByRecipientIdAndReadFalse(userId));
    }

    // 읽은걸 체크하는걸 비동기 실행
    @Transactional
    public void markAllRead(Long userId){
        notificationRepository.markAllRead(userId);
    }

    // 읽은걸 체크
    public boolean markRead(Long userId, Long notificationId){
        return notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .map(n->{
                    n.markRead();
                    return true;
                })
                .orElse(false);
    }
}
