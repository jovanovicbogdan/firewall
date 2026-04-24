package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;

@Slf4j
public class PgQListener implements Runnable {

  private static final String CHANNEL = "rule_events";

  private final DataSource dataSource;
  private final RuleEvaluator ruleEvaluator;

  public PgQListener(final DataSource dataSource, final RuleEvaluator ruleEvaluator) {
    this.dataSource = dataSource;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  public void run() {
    try (final var conn = dataSource.getConnection();
        final var stmt = conn.createStatement()) {
      stmt.execute(String.format("LISTEN %s", CHANNEL));
      final var pgConn = conn.unwrap(PGConnection.class);

      final var notifications = pgConn.getNotifications();

      if (notifications.length > 0) {
        log.info("Received new PgQ notification, requesting rule rebuild...");
        ruleEvaluator.requestRuleRebuild();
      }
    } catch (Exception ex) {
      log.error("PgQ listener failed", ex);
    }
  }

}
