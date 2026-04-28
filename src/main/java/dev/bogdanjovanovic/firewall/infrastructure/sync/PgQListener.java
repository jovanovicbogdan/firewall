package dev.bogdanjovanovic.firewall.infrastructure.sync;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgQListener implements Runnable {

  private static final String CHANNEL = "rule_events";
  private static Connection CONNECTION = null;

  private final AtomicBoolean listenerRegistered = new AtomicBoolean(false);
  private final DataSource dataSource;

  public PgQListener(final DataSource dataSource) {
    this.dataSource = dataSource;
    init();
  }

  @Override
  public void run() {
    if (!listenerRegistered.get()) {
      registerListener();
    }

    try (final var stmt = CONNECTION.createStatement()) {
      stmt.execute("SELECT 1;");
    } catch (Exception ex) {
      log.error("PgQ listener failed", ex);
      try {
        if (CONNECTION.isValid(3)) return;

        CONNECTION.close();
        CONNECTION = null;
        listenerRegistered.set(false);

        if (establishConnection()) {
          registerListener();
        }
      } catch (SQLException sqlException) {
        log.error("Failed to close the connection", sqlException);
      }
    }
  }

  public Connection getConnection() {
    return CONNECTION;
  }

  private void init() {
    if (establishConnection()) {
      registerListener();
    }
  }

  private boolean establishConnection() {
    try {
      log.info("Attempting to establish a connection");
      CONNECTION = dataSource.getConnection();
      log.info("PgQ listener connection established");
      return true;
    } catch (Exception ex) {
      log.error("Failed to establish a connection to PgQ", ex);
      return false;
    }
  }

  private void registerListener() {
    try (final var stmt = getConnection().createStatement()) {
      stmt.execute(String.format("LISTEN %s", CHANNEL));
      log.info("Listening on PgQ channel {}", CHANNEL);
      listenerRegistered.set(true);
    } catch (Exception ex) {
      log.error("Failed to register a listener", ex);
      listenerRegistered.set(false);
    }
  }

}
