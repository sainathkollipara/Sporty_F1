package com.example.f1bet.infrastructure.web.exception;

import com.example.f1bet.domain.exception.IllegalEventStateException;
import com.example.f1bet.domain.exception.InsufficientBalanceException;
import com.example.f1bet.domain.exception.InvalidBetException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Problem> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Problem.of(400, "Validation failed", ex.getMessage()));
  }

  @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
  public ResponseEntity<Problem> handleBadRequest(Exception ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Problem.of(400, "Bad request", ex.getMessage()));
  }

  @ExceptionHandler({
    InvalidBetException.class,
    InsufficientBalanceException.class,
    IllegalEventStateException.class
  })
  public ResponseEntity<Problem> handleDomainValidation(Exception ex, WebRequest request) {
    return ResponseEntity.status(422)
        .body(Problem.of(422, "Domain validation error", ex.getMessage()));
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<Problem> handleOptimisticLock(
      OptimisticLockingFailureException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Problem.of(409, "Optimistic lock error", ex.getMessage()));
  }

  @ExceptionHandler({org.springframework.web.server.ResponseStatusException.class})
  public ResponseEntity<Problem> handleNotFound(
      org.springframework.web.server.ResponseStatusException ex, WebRequest request) {
    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Problem.of(404, "Not found", ex.getMessage()));
    }
    return ResponseEntity.status(ex.getStatusCode().value())
        .body(Problem.of(ex.getStatusCode().value(), ex.getReason(), ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleOther(Exception ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Problem.of(500, "Internal error", ex.getMessage()));
  }
}
