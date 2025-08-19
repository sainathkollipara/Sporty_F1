package com.example.f1bet.infrastructure.web.mapper;

import com.example.f1bet.domain.entity.Bet;
import com.example.f1bet.domain.entity.Event;
import com.example.f1bet.domain.entity.User;
import com.example.f1bet.domain.vo.Money;
import com.example.f1bet.infrastructure.web.dto.*;
import java.util.List;
import java.util.stream.Collectors;

public final class DomainWebMappers {
  private DomainWebMappers() {}

  public static EventResponse toEventResponse(Event event) {
    EventResponse dto = new EventResponse();
    dto.setId(event.getId().toString());
    dto.setName(event.getName());
    dto.setSessionType(event.getSessionType().name());
    dto.setCountry(event.getCountry());
    dto.setYear(event.getYear());
    dto.setStartTime(event.getStartTime());
    dto.setDriverMarket(
        event.getMarket().getSelections().stream()
            .map(
                sel -> {
                  EventResponse.DriverMarketDto d = new EventResponse.DriverMarketDto();
                  d.setSelectionId(sel.getId().toString());
                  d.setDriverId(sel.getDriverId());
                  d.setDriverName(sel.getDriverName());
                  d.setOdds(sel.getOdds().getValue().doubleValue());
                  return d;
                })
            .collect(Collectors.toList()));
    return dto;
  }

  public static ListEventsResponse toListEventsResponse(
      List<Event> events, Integer page, Integer size, Integer total) {
    ListEventsResponse dto = new ListEventsResponse();
    dto.setItems(
        events.stream().map(DomainWebMappers::toEventResponse).collect(Collectors.toList()));
    dto.setPage(page);
    dto.setSize(size);
    dto.setTotal(total);
    return dto;
  }

  public static BetResponse toBetResponse(Bet bet) {
    BetResponse dto = new BetResponse();
    dto.setBetId(bet.getId().toString());
    dto.setStatus(bet.getStatus().name());
    dto.setStake(bet.getStake().getAmount());
    dto.setCapturedOdds(bet.getCapturedOdds().getValue().doubleValue());
    dto.setEventId(bet.getEventId().toString());
    dto.setSelectionId(bet.getSelectionId().toString());
    return dto;
  }

  public static UserBalanceResponse toUserBalanceResponse(User user) {
    UserBalanceResponse dto = new UserBalanceResponse();
    dto.setUserId(user.getId().toString());
    dto.setBalance(user.getBalance().getAmount());
    return dto;
  }

  public static Money toMoney(PlaceBetRequest req) {
    return Money.stake(req.getCurrency(), req.getStakeAmount());
  }
}
