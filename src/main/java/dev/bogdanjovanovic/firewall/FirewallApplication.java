package dev.bogdanjovanovic.firewall;

import dev.bogdanjovanovic.firewall.common.config.FirewallConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({FirewallConfig.class})
public class FirewallApplication {

  static void main(String[] args) {
    SpringApplication.run(FirewallApplication.class, args);
  }

}
