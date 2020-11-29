package com.miro.board.util;

import com.miro.board.widget.model.Widget;
import com.miro.board.widget.model.WidgetRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public final class WidgetAssertionUtil {

    private WidgetAssertionUtil() {
    }

    public static void assertWidget(Widget widget, WidgetRequest widgetRequest, Long expectedId) {
        assertThat(widget, is(notNullValue()));
        assertThat(widget.getId(), is(expectedId));
        assertThat(widget.getHeight(), is(widgetRequest.getHeight()));
        assertThat(widget.getWidth(), is(widgetRequest.getWidth()));
        assertThat(widget.getX(), is(widgetRequest.getX()));
        assertThat(widget.getY(), is(widgetRequest.getY()));
        assertThat(widget.getZ(), is(widgetRequest.getZ()));
    }
}
