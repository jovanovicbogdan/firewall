package dev.bogdanjovanovic.firewall.infrastructure.sync;

import dev.bogdanjovanovic.firewall.common.config.FirewallConfig;
import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
  private final DataSource dataSource;
  private final FirewallConfig firewallConfig;
  private final RuleEvaluator ruleEvaluator;

  public RuleSync(final DataSource dataSource, final FirewallConfig firewallConfig,
      final RuleEvaluator ruleEvaluator) {
    this.dataSource = dataSource;
    this.firewallConfig = firewallConfig;
    this.ruleEvaluator = ruleEvaluator;
  }

  @Override
  public void start() {
    final var pgQWorker = new Thread(new PgQWorker(dataSource, ruleEvaluator));
//    pgQWorker.setDaemon(true);

    for (int i = 0; i < firewallConfig.corePoolSize(); i++) {
      threadExecutor.execute(pgQWorker);
    }

    isRunning.set(true);
    log.info("PgQ listener started");
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
