package dev.bogdanjovanovic.firewall.application;

import dev.bogdanjovanovic.firewall.common.config.FirewallConfiguration;
import dev.bogdanjovanovic.firewall.common.utils.IPAddressUtils;
import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import dev.bogdanjovanovic.firewall.infrastructure.sync.RuleSyncJob;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EvaluateRuleUseCase {

  @SuppressWarnings("FieldCanBeLocal")
  private final ScheduledExecutorService scheduler;
  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  private final FirewallConfiguration firewallConfiguration;
  private final RuleEvaluator ruleEvaluator;

  public EvaluateRuleUseCase(final FirewallConfiguration firewallConfiguration,
      final RuleEvaluator ruleEvaluator, final RuleSyncJob ruleSyncJob) {
    this.firewallConfiguration = firewallConfiguration;
    this.ruleEvaluator = ruleEvaluator;
    scheduler = Executors.newScheduledThreadPool(firewallConfiguration.corePoolSize());
    scheduler.scheduleAtFixedRate(
        ruleSyncJob::pollForRules,
        0,
        1L,
        TimeUnit.SECONDS
    );
  }

  public boolean execute(final String srcIp, final String destIp) {
    return ruleEvaluator.isAllowed(IPAddressUtils.ipV4ToLong(srcIp),
        IPAddressUtils.ipV4ToLong(destIp));
  }

}
