package dev.bogdanjovanovic.firewall.infrastructure.sync;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PgQWatchdog implements Runnable {

  private final PgQListener pgQListener;

  @Override
  public void run() {
    if (pgQListener.getConnectionStatus()) return;
    pgQListener.tryToConnectAndStartListening();
  }

}
