package dev.bogdanjovanovic.firewall.application;

import com.google.common.net.InetAddresses;
import dev.bogdanjovanovic.firewall.common.exception.ClientErrorException;
import dev.bogdanjovanovic.firewall.common.utils.IPAddressUtils;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleEntity;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleRepository;
import dev.bogdanjovanovic.firewall.presentation.api.dto.AddRuleRequest;
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
    final var srcStart = InetAddresses.toBigInteger(InetAddresses.forString(request.getSrcStart()));
    final var srcEnd = InetAddresses.toBigInteger(InetAddresses.forString(request.getSrcEnd()));

    if (srcStart.compareTo(srcEnd) > 0) {
      throw new ClientErrorException("Invalid source range");
    }

    final String lockKey = getClass().getSimpleName().concat("-key");
    if (!ruleRepository.acquire_lock(lockKey)) {
      throw new RuntimeException("Unable to acquire the lock");
    }

    if (ruleRepository.isOverlap(srcStart.longValue(), srcEnd.longValue(),
        Action.valueOf(request.getAction()))) {
      throw new ClientErrorException("Rule overlaps with the existing rule", HttpStatus.CONFLICT);
    }

    ruleRepository.save(RuleEntity.builder()
        .srcStart(srcStart.longValue())
        .srcEnd(srcEnd.longValue())
        .destStart(IPAddressUtils.ipV4ToLong(request.getDestStart()))
        .destEnd(IPAddressUtils.ipV4ToLong(request.getDestEnd()))
        .action(Action.valueOf(request.getAction()))
        .build());
  }

}
