package dev.bogdanjovanovic.firewall.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config")
public record FirewallConfig(int corePoolSize) {

}
