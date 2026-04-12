package dev.bogdanjovanovic.firewall;

import dev.bogdanjovanovic.firewall.common.config.FirewallConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({FirewallConfiguration.class})
public class FirewallApplication {

  static void main(String[] args) {
    SpringApplication.run(FirewallApplication.class, args);
  }

}
