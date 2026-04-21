package dev.bogdanjovanovic.firewall.presentation.api;

import com.google.common.net.InetAddresses;
import dev.bogdanjovanovic.firewall.application.AddRuleUseCase;
import dev.bogdanjovanovic.firewall.application.EvaluateRuleUseCase;
import dev.bogdanjovanovic.firewall.common.exception.ClientErrorException;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.presentation.api.dto.AddRuleRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
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

  private static final InetAddressValidator INET_ADDRESS_VALIDATOR = InetAddressValidator.getInstance();

  private final AddRuleUseCase addRuleUseCase;
  private final EvaluateRuleUseCase evaluateRuleUseCase;

  @PostMapping(value = "/rules", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public void addRule(@Valid @RequestBody final AddRuleRequest request) {
    if (!INET_ADDRESS_VALIDATOR.isValidInet4Address(request.getSrcStart())
        || !INET_ADDRESS_VALIDATOR.isValidInet4Address(request.getSrcEnd())
        || !INET_ADDRESS_VALIDATOR.isValidInet4Address(request.getDestStart())
        || !INET_ADDRESS_VALIDATOR.isValidInet4Address(request.getDestEnd())) {
      throw new ClientErrorException("Please provide IPv4 addresses in format 'A.B.C.D'");
    }
    if (!EnumUtils.isValidEnum(Action.class, request.getAction())) {
      throw new ClientErrorException("Allowed actions are " + Arrays.toString(Action.values()));
    }
    addRuleUseCase.execute(request);
  }

  @GetMapping(value = "/decision")
  public ResponseEntity<?> evaluateRule(@RequestParam("srcIp") final String srcIp,
      @RequestParam("destIp") final String destIp) {
    if (!INET_ADDRESS_VALIDATOR.isValidInet4Address(srcIp)
        || !INET_ADDRESS_VALIDATOR.isValidInet4Address(destIp)) {
      throw new ClientErrorException("Please provide IPv4 addresses in format 'A.B.C.D'");
    }
    final var isAllowed = evaluateRuleUseCase.execute(InetAddresses.forString(srcIp),
        InetAddresses.forString(destIp));
    if (isAllowed) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

}
