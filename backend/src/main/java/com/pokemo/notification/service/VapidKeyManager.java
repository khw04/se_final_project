package com.pokemo.notification.service;

import com.interaso.webpush.VapidKeys;
import com.interaso.webpush.WebPushService;
import com.pokemo.notification.config.VapidProperties;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * VAPID 키 쌍을 관리한다. 설정값(pokemo.push.vapid.*)이 없으면 매 기동 시 임시 키를
 * 생성한다(개발/테스트용). 운영 환경에서는 환경 변수로 영구적인 키를 지정해야 한다.
 */
@Component
@EnableConfigurationProperties(VapidProperties.class)
public class VapidKeyManager {

  private static final Logger log = LoggerFactory.getLogger(VapidKeyManager.class);

  private final VapidKeys vapidKeys;
  private final WebPushService webPushService;

  public VapidKeyManager(VapidProperties properties) {
    if (properties.configured()) {
      this.vapidKeys = VapidKeys.Factory.create(properties.publicKey(), properties.privateKey());
    } else {
      log.warn("VAPID 키가 설정되지 않아 임시 키를 생성합니다. 서버 재시작 시 기존 푸시 구독은 무효화됩니다.");
      this.vapidKeys = VapidKeys.Factory.generate();
    }
    this.webPushService = new WebPushService(properties.subject(), vapidKeys);
  }

  public String publicKey() {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(vapidKeys.getApplicationServerKey());
  }

  public WebPushService webPushService() {
    return webPushService;
  }
}
