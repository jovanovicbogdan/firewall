package dev.bogdanjovanovic.firewall.infrastructure.persistence;

import dev.bogdanjovanovic.firewall.domain.Action;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RuleRepository extends Repository<RuleEntity, Long> {

  @Query("SELECT pg_try_advisory_xact_lock(hashtext(:key))")
  boolean acquire_lock(@Param("key") String key);

  void save(RuleEntity ruleEntity);

  @Query("SELECT EXISTS (SELECT 1 FROM rule WHERE :srcStart >= src_start "
      + "AND :srcStart <= src_end "
      + "OR :srcEnd <= src_end "
      + "AND :srcEnd >= src_start AND action = :action)")
  boolean isOverlap(@Param("srcStart") Long srcStart, @Param("srcEnd") Long srcEnd, @Param("action")
      Action action);

  @Query("SELECT * FROM rule WHERE version = (SELECT MAX(version) FROM rule)")
  Optional<RuleEntity> findLatestRule();

  @Query("SELECT * FROM rule WHERE version > :version ORDER BY version ASC")
  List<RuleEntity> findRulesFromVersion(@Param("version") Long version);

}
