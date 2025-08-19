package com.example.f1bet.infrastructure.web.controller;

import com.example.f1bet.application.service.ListEventsService;
import com.example.f1bet.application.service.RecordOutcomeService;
import com.example.f1bet.infrastructure.web.dto.ListEventsResponse;
import com.example.f1bet.infrastructure.web.dto.RecordOutcomeRequest;
import com.example.f1bet.ports.out.ProviderSessionFilter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
  private final ListEventsService listEventsService;
  private final RecordOutcomeService recordOutcomeService;

  public EventController(
      ListEventsService listEventsService, RecordOutcomeService recordOutcomeService) {
    this.listEventsService = listEventsService;
    this.recordOutcomeService = recordOutcomeService;
  }

  @GetMapping
  public ResponseEntity<ListEventsResponse> listEvents(
      @RequestParam(required = false) String sessionType,
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) String country,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    ProviderSessionFilter filter =
        new ProviderSessionFilter(
            sessionType != null
                ? com.example.f1bet.domain.enums.SessionType.valueOf(sessionType)
                : null,
            year,
            country);
    // Pagination is not implemented in service, so just pass all params
    ListEventsResponse response = listEventsService.list(filter, page, size);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{eventId}/outcome")
  public ResponseEntity<Void> recordOutcome(
      @PathVariable String eventId, @Valid @RequestBody RecordOutcomeRequest request) {
    recordOutcomeService.record(java.util.UUID.fromString(eventId), request.getWinningDriverId());
    return ResponseEntity.ok().build();
  }
}
