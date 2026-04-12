package dev.bogdanjovanovic.firewall.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> exceptionHandler(final Exception ex, final HttpServletRequest request) {
    log.debug(ex.getMessage());
    final var response = new ApiErrorResponse(request.getServletPath(), "Internal Server Error");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ClientErrorException.class)
  public ResponseEntity<?> exceptionHandler(final ClientErrorException ex,
      final HttpServletRequest request) {
    final var response = new ApiErrorResponse(request.getServletPath(), ex.getMessage());
    return new ResponseEntity<>(response, ex.getStatus());
  }

}
