package dev.bogdanjovanovic.firewall.application;

import dev.bogdanjovanovic.firewall.common.config.FirewallConfig;
import dev.bogdanjovanovic.firewall.common.utils.IPAddressUtils;
import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EvaluateRuleUseCase {

  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  private final FirewallConfig firewallConfig;
  private final RuleEvaluator ruleEvaluator;

  public EvaluateRuleUseCase(final FirewallConfig firewallConfig,
      final RuleEvaluator ruleEvaluator) {
    this.firewallConfig = firewallConfig;
    this.ruleEvaluator = ruleEvaluator;
  }

  public boolean execute(final String srcIp, final String destIp) {
    return ruleEvaluator.isAllowed(IPAddressUtils.ipV4ToLong(srcIp),
        IPAddressUtils.ipV4ToLong(destIp));
  }

}
