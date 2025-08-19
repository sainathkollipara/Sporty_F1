package com.example.f1bet.ports.out;

import java.util.List;

public interface F1ProviderPort {
  List<ProviderSession> listSessions(ProviderSessionFilter filter);

  List<ProviderDriver> listDriversForSession(String sessionId);
}
