package com.miro.board.exception;

public class WidgetNotFoundException extends Exception {
    public WidgetNotFoundException() {
        super("Widget was not found.");
    }
}
