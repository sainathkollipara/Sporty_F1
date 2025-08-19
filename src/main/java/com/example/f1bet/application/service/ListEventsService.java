package com.example.f1bet.application.service;

import com.example.f1bet.domain.entity.Event;
import com.example.f1bet.domain.entity.Market;
import com.example.f1bet.domain.entity.Selection;
import com.example.f1bet.domain.enums.SessionType;
import com.example.f1bet.domain.policy.OddsPolicy;
import com.example.f1bet.infrastructure.web.dto.ListEventsResponse;
import com.example.f1bet.infrastructure.web.mapper.DomainWebMappers;
import com.example.f1bet.ports.out.EventRepository;
import com.example.f1bet.ports.out.F1ProviderPort;
import com.example.f1bet.ports.out.ProviderDriver;
import com.example.f1bet.ports.out.ProviderSession;
import com.example.f1bet.ports.out.ProviderSessionFilter;
import com.example.f1bet.ports.out.RandomPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ListEventsService {
  public record EventView(
      UUID id,
      String name,
      SessionType sessionType,
      String country,
      int year,
      List<SelectionView> selections) {}

  public record SelectionView(UUID id, String driverId, String driverName, BigDecimal odds) {}

  private final F1ProviderPort provider;
  private final OddsPolicy oddsPolicy;
  private final RandomPort random;
  private final EventRepository eventRepository;

  public ListEventsService(
      F1ProviderPort provider,
      OddsPolicy oddsPolicy,
      RandomPort random,
      EventRepository eventRepository) {
    this.provider = provider;
    this.oddsPolicy = oddsPolicy;
    this.random = random;
    this.eventRepository = eventRepository;
  }

  public List<EventView> list(ProviderSessionFilter filter) {
    List<ProviderSession> sessions = provider.listSessions(filter);
    List<EventView> result = new ArrayList<>();
    for (ProviderSession s : sessions) {
      List<ProviderDriver> drivers = provider.listDriversForSession(s.id());
      List<Selection> selections = new ArrayList<>();
      List<SelectionView> selectionViews = new ArrayList<>();
      for (ProviderDriver d : drivers) {
        var odds = oddsPolicy.randomFrom(random);
        var sel = new Selection(UUID.randomUUID(), d.id(), d.fullName(), odds);
        selections.add(sel);
        selectionViews.add(new SelectionView(sel.getId(), d.id(), d.fullName(), odds.getDecimal()));
      }
      Event event =
          new Event(
              UUID.fromString(s.id()),
              s.name(),
              s.sessionType(),
              s.country(),
              s.year(),
              new Market(Market.WINNER, selections));
      // Save event to repository so it exists for betting
      eventRepository.save(event);
      result.add(
          new EventView(
              event.getId(),
              event.getName(),
              event.getSessionType(),
              event.getCountry(),
              event.getYear(),
              selectionViews));
    }
    return result;
  }

  // New: simple in-memory pagination returning the web DTO
  public ListEventsResponse list(ProviderSessionFilter filter, Integer page, Integer size) {
    // Build full list of domain Events (same logic as above)
    List<ProviderSession> sessions = provider.listSessions(filter);
    List<Event> events = new ArrayList<>();
    for (ProviderSession s : sessions) {
      UUID eventId = UUID.fromString(s.id());

      // Check if event already exists in repository
      Event event = eventRepository.findById(eventId).orElse(null);
      if (event == null) {
        // Create new event only if it doesn't exist
        List<ProviderDriver> drivers = provider.listDriversForSession(s.id());
        List<Selection> selections = new ArrayList<>();
        for (ProviderDriver d : drivers) {
          var odds = oddsPolicy.randomFrom(random);
          var sel = new Selection(UUID.randomUUID(), d.id(), d.fullName(), odds);
          selections.add(sel);
        }
        event =
            new Event(
                eventId,
                s.name(),
                s.sessionType(),
                s.country(),
                s.year(),
                new Market(Market.WINNER, selections));
        // Save event to repository so it exists for betting
        eventRepository.save(event);
      }
      events.add(event);
    }

    // Defaults
    int defaultPage = 0;
    int defaultSize = 20;
    int p = (page == null || page < 0) ? defaultPage : page;
    int s = (size == null || size <= 0) ? defaultSize : size;

    int total = events.size();
    int fromIndex = p * s;
    List<Event> pageItems;
    if (fromIndex >= total) {
      pageItems = new ArrayList<>();
    } else {
      int toIndex = Math.min(total, fromIndex + s);
      pageItems = events.subList(fromIndex, toIndex);
    }

    return DomainWebMappers.toListEventsResponse(pageItems, p, s, total);
  }
}
