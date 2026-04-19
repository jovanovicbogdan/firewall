package dev.bogdanjovanovic.firewall.application;

import com.google.common.net.InetAddresses;
import dev.bogdanjovanovic.firewall.common.config.FirewallConfig;
import dev.bogdanjovanovic.firewall.domain.service.RuleEvaluator;
import java.net.InetAddress;
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

  public boolean execute(final InetAddress srcIp, final InetAddress destIp) {
    return ruleEvaluator.isAllowed(InetAddresses.toBigInteger(srcIp).longValue(),
        InetAddresses.toBigInteger(destIp).longValue());
  }

}
