package com.example.f1bet.domain.exception;

public class InsufficientBalanceException extends DomainException {
  public InsufficientBalanceException(String message) {
    super(message);
  }
}
