package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;

@Slf4j
public class PgQListener implements Runnable {

  private final PgQConnection pgQConnection;
  private final RuleEvaluator ruleEvaluator;

  public PgQListener(final PgQConnection pgQConnection, final RuleEvaluator ruleEvaluator) {
    this.pgQConnection = pgQConnection;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  public void run() {
    try {
      final var pgConn = pgQConnection.getConnection().unwrap(PGConnection.class);

      final var notifications = pgConn.getNotifications();

      if (notifications.length > 0) {
        log.info("Received new PgQ notification, requesting rule rebuild");
        ruleEvaluator.requestRuleRebuild();
      }
    } catch (Exception ex) {
      log.error("PgQ listener failed", ex);
    }
  }

}
