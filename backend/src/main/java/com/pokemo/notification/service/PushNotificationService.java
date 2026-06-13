package com.pokemo.notification.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interaso.webpush.WebPush;
import com.pokemo.calendar.domain.CalendarEvent;
import com.pokemo.calendar.repository.CalendarEventRepository;
import com.pokemo.notification.domain.PushNotification;
import com.pokemo.notification.domain.PushSubscription;
import com.pokemo.notification.repository.PushNotificationRepository;
import com.pokemo.notification.repository.PushSubscriptionRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushNotificationService {

  private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

  private static final int TTL_SECONDS = (int) Duration.ofDays(28).toSeconds();

  private static final Map<String, Duration> REMINDER_LEAD_TIMES = Map.of(
      "15m", Duration.ofMinutes(15),
      "1h", Duration.ofHours(1),
      "1d", Duration.ofDays(1)
  );

  private final PushSubscriptionRepository subscriptionRepository;
  private final PushNotificationRepository notificationRepository;
  private final CalendarEventRepository calendarEventRepository;
  private final VapidKeyManager vapidKeyManager;
  private final ObjectMapper objectMapper;

  public PushNotificationService(
      PushSubscriptionRepository subscriptionRepository,
      PushNotificationRepository notificationRepository,
      CalendarEventRepository calendarEventRepository,
      VapidKeyManager vapidKeyManager,
      ObjectMapper objectMapper
  ) {
    this.subscriptionRepository = subscriptionRepository;
    this.notificationRepository = notificationRepository;
    this.calendarEventRepository = calendarEventRepository;
    this.vapidKeyManager = vapidKeyManager;
    // Safari Web Push가 페이로드의 비-ASCII 문자를 UTF-8로 디코딩하지 못해 한글이 깨지는 문제를 피하기 위해
    // 한글 등 비-ASCII 문자를 \\uXXXX 형태로 escape한다.
    this.objectMapper = objectMapper.copy().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
  }

  public String vapidPublicKey() {
    return vapidKeyManager.publicKey();
  }

  @Transactional
  public void subscribe(long userId, String endpoint, String p256dh, String auth) {
    if (subscriptionRepository.existsByEndpoint(endpoint)) {
      return;
    }
    subscriptionRepository.save(new PushSubscription(userId, endpoint, p256dh, auth));
  }

  @Transactional
  public void unsubscribe(String endpoint) {
    subscriptionRepository.deleteByEndpoint(endpoint);
  }

  @Scheduled(fixedRate = 60_000, initialDelay = 60_000)
  @Transactional
  public void sendDueReminders() {
    OffsetDateTime now = OffsetDateTime.now();
    REMINDER_LEAD_TIMES.forEach((reminder, leadTime) -> {
      OffsetDateTime from = now.plus(leadTime);
      OffsetDateTime to = from.plusMinutes(1);
      for (CalendarEvent event : calendarEventRepository.findByReminderAndStartAtBetween(reminder, from, to)) {
        if (notificationRepository.existsByUserIdAndCalendarEventId(event.userId(), event.id())) {
          continue;
        }
        sendEventReminder(event);
      }
    });
  }

  private void sendEventReminder(CalendarEvent event) {
    String title = "일정 알림";
    String body = event.title() + " 일정이 곧 시작됩니다.";
    sendToUser(event.userId(), title, body);
    notificationRepository.save(new PushNotification(event.userId(), event.id(), title, body));
  }

  private void sendToUser(long userId, String title, String body) {
    List<PushSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
    if (subscriptions.isEmpty()) {
      return;
    }

    String payload = toPayload(title, body);
    for (PushSubscription subscription : subscriptions) {
      try {
        WebPush.SubscriptionState state = vapidKeyManager.webPushService().send(
            payload,
            subscription.endpoint(),
            subscription.p256dh(),
            subscription.auth(),
            TTL_SECONDS,
            null,
            WebPush.Urgency.Normal
        );
        if (state == WebPush.SubscriptionState.EXPIRED) {
          subscriptionRepository.deleteByEndpoint(subscription.endpoint());
        }
      } catch (Exception e) {
        log.warn("푸시 발송 실패: endpoint={}, error={}", subscription.endpoint(), e.getMessage());
      }
    }
  }

  private String toPayload(String title, String body) {
    try {
      return objectMapper.writeValueAsString(Map.of("title", title, "body", body));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("푸시 알림 페이로드 생성에 실패했습니다", e);
    }
  }
}
