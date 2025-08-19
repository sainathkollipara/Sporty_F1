package com.example.f1bet.domain.entity;

import com.example.f1bet.domain.vo.Odds;
import java.util.UUID;

public class Selection {
  private final UUID id;
  private final String driverId;
  private final String driverName;
  private final Odds odds;

  public Selection(UUID id, String driverId, String driverName, Odds odds) {
    this.id = id;
    this.driverId = driverId;
    this.driverName = driverName;
    this.odds = odds;
  }

  public UUID getId() {
    return id;
  }

  public String getDriverId() {
    return driverId;
  }

  public String getDriverName() {
    return driverName;
  }

  public Odds getOdds() {
    return odds;
  }
}
