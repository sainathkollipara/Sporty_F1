package com.example.f1bet.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {
  private static final int SCALE = 2;
  private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

  private final BigDecimal amount;
  private final String currency;

  private Money(BigDecimal normalizedAmount, String currency) {
    this.amount = normalizedAmount;
    this.currency = currency;
  }

  public static Money of(String currency, BigDecimal amount) {
    Objects.requireNonNull(currency, "currency");
    Objects.requireNonNull(amount, "amount");
    return new Money(amount.setScale(SCALE, ROUNDING), currency);
  }

  public static Money of(String currency, String amount) {
    return of(currency, new BigDecimal(amount));
  }

  public static Money stake(String currency, BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("stake must be > 0");
    }
    return of(currency, amount);
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return of(currency, amount.add(other.amount));
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return of(currency, amount.subtract(other.amount));
  }

  public Money multiply(BigDecimal factor) {
    Objects.requireNonNull(factor, "factor");
    return of(currency, amount.multiply(factor));
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other, "other");
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + this.currency + " vs " + other.currency);
    }
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Money)) return false;
    Money money = (Money) o;
    return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }
}
