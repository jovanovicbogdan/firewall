package dev.bogdanjovanovic.firewall.presentation.api;

import dev.bogdanjovanovic.firewall.application.AddRuleUseCase;
import dev.bogdanjovanovic.firewall.application.EvaluateRuleUseCase;
import dev.bogdanjovanovic.firewall.common.exception.ClientErrorException;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleEntity;
import dev.bogdanjovanovic.firewall.presentation.api.dto.AddRuleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/firewall")
@RequiredArgsConstructor
public class FirewallController {

  private final AddRuleUseCase addRuleUseCase;
  private final EvaluateRuleUseCase evaluateRuleUseCase;

  @PostMapping(value = "/rules", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public void addRule(@Valid @RequestBody final AddRuleRequest request) {
    addRuleUseCase.execute(request);
  }

  @GetMapping(value = "/decision")
  public ResponseEntity<?> evaluateRule(@RequestParam("srcIp") final String srcIp,
      @RequestParam("destIp") final String destIp) {
    if (!srcIp.matches(RuleEntity.IP_V4_ADDRESS_PATTERN) || !destIp.matches(
        RuleEntity.IP_V4_ADDRESS_PATTERN)) {
      throw new ClientErrorException("Invalid parameter(s) provided.");
    }
    final var isAllowed = evaluateRuleUseCase.execute(srcIp, destIp);
    if (isAllowed) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

}
