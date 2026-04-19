package dev.bogdanjovanovic.firewall.presentation.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddRuleRequest {

  @NotNull
  private String srcStart;
  @NotNull
  private String srcEnd;
  @NotNull
  private String destStart;
  @NotNull
  private String destEnd;
  @NotNull
  private String action;

}
