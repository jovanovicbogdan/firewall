package dev.bogdanjovanovic.firewall.application;

import dev.bogdanjovanovic.firewall.common.exception.ClientErrorException;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleEntity;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleRepository;
import dev.bogdanjovanovic.firewall.presentation.api.dto.AddRuleRequest;
import dev.bogdanjovanovic.firewall.common.utils.IPAddressUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddRuleUseCase {

  private final RuleRepository ruleRepository;

  @Transactional
  public void execute(final AddRuleRequest request) {
    final var srcStart = IPAddressUtils.ipV4ToLong(request.getSrcStart());
    final var srcEnd = IPAddressUtils.ipV4ToLong(request.getSrcEnd());

    if (srcStart > srcEnd) {
      throw new ClientErrorException("Invalid source range");
    }

    final String lockKey = getClass().getSimpleName().concat("-key");
    if (!ruleRepository.acquire_lock(lockKey)) {
      throw new RuntimeException("Unable to acquire the lock");
    }
    if (ruleRepository.isOverlap(srcStart, srcEnd, Action.valueOf(request.getAction()))) {
      throw new ClientErrorException("Rule overlaps with the existing rule", HttpStatus.CONFLICT);
    }

    final var rule = ruleRepository.findLatestRule();

    ruleRepository.save(RuleEntity.builder()
        .srcStart(srcStart)
        .srcEnd(srcEnd)
        .destStart(IPAddressUtils.ipV4ToLong(request.getDestStart()))
        .destEnd(IPAddressUtils.ipV4ToLong(request.getDestEnd()))
        .action(Action.valueOf(request.getAction()))
        .version(rule.map(ruleEntity -> ruleEntity.getVersion() + 1).orElse(1L))
        .build());
  }

}
