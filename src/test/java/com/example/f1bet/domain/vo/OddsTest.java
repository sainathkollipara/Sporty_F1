package com.example.f1bet.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OddsTest {

  @Test
  void shouldRejectUnsupportedValues_whenConstructing() {
    assertThatThrownBy(() -> Odds.of(new BigDecimal("1.50")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Odds.of(new BigDecimal("2.50")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Odds.of(new BigDecimal("5.00")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldComputePayout_withTwoDecimalRounding() {
    Money stake = Money.stake("EUR", new BigDecimal("12.34"));
    Odds odds = Odds.of(new BigDecimal("3.00"));
    Money payout = odds.payout(stake);
    assertThat(payout.getCurrency()).isEqualTo("EUR");
    assertThat(payout.getAmount()).isEqualByComparingTo("37.02");
  }

  @Test
  void shouldHandleLargeStakeEdgeCases() {
    Money bigStake = Money.stake("EUR", new BigDecimal("1000000000000.00"));
    Odds odds = Odds.of(new BigDecimal("4.00"));
    Money payout = odds.payout(bigStake);
    assertThat(payout.getAmount()).isEqualByComparingTo("4000000000000.00");
  }
}
