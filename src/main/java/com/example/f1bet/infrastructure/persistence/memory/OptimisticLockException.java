package com.example.f1bet.infrastructure.persistence.memory;

public class OptimisticLockException extends RuntimeException {
  public OptimisticLockException(String message) {
    super(message);
  }
}
