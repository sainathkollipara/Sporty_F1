package com.example.f1bet.domain.entity;

import com.example.f1bet.domain.enums.EventState;
import com.example.f1bet.domain.enums.SessionType;
import com.example.f1bet.domain.exception.IllegalEventStateException;
import java.util.Objects;
import java.util.UUID;

public class Event {
  public java.time.Instant getStartTime() {
    // TODO: Wire up actual startTime if available in future
    return null;
  }

  private final UUID id;
  private final String name;
  private final SessionType sessionType;
  private final String country;
  private final int year;
  private EventState state;
  private final Market market; // single WINNER market

  public Event(
      UUID id, String name, SessionType sessionType, String country, int year, Market market) {
    this.id = Objects.requireNonNull(id);
    this.name = Objects.requireNonNull(name);
    this.sessionType = Objects.requireNonNull(sessionType);
    this.country = Objects.requireNonNull(country);
    this.year = year;
    this.market = Objects.requireNonNull(market);
    if (!Market.WINNER.equals(market.getType())) {
      throw new IllegalArgumentException("Event must contain WINNER market only");
    }
    this.state = EventState.SCHEDULED;
  }

  public void markFinished() {
    if (state != EventState.SCHEDULED) {
      throw new IllegalEventStateException("Event must be SCHEDULED to finish");
    }
    this.state = EventState.FINISHED;
  }

  public void markSettled() {
    if (state != EventState.FINISHED) {
      throw new IllegalEventStateException("Event must be FINISHED to settle");
    }
    this.state = EventState.SETTLED;
  }

  public boolean hasSelection(UUID selectionId) {
    return market.containsSelectionId(selectionId);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public SessionType getSessionType() {
    return sessionType;
  }

  public String getCountry() {
    return country;
  }

  public int getYear() {
    return year;
  }

  public EventState getState() {
    return state;
  }

  public Market getMarket() {
    return market;
  }
}
