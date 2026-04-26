package dev.bogdanjovanovic.firewall.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

public interface RuleRepository extends Repository<RuleEntity, Long> {

  void save(RuleEntity ruleEntity);

  @Query("SELECT * FROM rule")
  List<RuleEntity> findRules();

  @Query("SELECT pg_notification_queue_usage();")
  double findPgQSize();

}
