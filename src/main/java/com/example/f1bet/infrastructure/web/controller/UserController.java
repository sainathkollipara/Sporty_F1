package com.example.f1bet.infrastructure.web.controller;

import com.example.f1bet.application.service.UserBalanceService;
import com.example.f1bet.infrastructure.web.dto.UserBalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserBalanceService userBalanceService;

  public UserController(UserBalanceService userBalanceService) {
    this.userBalanceService = userBalanceService;
  }

  @GetMapping("/{userId}/balance")
  public ResponseEntity<UserBalanceResponse> getBalance(@PathVariable String userId) {
    UserBalanceResponse response = userBalanceService.getBalance(userId);
    return ResponseEntity.ok(response);
  }
}
