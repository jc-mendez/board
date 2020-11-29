package com.miro.board.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({WidgetNotFoundException.class, NotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
        return getErrorResponseEntity(ex, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(InvalidPageSizeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPageSize(Exception ex) {
        return getErrorResponseEntity(ex, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        ErrorResponse errorResponse = new ErrorResponse("Validation error");

        if (!CollectionUtils.isEmpty(fieldErrors)) {
            List<ErrorResponse.Error> errors = fieldErrors.stream()
                    .map(fieldError -> new ErrorResponse.Error(fieldError.getField(), fieldError.getDefaultMessage()))
                    .collect(Collectors.toList());
            errorResponse.addErrors(errors);
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ResponseEntity<ErrorResponse> getErrorResponseEntity(Exception ex, HttpStatus httpStatus) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

}
