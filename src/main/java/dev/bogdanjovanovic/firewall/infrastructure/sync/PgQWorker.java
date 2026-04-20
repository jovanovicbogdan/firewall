package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;

@Slf4j
public class PgQWorker implements Runnable {

  private static final String CHANNEL = "rule_events";
  private static final String RULE_CHANGED_EVENT = "rule_changed";

  private final DataSource dataSource;
  private final RuleEvaluator ruleEvaluator;

  public PgQWorker(final DataSource dataSource, final RuleEvaluator ruleEvaluator) {
    this.dataSource = dataSource;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  public void run() {
    try (final var conn = dataSource.getConnection();
        final var stmt = conn.createStatement()) {
      stmt.execute(String.format("LISTEN %s", CHANNEL));
      final var pgConn = conn.unwrap(PGConnection.class);

      while (!Thread.interrupted()) {
        // block for a small amount of time to avoid busy polling
        final var notifications = pgConn.getNotifications(250);

        for (final var notification : notifications) {
          final String msg = notification.getParameter();
          log.info("Received PgQ notification {}", msg);
          if (msg.equals(RULE_CHANGED_EVENT)) {
            ruleEvaluator.requestRuleRebuild();
          }
        }
      }
    } catch (SQLException ex) {
      log.error("Unexpected error occurred while trying to listen for a PgQ messages: {}",
          ex.getMessage());
    }
  }

}
