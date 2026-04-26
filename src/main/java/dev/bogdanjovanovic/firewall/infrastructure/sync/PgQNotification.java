package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;

@Slf4j
public class PgQNotification implements Runnable {

  private final PgQListener pgQListener;
  private final RuleEvaluator ruleEvaluator;

  public PgQNotification(final PgQListener pgQListener, final RuleEvaluator ruleEvaluator) {
    this.pgQListener = pgQListener;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  public void run() {
    try {
      final var pgConn = pgQListener.getConnection().unwrap(PGConnection.class);

      final var notifications = pgConn.getNotifications();

      if (notifications.length > 0) {
        log.info("Received new PgQ notification, requesting rule rebuild");
        ruleEvaluator.requestRuleRebuild();
      }
    } catch (Exception ex) {
      log.error("PgQ notification failed", ex);
    }
  }

}
