package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler({ValidationException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse constraintViolationHandle(final RuntimeException e) {
        log.error("Bad request : {}", e.getMessage(), e);
        return new ValidationErrorResponse(e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ValidationErrorResponse handle(final Throwable e) {
        log.error("Bad request : {}", e.getMessage(), e);
        return new ValidationErrorResponse("An unexpected error has occurred.");
    }

    private static class ValidationErrorResponse {
        String error;

        public ValidationErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}
