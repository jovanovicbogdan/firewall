package dev.bogdanjovanovic.firewall.infrastructure.sync;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableRangeMap.Builder;
import com.google.common.collect.Range;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleSyncJob {

  private long rulesVersion = 0;
  private final Object snapshotLock = new Object();

  private final RuleEvaluator ruleEvaluator;
  private final RuleRepository ruleRepository;

  @Transactional
  public void pollForRules() {
    final var lockKey = getClass().getSimpleName().concat("-key");
    if (!ruleRepository.acquire_lock(lockKey)) {
      throw new RuntimeException("Unable to acquire lock");
    }

    log.debug("Finding latest rule...");
    final var maybeRule = ruleRepository.findLatestRule();

    if (maybeRule.isEmpty()) {
      return;
    }

    final var latestRule = maybeRule.get();

    synchronized (snapshotLock) {
      if (latestRule.getVersion() <= rulesVersion) {
        return;
      }

      log.debug("Find rules from version '{}'", rulesVersion);
      final var rules = ruleRepository.findRulesFromVersion(rulesVersion);

      if (rules.isEmpty()) {
        return;
      }

      final Builder<Long, ImmutableRangeMap<Long, Action>> rulesMapBuilder = ImmutableRangeMap.builder();
      rulesMapBuilder.putAll(ruleEvaluator.getRules());
      for (final var rule : rules) {
        final var destMap = ImmutableRangeMap.of(
            Range.closed(rule.getDestStart(), rule.getDestEnd()), rule.getAction());
        rulesMapBuilder.put(Range.closed(rule.getSrcStart(), rule.getSrcEnd()), destMap);
      }

      ruleEvaluator.setRules(rulesMapBuilder.build());
      rulesVersion = latestRule.getVersion();
    }
  }

}
