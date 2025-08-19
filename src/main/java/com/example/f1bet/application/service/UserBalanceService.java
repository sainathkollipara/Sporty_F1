package com.example.f1bet.application.service;

import com.example.f1bet.domain.entity.User;
import com.example.f1bet.infrastructure.web.dto.UserBalanceResponse;
import com.example.f1bet.ports.out.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserBalanceService {
  private final UserRepository userRepository;

  public UserBalanceService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserBalanceResponse getBalance(String userId) {
    UUID uuid = UUID.fromString(userId);
    User user =
        userRepository.findById(uuid).orElseGet(() -> userRepository.save(User.create(uuid)));
    UserBalanceResponse response = new UserBalanceResponse();
    response.setUserId(user.getId().toString());
    response.setBalance(user.getBalance().getAmount());
    return response;
  }
}
