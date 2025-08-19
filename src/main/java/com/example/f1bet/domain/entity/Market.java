package com.example.f1bet.domain.entity;

import java.util.Collections;
import java.util.List;

public class Market {
  public static final String WINNER = "WINNER";

  private final String type;
  private final List<Selection> selections;

  public Market(String type, List<Selection> selections) {
    this.type = type;
    this.selections = selections == null ? List.of() : List.copyOf(selections);
  }

  public String getType() {
    return type;
  }

  public List<Selection> getSelections() {
    return Collections.unmodifiableList(selections);
  }

  public boolean containsSelectionId(java.util.UUID selectionId) {
    return selections.stream().anyMatch(s -> s.getId().equals(selectionId));
  }
}
