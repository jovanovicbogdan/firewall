package dev.bogdanjovanovic.firewall.rest;

import dev.bogdanjovanovic.firewall.BaseTest;
import dev.bogdanjovanovic.firewall.domain.Action;
import dev.bogdanjovanovic.firewall.presentation.api.dto.AddRuleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class FirewallIntegrationTests extends BaseTest {

  private static final String FIREWALL_RULES_API = "/api/v1/firewall/rules";
  private static final String FIREWALL_DECISION_API = "/api/v1/firewall/decision";
  private static final String DECISION_QUERY_PARAM_SRC_IP = "srcIp";
  private static final String DECISION_QUERY_PARAM_DEST_IP = "destIp";

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

  @Test
  void shouldAllowWhenSrcIpAndDestIpInRange() {
    final var addRuleRequest = new AddRuleRequest(
        "1.1.1.1",
        "10.10.10.10",
        "10.10.10.10",
        "15.15.15.15",
        Action.ALLOW.name()
    );

    addRule(addRuleRequest);

    sleep(6500L);

    final var srcIp = addRuleRequest.getSrcStart();
    final var destIp = addRuleRequest.getDestStart();

    restTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path(FIREWALL_DECISION_API)
            .queryParam(DECISION_QUERY_PARAM_SRC_IP, srcIp)
            .queryParam(DECISION_QUERY_PARAM_DEST_IP, destIp)
            .build())
        .exchange()
        .expectStatus().is2xxSuccessful();
  }

  @Test
  void shouldNotAllowWhenSrcIpAndDestIpInRangeAndActionDeny() {
    final var addRuleRequest = new AddRuleRequest(
        "60.60.60.60",
        "70.70.70.70",
        "80.80.80.80",
        "90.90.90.90",
        Action.DENY.name()
    );

    addRule(addRuleRequest);

    sleep(6500L);

    final var srcIp = addRuleRequest.getSrcStart();
    final var destIp = addRuleRequest.getDestStart();

    restTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path(FIREWALL_DECISION_API)
            .queryParam(DECISION_QUERY_PARAM_SRC_IP, srcIp)
            .queryParam(DECISION_QUERY_PARAM_DEST_IP, destIp)
            .build())
        .exchange()
        .expectStatus().is4xxClientError();
  }

  private void addRule(final AddRuleRequest request) {
    restTestClient.post()
        .uri(FIREWALL_RULES_API)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .exchange()
        .expectStatus().is2xxSuccessful();
  }

}
