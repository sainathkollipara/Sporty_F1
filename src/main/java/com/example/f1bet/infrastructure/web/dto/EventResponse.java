package com.example.f1bet.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public class EventResponse {
  @NotBlank private String id;
  @NotBlank private String name;
  @NotBlank private String sessionType;
  @NotBlank private String country;
  @NotNull private Integer year;
  @NotNull private Instant startTime;
  @NotNull private List<DriverMarketDto> driverMarket;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSessionType() {
    return sessionType;
  }

  public void setSessionType(String sessionType) {
    this.sessionType = sessionType;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public List<DriverMarketDto> getDriverMarket() {
    return driverMarket;
  }

  public void setDriverMarket(List<DriverMarketDto> driverMarket) {
    this.driverMarket = driverMarket;
  }

  public static class DriverMarketDto {
    private String selectionId;
    private String driverId;
    private String driverName;
    private Double odds;

    public String getSelectionId() {
      return selectionId;
    }

    public void setSelectionId(String selectionId) {
      this.selectionId = selectionId;
    }

    public String getDriverId() {
      return driverId;
    }

    public void setDriverId(String driverId) {
      this.driverId = driverId;
    }

    public String getDriverName() {
      return driverName;
    }

    public void setDriverName(String driverName) {
      this.driverName = driverName;
    }

    public Double getOdds() {
      return odds;
    }

    public void setOdds(Double odds) {
      this.odds = odds;
    }
  }
}
