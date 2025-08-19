package com.example.f1bet.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.f1bet.domain.vo.Odds;
import com.example.f1bet.ports.out.RandomPort;
import java.math.BigDecimal;
import java.util.Random;
import org.junit.jupiter.api.Test;

class OddsPolicyTest {

  private static final class SeededRandomPort implements RandomPort {
    private final Random random;

    private SeededRandomPort(long seed) {
      this.random = new Random(seed);
    }

    @Override
    public int nextInt(int bound) {
      return random.nextInt(bound);
    }
  }

  @Test
  void shouldBeDeterministic_withSeededRandom() {
    OddsPolicy policy = new OddsPolicy();
    RandomPort rand1 = new SeededRandomPort(42L);
    RandomPort rand2 = new SeededRandomPort(42L);

    for (int i = 0; i < 10; i++) {
      Odds o1 = policy.randomFrom(rand1);
      Odds o2 = policy.randomFrom(rand2);
      assertThat(o1.getDecimal()).isEqualByComparingTo(o2.getDecimal());
    }
  }

  @Test
  void shouldBeUniform_overLargeSample() {
    OddsPolicy policy = new OddsPolicy();
    RandomPort rand = new SeededRandomPort(123L);
    int c2 = 0, c3 = 0, c4 = 0;
    for (int i = 0; i < 3000; i++) {
      BigDecimal d = policy.randomFrom(rand).getDecimal();
      if (d.compareTo(new BigDecimal("2.00")) == 0) c2++;
      else if (d.compareTo(new BigDecimal("3.00")) == 0) c3++;
      else if (d.compareTo(new BigDecimal("4.00")) == 0) c4++;
    }
    // crude uniformity check: counts roughly equal within 15%
    assertThat(c2).isBetween(800, 1200);
    assertThat(c3).isBetween(800, 1200);
    assertThat(c4).isBetween(800, 1200);
  }
}
