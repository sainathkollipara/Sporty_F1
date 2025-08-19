package com.example.f1bet.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PlaceBetRequest {
  @NotBlank private String userId;
  @NotBlank private String eventId;
  @NotBlank private String selectionId;
  @NotNull private BigDecimal stakeAmount;
  @NotBlank private String currency = "EUR";

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
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

  public BigDecimal getStakeAmount() {
    return stakeAmount;
  }

  public void setStakeAmount(BigDecimal stakeAmount) {
    this.stakeAmount = stakeAmount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}
