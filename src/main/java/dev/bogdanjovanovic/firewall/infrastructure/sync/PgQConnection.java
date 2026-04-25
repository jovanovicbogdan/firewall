package dev.bogdanjovanovic.firewall.infrastructure.sync;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgQConnection implements Runnable {

  private static final String CHANNEL = "rule_events";
  private final DataSource dataSource;
  private final AtomicReference<Connection> connection;

  public PgQConnection(final DataSource dataSource) {
    this.dataSource = dataSource;
    this.connection = new AtomicReference<>(establishConnection());
  }

  @Override
  public void run() {
    try (final var stmt = connection.get().createStatement()) {
      stmt.execute("SELECT 1;");
    } catch (Exception ex) {
      log.error("PgQ connection keep alive failed", ex);
      if (ex instanceof SQLException) {
        final var conn = establishConnection();
        if (conn != null) {
          connection.set(conn);
          registerListener();
        }
      }
    }
  }

  public Connection getConnection() {
    return connection.get();
  }

  public void registerListener() {
    try (final var stmt = getConnection().createStatement()) {
      stmt.execute(String.format("LISTEN %s", CHANNEL));
    } catch (Exception ex) {
      log.error("Failed to register PgQ listener", ex);
    }
  }

  private Connection establishConnection() {
    try {
      log.info("Attempting to establish a connection");
      return dataSource.getConnection();
    } catch (Exception ex) {
      log.error("Failed to establish a connection to PgQ", ex);
      return null;
    }
  }

}
