package org.example.shared.exception;

import lombok.Getter;

@Getter
public class TooManyRequestsException extends RuntimeException {

  private final long window;

  public TooManyRequestsException(String message, long window) {
    super(message);

    this.window = window;
  }

  public String getRetryAfterSeconds() {
    return String.valueOf(window);
  }
}
