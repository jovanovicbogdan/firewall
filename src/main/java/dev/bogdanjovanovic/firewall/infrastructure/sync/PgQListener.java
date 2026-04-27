package dev.bogdanjovanovic.firewall.infrastructure.sync;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgQListener implements Runnable {

  private static final String CHANNEL = "rule_events";
  private static Connection CONNECTION = null;
  private final DataSource dataSource;

  public PgQListener(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void run() {
    if (CONNECTION == null) {
      establishConnection();
      return;
    }

    try (final var stmt = CONNECTION.createStatement()) {
      stmt.execute("SELECT 1;");
    } catch (Exception ex) {
      log.error("PgQ listener failed", ex);
      if (ex instanceof SQLException) {
        validateConnection();
      }
    }
  }

  public Connection getConnection() {
    return CONNECTION;
  }

  private void establishConnection() {
    try {
      log.info("Attempting to establish a connection");
      CONNECTION = dataSource.getConnection();
      registerListener();
    } catch (Exception ex) {
      log.error("Failed to establish a connection to PgQ", ex);
      CONNECTION = null;
    }
  }

  private void registerListener() {
    try (final var stmt = getConnection().createStatement()) {
      stmt.execute(String.format("LISTEN %s", CHANNEL));
      log.info("Listening on PgQ channel {}", CHANNEL);
    } catch (Exception ex) {
      log.error("Failed to register PgQ listener", ex);
    }
  }

  private void validateConnection() {
    try {
      if (!CONNECTION.isValid(3)) {
        CONNECTION.close();
        CONNECTION = null;
      }
    } catch (Exception ex) {
      log.info("Connection is invalid", ex);
      CONNECTION = null;
    }
  }

}
