package dev.bogdanjovanovic.firewall.infrastructure.persistence;

import dev.bogdanjovanovic.firewall.domain.Action;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("rule")
public class RuleEntity {

  public static final String IP_V4_ADDRESS_PATTERN = "^(((?!25?[6-9])[12]\\d|[1-9])?\\d\\.?\\b){4}$";

  @Id
  private Long ruleId;
  private Long srcStart;
  private Long srcEnd;
  private Long destStart;
  private Long destEnd;
  private Action action;
  /**
   * Version of ruleset.
   * Increments everytime new rule is inserted.
   */
  private Long version;

}
