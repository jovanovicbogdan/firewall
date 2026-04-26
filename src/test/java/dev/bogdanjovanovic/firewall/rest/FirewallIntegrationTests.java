package dev.bogdanjovanovic.firewall.rest;

import dev.bogdanjovanovic.firewall.BaseTest;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.presentation.api.dto.AddRuleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class FirewallIntegrationTests extends BaseTest {

  private static final String FIREWALL_RULES_API = "/api/v1/firewall/rules";

  @Test
  void shouldAddNewRule() {
    restTestClient.post()
        .uri(FIREWALL_RULES_API)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new AddRuleRequest(
        "20.20.20.20",
        "25.25.25.25",
        "30.30.30.30",
        "35.35.35.35",
        Action.ALLOW.name()
    ))
        .exchange()
        .expectStatus().is2xxSuccessful();
  }

}
