package com.pokemo.notification.repository;

import com.pokemo.notification.domain.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {

  boolean existsByUserIdAndCalendarEventId(Long userId, Long calendarEventId);
}
