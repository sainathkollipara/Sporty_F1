package com.example.f1bet.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.f1bet.domain.entity.Event;
import com.example.f1bet.domain.entity.Market;
import com.example.f1bet.domain.entity.Selection;
import com.example.f1bet.domain.enums.SessionType;
import com.example.f1bet.domain.exception.InsufficientBalanceException;
import com.example.f1bet.domain.vo.Odds;
import com.example.f1bet.infrastructure.persistence.memory.InMemoryBetRepository;
import com.example.f1bet.infrastructure.persistence.memory.InMemoryEventRepository;
import com.example.f1bet.infrastructure.persistence.memory.InMemoryUserRepository;
import com.example.f1bet.ports.out.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ApplicationServicesTest {

  @Test
  void placeBet_happyPath_debitsBalance() {
    var users = new InMemoryUserRepository();
    var events = new InMemoryEventRepository();
    var bets = new InMemoryBetRepository();
    var service = new PlaceBetService(users, events, bets);

    UUID userId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();
    UUID selId = UUID.randomUUID();
    var event =
        new Event(
            eventId,
            "X",
            SessionType.RACE,
            "GB",
            2024,
            new Market(
                Market.WINNER,
                List.of(new Selection(selId, "d1", "Norris", Odds.of(new BigDecimal("2.00"))))));
    events.save(event);

    var before = users.findById(userId).orElseThrow();
    assertThat(before.getBalance().getAmount()).isEqualByComparingTo("100.00");

    service.place(
        new PlaceBetService.Command(
            userId, eventId, selId, new BigDecimal("10.00"), "EUR", "key-1"));

    var after = users.findById(userId).orElseThrow();
    assertThat(after.getBalance().getAmount()).isEqualByComparingTo("90.00");
  }

  @Test
  void placeBet_insufficientBalance_throws() {
    var users = new InMemoryUserRepository();
    var events = new InMemoryEventRepository();
    var bets = new InMemoryBetRepository();
    var service = new PlaceBetService(users, events, bets);

    UUID userId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();
    UUID selId = UUID.randomUUID();
    var event =
        new Event(
            eventId,
            "X",
            SessionType.RACE,
            "GB",
            2024,
            new Market(
                Market.WINNER,
                List.of(new Selection(selId, "d1", "Norris", Odds.of(new BigDecimal("2.00"))))));
    events.save(event);

    assertThatThrownBy(
            () ->
                service.place(
                    new PlaceBetService.Command(
                        userId, eventId, selId, new BigDecimal("1000.00"), "EUR", "key-2")))
        .isInstanceOf(InsufficientBalanceException.class);
  }

  @Test
  void recordOutcome_settlesAndCreditsWinners_idempotent() {
    var users = new InMemoryUserRepository();
    var events = new InMemoryEventRepository();
    var bets = new InMemoryBetRepository();
    var place = new PlaceBetService(users, events, bets);
    var record = new RecordOutcomeService(events, bets, users);

    UUID userId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();
    UUID winnerSelId = UUID.randomUUID();
    UUID loserSelId = UUID.randomUUID();
    var market =
        new Market(
            Market.WINNER,
            List.of(
                new Selection(winnerSelId, "d1", "Winner", Odds.of(new BigDecimal("2.00"))),
                new Selection(loserSelId, "d2", "Loser", Odds.of(new BigDecimal("4.00")))));
    var event = new Event(eventId, "X", SessionType.RACE, "GB", 2024, market);
    events.save(event);

    place.place(
        new PlaceBetService.Command(
            userId, eventId, winnerSelId, new BigDecimal("10.00"), "EUR", "key-3"));
    place.place(
        new PlaceBetService.Command(
            userId, eventId, loserSelId, new BigDecimal("10.00"), "EUR", "key-4"));

    var before = users.findById(userId).orElseThrow();

    record.record(eventId, "d1");
    var first = users.findById(userId).orElseThrow();
    assertThat(first.getBalance().getAmount()).isGreaterThan(before.getBalance().getAmount());

    // calling again should not double-credit
    record.record(eventId, "d1");
    var second = users.findById(userId).orElseThrow();
    assertThat(second.getBalance().getAmount())
        .isEqualByComparingTo(first.getBalance().getAmount());
  }
}
