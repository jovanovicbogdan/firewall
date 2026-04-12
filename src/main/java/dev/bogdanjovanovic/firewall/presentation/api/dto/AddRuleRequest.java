package dev.bogdanjovanovic.firewall.presentation.api.dto;

import dev.bogdanjovanovic.firewall.common.validator.EnumValidator;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddRuleRequest {

  @NotNull
  @Pattern(regexp = RuleEntity.IP_V4_ADDRESS_PATTERN)
  private String srcStart;
  @NotNull
  @Pattern(regexp = RuleEntity.IP_V4_ADDRESS_PATTERN)
  private String srcEnd;
  @NotNull
  @Pattern(regexp = RuleEntity.IP_V4_ADDRESS_PATTERN)
  private String destStart;
  @NotNull
  @Pattern(regexp = RuleEntity.IP_V4_ADDRESS_PATTERN)
  private String destEnd;
  @NotNull
  @EnumValidator(value = Action.class, message = "action can be either 'ALLOW' or 'DENY'")
  private String action;

}
