package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.common.config.FirewallConfig;
import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RuleSync implements SmartLifecycle {

  private final AtomicBoolean isRunning = new AtomicBoolean(false);
  private final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
  private final ScheduledExecutorService threadScheduler = Executors.newSingleThreadScheduledExecutor();
  private final DataSource dataSource;
  private final RuleEvaluator ruleEvaluator;
  private final RuleRepository ruleRepository;

  public RuleSync(final DataSource dataSource, final RuleEvaluator ruleEvaluator,
      final RuleRepository ruleRepository) {
    this.dataSource = dataSource;
    this.ruleEvaluator = ruleEvaluator;
    this.ruleRepository = ruleRepository;
  }

  @Override
  public void start() {
    final var pgQWorker = new Thread(new PgQWorker(dataSource, ruleEvaluator));
//    pgQWorker.setDaemon(true);

    threadExecutor.execute(pgQWorker);
    log.info("PgQ listener started");

    final var pgQMonitor = new Thread(new PgQMonitor(ruleRepository));

    threadScheduler.scheduleWithFixedDelay(pgQMonitor, 0, 10, TimeUnit.SECONDS);
    log.info("PgQ monitor started");

    isRunning.set(true);
  }

  @Override
  public void stop() {
    threadExecutor.shutdownNow();
    isRunning.set(false);
    log.info("PgQ listener stopped");
  }

  @Override
  public boolean isRunning() {
    return isRunning.get();
  }

}
