package dev.bogdanjovanovic.firewall.common.utils;

public class IPAddressUtils {

  public static Long ipV4ToLong(final String ip) {
    final var ipParts = ip.split("\\.");
    final var a = Long.parseLong(ipParts[0]);
    final var b = Long.parseLong(ipParts[1]);
    final var c = Long.parseLong(ipParts[2]);
    final var d = Long.parseLong(ipParts[3]);
    return (a << 24) | (b << 16) | (c << 8) | d;
  }

}
