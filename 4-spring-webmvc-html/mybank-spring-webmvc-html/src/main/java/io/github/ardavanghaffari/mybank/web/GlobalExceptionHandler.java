package io.github.ardavanghaffari.mybank.web;

import io.github.ardavanghaffari.mybank.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorDto handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {

        return new ErrorDto(exception.getMessage(),
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(FieldError::getField)
                        .toList());
    }

}
