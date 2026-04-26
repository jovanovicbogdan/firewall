package dev.bogdanjovanovic.firewall.application;

import com.google.common.net.InetAddresses;
import dev.bogdanjovanovic.firewall.common.exception.ClientErrorException;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleEntity;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleRepository;
import dev.bogdanjovanovic.firewall.presentation.api.dto.AddRuleRequest;
import lombok.RequiredArgsConstructor;
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
    final var destStart = InetAddresses.toBigInteger(InetAddresses.forString(request.getDestStart()));
    final var destEnd = InetAddresses.toBigInteger(InetAddresses.forString(request.getDestEnd()));

    if (srcStart.compareTo(srcEnd) > 0 || destStart.compareTo(destEnd) > 0) {
      throw new ClientErrorException("Invalid source or destination range");
    }

    ruleRepository.save(RuleEntity.builder()
        .srcStart(srcStart.longValue())
        .srcEnd(srcEnd.longValue())
        .destStart(destStart.longValue())
        .destEnd(destEnd.longValue())
        .action(Action.valueOf(request.getAction()))
        .build());
  }

}
