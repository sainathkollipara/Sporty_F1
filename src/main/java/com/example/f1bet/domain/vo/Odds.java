package com.example.f1bet.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Odds {
  public java.math.BigDecimal getValue() {
    return getDecimal();
  }

  private static final int SCALE = 2;
  private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

  private static final BigDecimal TWO = new BigDecimal("2.00");
  private static final BigDecimal THREE = new BigDecimal("3.00");
  private static final BigDecimal FOUR = new BigDecimal("4.00");

  private final BigDecimal decimal;

  private Odds(BigDecimal normalized) {
    this.decimal = normalized;
  }

  public static Odds of(BigDecimal decimal) {
    Objects.requireNonNull(decimal, "decimal");
    BigDecimal normalized = decimal.setScale(SCALE, ROUNDING);
    if (!isAllowed(normalized)) {
      throw new IllegalArgumentException(
          "Unsupported odds: " + normalized + " (allowed: 2.00, 3.00, 4.00)");
    }
    return new Odds(normalized);
  }

  private static boolean isAllowed(BigDecimal value) {
    return value.compareTo(TWO) == 0 || value.compareTo(THREE) == 0 || value.compareTo(FOUR) == 0;
  }

  public Money payout(Money stake) {
    Objects.requireNonNull(stake, "stake");
    return stake.multiply(decimal);
  }

  public BigDecimal getDecimal() {
    return decimal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Odds)) return false;
    Odds odds = (Odds) o;
    return Objects.equals(decimal, odds.decimal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(decimal);
  }
}
