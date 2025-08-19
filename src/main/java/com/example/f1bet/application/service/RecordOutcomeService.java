package com.example.f1bet.application.service;

import com.example.f1bet.domain.entity.Bet;
import com.example.f1bet.domain.entity.Event;
import com.example.f1bet.domain.enums.EventState;
import com.example.f1bet.domain.exception.IllegalEventStateException;
import com.example.f1bet.ports.out.BetRepository;
import com.example.f1bet.ports.out.EventRepository;
import com.example.f1bet.ports.out.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RecordOutcomeService {

  private final EventRepository events;
  private final BetRepository bets;
  private final UserRepository users;

  public RecordOutcomeService(EventRepository events, BetRepository bets, UserRepository users) {
    this.events = events;
    this.bets = bets;
    this.users = users;
  }

  public void record(UUID eventId, String winningDriverId) {
    Event event =
        events
            .findById(eventId)
            .orElseThrow(() -> new IllegalEventStateException("Event not found"));

    if (event.getState() == EventState.SETTLED) {
      return; // idempotent
    }
    long version = events.versionOf(eventId);
    if (event.getState() == EventState.SCHEDULED) {
      event.markFinished();
      events.update(event, version);
      version++;
    }
    if (event.getState() == EventState.FINISHED) {
      event.markSettled();
      events.update(event, version);
      version++;
    }

    List<Bet> byEvent = bets.findByEventId(eventId);
    for (Bet bet : byEvent) {
      boolean winner =
          event.getMarket().getSelections().stream()
              .anyMatch(
                  s ->
                      s.getId().equals(bet.getSelectionId())
                          && s.getDriverId().equals(winningDriverId));
      if (winner) {
        bet.markWon();
        // update bet status first (use repository version) then credit user
        long betVersion = bets.versionOf(bet.getId());
        bets.update(bet, betVersion);

        users
            .findById(bet.getUserId())
            .ifPresent(
                u -> {
                  long uv = users.versionOf(u.getId());
                  users.update(
                      u.withBalance(
                          u.getBalance().add(bet.getCapturedOdds().payout(bet.getStake()))),
                      uv);
                });
      } else {
        bet.markLost();
        long betVersion = bets.versionOf(bet.getId());
        bets.update(bet, betVersion);
      }
    }
  }
}
