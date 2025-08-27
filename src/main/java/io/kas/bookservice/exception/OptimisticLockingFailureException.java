package io.kas.bookservice.exception;

public class OptimisticLockingFailureException extends RuntimeException {
  public OptimisticLockingFailureException(String message) {
    super(message);
  }
}

