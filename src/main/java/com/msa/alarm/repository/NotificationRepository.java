package com.msa.alarm.repository;

import com.msa.alarm.entity.Notification;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

// 커서 페이징 기법은 기존 피드, 팔로워 불러오기 참고
// 첫 페이지 조회
    List<Notification> findByRecipientIdOrderByIdDesc(Long recipientId, Limit limit);

    // ①-2 다음 페이지 조회 (cursor != null 일 때)
    List<Notification> findByRecipientIdAndIdLessThanOrderByIdDesc(Long recipientId, Long cursor, Limit limit);

    Long countByRecipientIdAndIsReadFalse(Long recipientId);

    /* 남의 알림이면 비어 있다 */
    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    // 한번의 업데이트로 모두 읽음 처리함
    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.isRead = true" +
            " where n.recipientId = :recipientId" +
            " and n.isRead = false")
    int markAllRead(@Param("recipientId") Long recipientId);
}
