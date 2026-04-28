package dev.bogdanjovanovic.firewall.infrastructure.sync;

import java.sql.Connection;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

@Slf4j
public class PgQListener implements AutoCloseable {

  private static final PGNotification[] EMPTY_PG_NOTIFICATION_ARRAY = new PGNotification[0];
  private static final String CHANNEL = "rule_events";
  private static Connection connection;

  private final Object connectionLock = new Object();
  private final DataSource dataSource;

  public PgQListener(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void close() {
    synchronized (connectionLock) {
      closeConnection();
    }
  }

  public PGNotification[] getPgNotifications() {
    synchronized (connectionLock) {
      try {
        return connection.unwrap(PGConnection.class).getNotifications();
      } catch (Exception ex) {
        log.error("Failed to get PgQ notifications", ex);
        return EMPTY_PG_NOTIFICATION_ARRAY;
      }
    }
  }

  public boolean getConnectionStatus() {
    synchronized (connectionLock) {
      try {
        if (connection != null && !connection.isClosed() && connection.isValid(3)) {
          // Calling multiple times LISTEN on the same CHANNEL
          // won't hurt, PostgreSQL treat it as idempotent.
          // At the same time use it to ping database to
          // keep the connection alive.
          registerListener();
          return true;
        }
        return false;
      } catch (Exception ex) {
        log.error("PgQ listener connection failure", ex);
        return false;
      }
    }
  }

  public void tryToConnectAndStartListening() {
    synchronized (connectionLock) {
      try {
        // connection may still be open but invalid
        closeConnection();

        log.info("Attempting to establish a new connection");
        connection = dataSource.getConnection();
        log.info("PgQ listener connection established");

        registerListener();
      } catch (Exception ex) {
        log.error("PgQ listener connection failure", ex);
      }
    }
  }

  private void registerListener() {
    try (final var stmt = connection.createStatement()) {
      stmt.execute(String.format("LISTEN %s", CHANNEL));
      log.info("Listening on PgQ channel {}", CHANNEL);
    } catch (Exception ex) {
      log.error("Failed to register a listener", ex);
    }
  }

  private void closeConnection() {
    try {
      log.info("Trying to close PgQ listener connection");
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
      log.info("PgQ listener connection closed");
    } catch (Exception ex) {
      log.error("Failed to close PgQ listener connection", ex);
    } finally {
      connection = null;
    }
  }

}
