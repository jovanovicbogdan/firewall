package dev.bogdanjovanovic.firewall;

import dev.bogdanjovanovic.firewall.common.config.FirewallConfig;
import dev.bogdanjovanovic.firewall.infrastructure.persistence.RuleEntity;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.relational.core.mapping.DefaultNamingStrategy;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseTest {

  @Autowired
  private JdbcClient jdbcClient;
  @Autowired
  protected RestTestClient restTestClient;
  @Autowired
  protected FirewallConfig firewallConfig;

  protected final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:18.0-alpine")).withDatabaseName("firewall");

  static {
    postgres.start();
  }

  @BeforeAll
  static void beforeAll() throws LiquibaseException {
    final var databaseConnectionProvider = new DBConnectionProvider(
        postgres.getJdbcUrl(),
        postgres.getUsername(),
        postgres.getPassword()
    );
    final var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
        new JdbcConnection(databaseConnectionProvider.getConnection()));
    final var liquibase = new Liquibase("migrations/schema.xml", new ClassLoaderResourceAccessor(),
        database);
    liquibase.update(new Contexts(), new LabelExpression());
  }

  @BeforeEach
  void beforeEach() {
    final var ruleTable = namingStrategy.getTableName(RuleEntity.class)
        .replace("_entity", "");
    JdbcTestUtils.deleteFromTables(jdbcClient, ruleTable);
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  protected void sleep(final long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

}
