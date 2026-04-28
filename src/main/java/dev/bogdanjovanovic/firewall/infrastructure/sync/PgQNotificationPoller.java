package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgQNotificationPoller implements Runnable {

  private final PgQListener pgQListener;
  private final RuleEvaluator ruleEvaluator;

  public PgQNotificationPoller(final PgQListener pgQListener, final RuleEvaluator ruleEvaluator) {
    this.pgQListener = pgQListener;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  public void run() {
    try {
      final var notifications = pgQListener.getPgNotifications();

      if (notifications.length > 0) {
        log.info("Received new PgQ notification(s), requesting rule rebuild");
        ruleEvaluator.requestRuleRebuild();
      }
    } catch (Exception ex) {
      log.error("PgQ notification failed", ex);
    }
  }

}
