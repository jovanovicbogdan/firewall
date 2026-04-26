package dev.bogdanjovanovic.firewall.domain.service;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import dev.bogdanjovanovic.firewall.common.config.FirewallConfig;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleRepository;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RuleEvaluator {

  private final AtomicBoolean shouldRebuild = new AtomicBoolean(false);
  private final AtomicLong lastRebuildTs = new AtomicLong(System.currentTimeMillis());
  private final AtomicReference<RangeMap<Long, RangeMap<Long, Action>>> rules = new AtomicReference<>(
      TreeRangeMap.create());

  private final RuleRepository ruleRepository;
  private final FirewallConfig firewallConfig;
  @SuppressWarnings("FieldCanBeLocal")
  private final ScheduledExecutorService threadScheduler;

  public RuleEvaluator(final RuleRepository ruleRepository, final FirewallConfig firewallConfig) {
    this.ruleRepository = ruleRepository;
    this.firewallConfig = firewallConfig;

    threadScheduler = Executors.newScheduledThreadPool(firewallConfig.corePoolSize());
    threadScheduler.scheduleWithFixedDelay(this::refreshRulesIfNeeded, 0,
        firewallConfig.cooldownMs() / 2, TimeUnit.MILLISECONDS);
    threadScheduler.scheduleWithFixedDelay(this::rebuildRules, 0, 10, TimeUnit.MINUTES);
  }

  public boolean isAllowed(final long srcIp, final long destIp) {
    final var srcRange = getRules().get(srcIp);
    if (srcRange == null) {
      log.info("Source ip '{}' not found in rules", srcIp);
      return false;
    }

    final var action = srcRange.get(destIp);
    if (action == null) {
      log.info("Destination ip '{}' not found in rules", destIp);
      return false;
    }

    return Action.ALLOW.equals(action);
  }

  public RangeMap<Long, RangeMap<Long, Action>> getRules() {
    return rules.get();
  }

  public void requestRuleRebuild() {
    shouldRebuild.set(true);
  }

  private void refreshRulesIfNeeded() {
    if (!shouldRebuild.get()) {
      return;
    }

    final var now = System.currentTimeMillis();
    final var elapsedTime = now - lastRebuildTs.get();
    if (elapsedTime < firewallConfig.cooldownMs()) {
      log.info("Rule sync throttled");
      return;
    }

    rebuildRules();
  }

  private void rebuildRules() {
    log.info("Rebuilding rules");
    try {
      final var rules = ruleRepository.findRules();

      if (rules.isEmpty()) {
        return;
      }

      final var start = System.currentTimeMillis();
      final RangeMap<Long, RangeMap<Long, Action>> rulesRebuild = TreeRangeMap.create();
      for (final var rule : rules) {
        final RangeMap<Long, Action> destMap = TreeRangeMap.create();

        final var destRange = Range.closed(rule.getDestStart(), rule.getDestEnd());
        destMap.put(destRange, rule.getAction());

        final var srcRange = Range.closed(rule.getSrcStart(), rule.getSrcEnd());
        rulesRebuild.merge(srcRange, destMap, (originalDest, incomingDest) -> incomingDest);
      }
      this.rules.set(rulesRebuild);
      final var end = System.currentTimeMillis();
      log.info("Rules rebuilt, took {}ms", end - start);

      shouldRebuild.set(false);
      lastRebuildTs.set(System.currentTimeMillis());
    } catch (Exception ex) {
      log.error("Failed to rebuild rules", ex);
    }
  }

}
