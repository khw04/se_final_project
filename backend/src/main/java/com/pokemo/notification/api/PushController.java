package com.pokemo.notification.api;

import com.pokemo.common.CurrentUserProvider;
import com.pokemo.notification.service.PushNotificationService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push")
public class PushController {

  private final PushNotificationService pushNotificationService;
  private final CurrentUserProvider currentUserProvider;

  public PushController(PushNotificationService pushNotificationService, CurrentUserProvider currentUserProvider) {
    this.pushNotificationService = pushNotificationService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping("/public-key")
  public VapidPublicKeyResponse publicKey() {
    return new VapidPublicKeyResponse(pushNotificationService.vapidPublicKey());
  }

  @PostMapping("/subscribe")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void subscribe(Principal principal, @Valid @RequestBody PushSubscriptionRequest request) {
    long userId = currentUserProvider.userId(principal);
    pushNotificationService.subscribe(userId, request.endpoint(), request.keys().p256dh(), request.keys().auth());
  }

  @DeleteMapping("/subscribe")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unsubscribe(@Valid @RequestBody PushUnsubscribeRequest request) {
    pushNotificationService.unsubscribe(request.endpoint());
  }
}
