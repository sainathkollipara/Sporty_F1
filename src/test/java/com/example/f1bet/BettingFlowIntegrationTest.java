package com.example.f1bet;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.f1bet.infrastructure.web.dto.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    classes = com.example.f1bet.bootstrap.F1BetApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BettingFlowIntegrationTest {

  @Autowired private WebTestClient webTestClient;

  @Test
  void happyPath_bet_and_settle() {
    // Generate valid UUIDs for the test
    String userId = UUID.randomUUID().toString();

    // 1. List events
    ListEventsResponse events =
        webTestClient
            .get()
            .uri("/api/v1/events?year=2025&country=Australia&sessionType=RACE")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ListEventsResponse.class)
            .returnResult()
            .getResponseBody();

    assert events != null;
    EventResponse event = events.getItems().get(0);
    String eventId = event.getId();
    EventResponse.DriverMarketDto selection = event.getDriverMarket().get(0);

    // 2. Place bet (twice with same Idempotency-Key)
    PlaceBetRequest betRequest = new PlaceBetRequest();
    betRequest.setUserId(userId);
    betRequest.setEventId(eventId);
    betRequest.setSelectionId(selection.getSelectionId());
    betRequest.setStakeAmount(BigDecimal.valueOf(10));
    betRequest.setCurrency("EUR");

    String idempotencyKey = "test-key-123";
    // First bet should succeed
    webTestClient
        .post()
        .uri("/api/v1/bets")
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(betRequest)
        .exchange()
        .expectStatus()
        .isOk();

    // Second bet with same idempotency key should also succeed (idempotent)
    webTestClient
        .post()
        .uri("/api/v1/bets")
        .header("Idempotency-Key", idempotencyKey)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(betRequest)
        .exchange()
        .expectStatus()
        .isOk();

    // 3. Settle outcome
    RecordOutcomeRequest outcome = new RecordOutcomeRequest();
    outcome.setWinningDriverId(selection.getDriverId());
    webTestClient
        .post()
        .uri("/api/v1/events/" + eventId + "/outcome")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(outcome)
        .exchange()
        .expectStatus()
        .isOk();

    // 4. Check user balance increased
    UserBalanceResponse balance =
        webTestClient
            .get()
            .uri("/api/v1/users/" + userId + "/balance")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(UserBalanceResponse.class)
            .returnResult()
            .getResponseBody();

    assert balance != null;
    // Balance should be greater than initial balance (100.00) minus bet stake (10.00) plus winnings
    // Initial: 100.00, After bet: 90.00, After winning: 90.00 + (10.00 * odds)
    assertThat(balance.getBalance()).isGreaterThan(new BigDecimal("90.00"));
  }

  @Test
  void negative_insufficient_balance() {
    // First get a valid event and selection
    ListEventsResponse events =
        webTestClient
            .get()
            .uri("/api/v1/events?year=2025&country=Australia&sessionType=RACE")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ListEventsResponse.class)
            .returnResult()
            .getResponseBody();

    assert events != null && !events.getItems().isEmpty();
    EventResponse event = events.getItems().get(0);
    String eventId = event.getId();
    EventResponse.DriverMarketDto selection = event.getDriverMarket().get(0);

    // Try to place a bet with a stake greater than user balance
    PlaceBetRequest betRequest = new PlaceBetRequest();
    betRequest.setUserId(UUID.randomUUID().toString()); // Valid UUID
    betRequest.setEventId(eventId); // Valid event ID
    betRequest.setSelectionId(selection.getSelectionId()); // Valid selection ID
    betRequest.setStakeAmount(BigDecimal.valueOf(1000000)); // Excessive amount
    betRequest.setCurrency("EUR");

    webTestClient
        .post()
        .uri("/api/v1/bets")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(betRequest)
        .exchange()
        .expectStatus()
        .isEqualTo(422);
  }

  @Test
  void negative_invalid_selection() {
    // First get a valid event
    ListEventsResponse events =
        webTestClient
            .get()
            .uri("/api/v1/events?year=2025&country=Australia&sessionType=RACE")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(ListEventsResponse.class)
            .returnResult()
            .getResponseBody();

    assert events != null && !events.getItems().isEmpty();
    EventResponse event = events.getItems().get(0);
    String eventId = event.getId();

    // Try to place a bet with an invalid selection for the event
    PlaceBetRequest betRequest = new PlaceBetRequest();
    betRequest.setUserId(UUID.randomUUID().toString()); // Valid UUID
    betRequest.setEventId(eventId); // Valid event ID
    betRequest.setSelectionId(
        UUID.randomUUID().toString()); // Invalid selection ID (not part of this event)
    betRequest.setStakeAmount(BigDecimal.valueOf(10));
    betRequest.setCurrency("EUR");

    webTestClient
        .post()
        .uri("/api/v1/bets")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(betRequest)
        .exchange()
        .expectStatus()
        .isEqualTo(422);
  }
}
