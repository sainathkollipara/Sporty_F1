package com.example.f1bet.domain.policy;

import com.example.f1bet.domain.vo.Odds;
import com.example.f1bet.ports.out.RandomPort;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OddsPolicy {
  public Odds randomFrom(RandomPort random) {
    int r = random.nextInt(3); // 0,1,2
    return switch (r) {
      case 0 -> Odds.of(new BigDecimal("2.00"));
      case 1 -> Odds.of(new BigDecimal("3.00"));
      default -> Odds.of(new BigDecimal("4.00"));
    };
  }
}
