package dev.bogdanjovanovic.firewall.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ClientErrorException extends RuntimeException {

  @Getter
  private final HttpStatus status;

  public ClientErrorException(final String message) {
    super(message);
    status = HttpStatus.BAD_REQUEST;
  }

  public ClientErrorException(final String message, final HttpStatus status) {
    super(message);
    this.status = status;
  }

}
