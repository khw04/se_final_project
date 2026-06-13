package com.pokemo.notification.repository;

import com.pokemo.notification.domain.PushSubscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

  List<PushSubscription> findByUserId(Long userId);

  boolean existsByEndpoint(String endpoint);

  void deleteByEndpoint(String endpoint);
}
