package org.opentripplanner.framework.application;

/**
 * This Exception is used to signal that the current (HTTP) request has timeout.
 */
public class OTPRequestTimeoutException extends RuntimeException {

  @Override
  public String getMessage() {
    return "TIMEOUT! The request is too resource intensive.";
  }

  /**
   * The Grizzly web server is configured with a transaction timeout. The Grizzly server
   * will set the interrupt flag on the current thread. OTP do not have many blocking operations
   * witch check the interrupted flag, so instead we need to do the check manually. The check has
   * a small performance overhead so try to place the check in the beginning of significantly big
   * finite block of calculations.
   */
  public static void checkForTimeout() {
    if (Thread.currentThread().isInterrupted()) {
      throw new OTPRequestTimeoutException();
    }
  }
}
