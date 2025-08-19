package com.example.f1bet.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ListEventsResponse {
  @NotNull private List<EventResponse> items;
  private Integer page;
  private Integer size;
  private Integer total;

  public List<EventResponse> getItems() {
    return items;
  }

  public void setItems(List<EventResponse> items) {
    this.items = items;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }
}
