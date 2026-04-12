package dev.bogdanjovanovic.firewall.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorResponse {

  private String path;
  private String message;

}
