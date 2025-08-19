package com.example.f1bet.infrastructure.web.controller;

import com.example.f1bet.application.service.PlaceBetService;
import com.example.f1bet.domain.entity.Bet;
import com.example.f1bet.infrastructure.web.dto.BetResponse;
import com.example.f1bet.infrastructure.web.dto.PlaceBetRequest;
import com.example.f1bet.infrastructure.web.mapper.DomainWebMappers;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bets")
public class BetController {
  private final PlaceBetService placeBetService;

  public BetController(PlaceBetService placeBetService) {
    this.placeBetService = placeBetService;
  }

  @PostMapping
  public ResponseEntity<BetResponse> placeBet(
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @Valid @RequestBody PlaceBetRequest request) {
    // Build command
    PlaceBetService.Command cmd =
        new PlaceBetService.Command(
            UUID.fromString(request.getUserId()),
            UUID.fromString(request.getEventId()),
            UUID.fromString(request.getSelectionId()),
            request.getStakeAmount(),
            request.getCurrency(),
            idempotencyKey);
    Bet bet = placeBetService.place(cmd);
    BetResponse response = DomainWebMappers.toBetResponse(bet);
    return ResponseEntity.ok(response);
  }
}
