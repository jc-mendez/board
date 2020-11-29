package com.miro.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorResponse {
    private final String message;
    private final List<Error> errors;

    public ErrorResponse(String message) {
        this.message = message;
        errors = new ArrayList<>();
    }

    public void addErrors(List<Error> errors) {
        this.errors.addAll(errors);
    }

    @Getter
    @AllArgsConstructor
    public static class Error {
        private final String field;
        private final String message;
    }
}
