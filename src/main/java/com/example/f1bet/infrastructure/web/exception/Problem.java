package com.example.f1bet.infrastructure.web.exception;

public class Problem {
  private int status;
  private String title;
  private String detail;

  public Problem(int status, String title, String detail) {
    this.status = status;
    this.title = title;
    this.detail = detail;
  }

  public static Problem of(int status, String title, String detail) {
    return new Problem(status, title, detail);
  }

  public int getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }
}
