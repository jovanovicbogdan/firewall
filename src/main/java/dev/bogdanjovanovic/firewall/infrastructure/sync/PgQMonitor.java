package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PgQMonitor implements Runnable {

  private volatile boolean isAlertActive = false;
  private final RuleRepository ruleRepository;

  @Override
  public void run() {
    try {
      final var pgQSize = ruleRepository.findPgQSize();

      if (pgQSize > 0.5) {
        if (!isAlertActive) {
          isAlertActive = true;
          log.warn("PgQ exceeded 50% threshold");
          // no way to programmatically clear the queue, sound an alarm, e.g. through PagerDuty
        }
      } else {
        isAlertActive = false;
      }
    } catch (Exception ex) {
      log.error("PgQ monitor failed", ex);
    }
  }

}
