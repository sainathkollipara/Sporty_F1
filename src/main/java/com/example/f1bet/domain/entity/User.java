package com.example.f1bet.domain.entity;

import com.example.f1bet.domain.vo.Money;
import java.util.UUID;

public class User {
  private static final String DEFAULT_CURRENCY = "EUR";

  private final UUID id;
  private final Money balance;

  private User(UUID id, Money balance) {
    this.id = id;
    this.balance = balance;
  }

  public static User create(UUID id) {
    return new User(id, Money.of(DEFAULT_CURRENCY, "100.00"));
  }

  public UUID getId() {
    return id;
  }

  public Money getBalance() {
    return balance;
  }

  public User withBalance(Money newBalance) {
    return new User(this.id, newBalance);
  }
}
