package com.example.f1bet.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public class RecordOutcomeRequest {
  @NotBlank private String winningDriverId;

  public String getWinningDriverId() {
    return winningDriverId;
  }

  public void setWinningDriverId(String winningDriverId) {
    this.winningDriverId = winningDriverId;
  }
}
