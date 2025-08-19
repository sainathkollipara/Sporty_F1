package com.example.f1bet.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void shouldNormalizeScaleToTwoWithHalfEven_whenConstructing() {
    Money m = Money.of("USD", new BigDecimal("1.234"));
    assertThat(m.getAmount()).isEqualByComparingTo("1.23");

    Money n = Money.of("USD", new BigDecimal("1.235"));
    assertThat(n.getAmount()).isEqualByComparingTo("1.24");
  }

  @Test
  void shouldRoundHalfEven_whenValueIs1_005_andMultipliedByOne() {
    Money m = Money.of("USD", new BigDecimal("1.005"));
    Money x = m.multiply(new BigDecimal("1"));
    assertThat(x.getAmount()).isEqualByComparingTo("1.00");
  }

  @Test
  void shouldAddSubtractAndMultiply_withHalfEvenNormalization() {
    Money a = Money.of("EUR", "10.10");
    Money b = Money.of("EUR", "2.05");

    assertThat(a.add(b).getAmount()).isEqualByComparingTo("12.15");
    assertThat(a.subtract(b).getAmount()).isEqualByComparingTo("8.05");
    assertThat(a.multiply(new BigDecimal("2.5")).getAmount()).isEqualByComparingTo("25.25");
  }

  @Test
  void shouldRejectNonPositiveStake_whenCreatingStake() {
    assertThatThrownBy(() -> Money.stake("USD", new BigDecimal("0")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("stake must be > 0");
    assertThatThrownBy(() -> Money.stake("USD", new BigDecimal("-1")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowOnCurrencyMismatch_whenAddingOrSubtracting() {
    Money a = Money.of("USD", "1.00");
    Money b = Money.of("EUR", "1.00");
    assertThatThrownBy(() -> a.add(b)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> a.subtract(b)).isInstanceOf(IllegalArgumentException.class);
  }
}
