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

  @Id
  private Long ruleId;
  private Long srcStart;
  private Long srcEnd;
  private Long destStart;
  private Long destEnd;
  private Action action;

}
