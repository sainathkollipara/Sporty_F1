package com.example.f1bet.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.f1bet")
public class F1BetApplication {
  public static void main(String[] args) {
    SpringApplication.run(F1BetApplication.class, args);
  }
}
