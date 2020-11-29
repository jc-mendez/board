package com.miro.board.util;

import com.miro.board.widget.model.WidgetRequest;

public final class WidgetFactory {

    private WidgetFactory() {
    }

    public static WidgetRequest buildWidgetRequest(Integer zIndex) {
        WidgetRequest widgetRequest = new WidgetRequest();
        widgetRequest.setHeight(10);
        widgetRequest.setWidth(20);
        widgetRequest.setX(30);
        widgetRequest.setY(40);
        widgetRequest.setZ(zIndex);
        return widgetRequest;
    }
}
