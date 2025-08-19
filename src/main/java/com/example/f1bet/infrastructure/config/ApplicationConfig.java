package com.example.f1bet.infrastructure.config;

import com.example.f1bet.infrastructure.persistence.memory.InMemoryBetRepository;
import com.example.f1bet.infrastructure.persistence.memory.InMemoryEventRepository;
import com.example.f1bet.infrastructure.persistence.memory.InMemoryUserRepository;
import com.example.f1bet.infrastructure.provider.http.HttpF1ProviderAdapter;
import com.example.f1bet.infrastructure.provider.stub.StubF1ProviderAdapter;
import com.example.f1bet.ports.out.BetRepository;
import com.example.f1bet.ports.out.EventRepository;
import com.example.f1bet.ports.out.F1ProviderPort;
import com.example.f1bet.ports.out.RandomPort;
import com.example.f1bet.ports.out.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ApplicationConfig {
  @Bean
  public UserRepository userRepository() {
    return new InMemoryUserRepository();
  }

  @Bean
  public EventRepository eventRepository() {
    return new InMemoryEventRepository();
  }

  @Bean
  public BetRepository betRepository() {
    return new InMemoryBetRepository();
  }

  @Bean
  public RandomPort randomPortBean() {
    return new RandomPort() {
      private final java.util.Random rnd = new java.util.Random();

      @Override
      public int nextInt(int bound) {
        return rnd.nextInt(bound);
      }
    };
  }

  @Bean
  @ConditionalOnProperty(name = "app.provider.mode", havingValue = "http")
  public F1ProviderPort httpF1ProviderAdapter(HttpF1ProviderAdapter httpAdapter) {
    return httpAdapter;
  }

  @Bean
  @Primary
  @ConditionalOnProperty(name = "app.provider.mode", havingValue = "stub", matchIfMissing = true)
  public F1ProviderPort stubF1ProviderAdapter() {
    return new StubF1ProviderAdapter();
  }
}
