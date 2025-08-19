package com.example.f1bet.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BetResponse {
  @NotBlank private String betId;
  @NotBlank private String status;
  @NotNull private BigDecimal stake;
  @NotNull private Double capturedOdds;
  @NotBlank private String eventId;
  @NotBlank private String selectionId;

  public String getBetId() {
    return betId;
  }

  public void setBetId(String betId) {
    this.betId = betId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public BigDecimal getStake() {
    return stake;
  }

  public void setStake(BigDecimal stake) {
    this.stake = stake;
  }

  public Double getCapturedOdds() {
    return capturedOdds;
  }

  public void setCapturedOdds(Double capturedOdds) {
    this.capturedOdds = capturedOdds;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getSelectionId() {
    return selectionId;
  }

  public void setSelectionId(String selectionId) {
    this.selectionId = selectionId;
  }
}
