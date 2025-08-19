package com.example.f1bet.domain.entity;

import com.example.f1bet.domain.enums.BetStatus;
import com.example.f1bet.domain.enums.EventState;
import com.example.f1bet.domain.exception.InvalidBetException;
import com.example.f1bet.domain.vo.Money;
import com.example.f1bet.domain.vo.Odds;
import java.util.Objects;
import java.util.UUID;

public class Bet {
  private final UUID id;
  private final UUID userId;
  private final UUID eventId;
  private final UUID selectionId;
  private final Money stake;
  private final Odds capturedOdds;
  private BetStatus status;
  private final long version;

  public Bet(
      UUID id,
      UUID userId,
      UUID eventId,
      UUID selectionId,
      Money stake,
      Odds capturedOdds,
      Event event,
      long version) {
    this.id = Objects.requireNonNull(id);
    this.userId = Objects.requireNonNull(userId);
    this.eventId = Objects.requireNonNull(eventId);
    this.selectionId = Objects.requireNonNull(selectionId);
    this.stake = Objects.requireNonNull(stake);
    this.capturedOdds = Objects.requireNonNull(capturedOdds);
    validate(event);
    this.status = BetStatus.PENDING;
    this.version = version;
  }

  private void validate(Event event) {
    if (event.getState() != EventState.SCHEDULED) {
      throw new InvalidBetException("Bet allowed only when event is SCHEDULED");
    }
    if (!event.hasSelection(selectionId)) {
      throw new InvalidBetException("Selection does not belong to event");
    }
  }

  public void markWon() {
    ensurePending();
    this.status = BetStatus.WON;
  }

  public void markLost() {
    ensurePending();
    this.status = BetStatus.LOST;
  }

  public void markVoid() {
    ensurePending();
    this.status = BetStatus.VOID;
  }

  private void ensurePending() {
    if (this.status != BetStatus.PENDING) {
      throw new InvalidBetException("Bet is not in PENDING state");
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getEventId() {
    return eventId;
  }

  public UUID getSelectionId() {
    return selectionId;
  }

  public Money getStake() {
    return stake;
  }

  public Odds getCapturedOdds() {
    return capturedOdds;
  }

  public BetStatus getStatus() {
    return status;
  }

  public long getVersion() {
    return version;
  }
}
