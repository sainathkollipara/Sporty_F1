package com.example.f1bet.infrastructure.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.f1bet.domain.entity.Bet;
import com.example.f1bet.domain.entity.Event;
import com.example.f1bet.domain.entity.Market;
import com.example.f1bet.domain.entity.Selection;
import com.example.f1bet.domain.enums.SessionType;
import com.example.f1bet.domain.vo.Money;
import com.example.f1bet.domain.vo.Odds;
import com.example.f1bet.ports.out.BetRepository;
import com.example.f1bet.ports.out.EventRepository;
import com.example.f1bet.ports.out.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryRepositoriesTest {

  @Test
  void user_save_find_update_withOptimisticLock() {
    UserRepository users = new InMemoryUserRepository();
    UUID id = UUID.randomUUID();

    assertThat(users.findById(id)).isPresent(); // created on demand

    var u1 = users.findById(id).orElseThrow();
    var u2 = users.findById(id).orElseThrow();

    users.update(u1, 0L);
    assertThatThrownBy(() -> users.update(u2, 0L)).isInstanceOf(OptimisticLockException.class);
  }

  @Test
  void event_save_find_paging_update_withOptimisticLock() {
    EventRepository events = new InMemoryEventRepository();
    Event e1 =
        new Event(
            UUID.randomUUID(),
            "Bahrain GP",
            SessionType.RACE,
            "BH",
            2025,
            new Market(Market.WINNER, List.of()));
    events.save(e1);

    Optional<Event> loaded = events.findById(e1.getId());
    assertThat(loaded).isPresent();

    assertThat(events.findPage(0, 10)).hasSize(1);
    assertThat(events.findPage(1, 10)).isEmpty();

    Event eLoaded1 = loaded.get();
    Event eLoaded2 = events.findById(e1.getId()).orElseThrow();
    events.update(eLoaded1, 0L);
    assertThatThrownBy(() -> events.update(eLoaded2, 0L))
        .isInstanceOf(OptimisticLockException.class);
  }

  @Test
  void bet_save_find_update_withOptimisticLock() {
    BetRepository bets = new InMemoryBetRepository();
    UUID userId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();
    UUID selId = UUID.randomUUID();
    Bet b =
        new Bet(
            UUID.randomUUID(),
            userId,
            eventId,
            selId,
            Money.stake("EUR", new BigDecimal("10.00")),
            Odds.of(new BigDecimal("2.00")),
            // Event precondition not checked here; repository test focuses on versioning
            new Event(
                eventId,
                "X",
                SessionType.RACE,
                "GB",
                2024,
                new Market(
                    Market.WINNER,
                    List.of(
                        new Selection(selId, "d1", "Norris", Odds.of(new BigDecimal("2.00")))))),
            0L);

    bets.save(b);
    Bet b1 = bets.findById(b.getId()).orElseThrow();
    Bet b2 = bets.findById(b.getId()).orElseThrow();

    bets.update(b1, 0L);
    assertThatThrownBy(() -> bets.update(b2, 0L)).isInstanceOf(OptimisticLockException.class);
  }
}
