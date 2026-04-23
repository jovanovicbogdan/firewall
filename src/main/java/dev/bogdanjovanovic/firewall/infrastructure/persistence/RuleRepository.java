package dev.bogdanjovanovic.firewall.infrastructure.persistence;

import dev.bogdanjovanovic.firewall.domain.Action;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RuleRepository extends Repository<RuleEntity, Long> {

  @Query("SELECT pg_try_advisory_xact_lock(hashtext(:key))")
  boolean acquire_lock(@Param("key") String key);

  void save(RuleEntity ruleEntity);

  // Two ranges overlap if:
  //   - the start of one is before the end of the other, and
  //   - the end of one is after the start of the other
  @Query("SELECT EXISTS ("
      + "SELECT 1 "
      + "FROM rule "
      + "WHERE src_start <= :srcEnd "
      + "AND src_end >= :srcStart "
      + "AND action = :action)")
  boolean isOverlap(@Param("srcStart") Long srcStart, @Param("srcEnd") Long srcEnd,
      @Param("action") Action action);

  @Query("SELECT * FROM rule")
  List<RuleEntity> findRules();

  @Query("SELECT pg_notification_queue_usage();")
  double findPgQSize();

}
