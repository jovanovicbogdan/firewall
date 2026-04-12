package dev.bogdanjovanovic.firewall.domain.service;

import com.google.common.collect.ImmutableRangeMap;
import dev.bogdanjovanovic.firewall.domain.Action;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RuleEvaluator {

  private final AtomicReference<ImmutableRangeMap<Long, ImmutableRangeMap<Long, Action>>> rules = new AtomicReference<>(
      ImmutableRangeMap.of());

  public boolean isAllowed(final Long srcIp, final Long destIp) {
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

  public void setRules(final ImmutableRangeMap<Long, ImmutableRangeMap<Long, Action>> rules) {
    this.rules.set(rules);
  }

  public ImmutableRangeMap<Long, ImmutableRangeMap<Long, Action>> getRules() {
    return rules.get();
  }

}
