package com.miro.board.exception;

public class InvalidPageSizeException extends Exception {
    public InvalidPageSizeException(int limit) {
        super("Page size must be less or equal to " + limit);
    }
}
