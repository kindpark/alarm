package com.msa.alarm;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

// 커서 페이징 기법은 기존 피드, 팔로워 불러오기 참고
}
