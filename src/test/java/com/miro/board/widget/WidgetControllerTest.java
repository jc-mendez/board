package com.miro.board.widget;

import com.miro.board.util.WidgetAssertionUtil;
import com.miro.board.util.WidgetFactory;
import com.miro.board.util.WidgetsPage;
import com.miro.board.widget.model.Widget;
import com.miro.board.widget.model.WidgetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"use-sql-repository=true"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WidgetControllerTest {

    private static final int Z_INDEX = 2;
    private static final long FIRST_ID = 1L;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAWidget() {
        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);

        ResponseEntity<Widget> response = createWidget(widgetRequest);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        Widget widget = response.getBody();
        WidgetAssertionUtil.assertWidget(widget, widgetRequest, FIRST_ID);
    }

    @Test
    void createAWidgetWithoutZIndexAssignsMaxZIndex() {
        createWidget(WidgetFactory.buildWidgetRequest(Z_INDEX));
        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(null);

        ResponseEntity<Widget> response = createWidget(widgetRequest);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        Widget widget = response.getBody();
        assertThat(widget, is(notNullValue()));
        assertThat(widget.getZ(), is(Z_INDEX + 1));
    }

    @Test
    void createAWidgetWithInvalidRequest() {
        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);
        widgetRequest.setHeight(-1);

        ResponseEntity<Widget> response = createWidget(widgetRequest);

        assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void updateAWidget() {
        createWidget(WidgetFactory.buildWidgetRequest(Z_INDEX));

        WidgetRequest widgetRequest = new WidgetRequest();

        widgetRequest.setHeight(5);
        widgetRequest.setWidth(4);
        widgetRequest.setX(3);
        widgetRequest.setY(2);
        widgetRequest.setZ(1);

        ResponseEntity<Widget> response = updateWidget(FIRST_ID, widgetRequest);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        Widget widget = response.getBody();
        WidgetAssertionUtil.assertWidget(widget, widgetRequest, FIRST_ID);

        assertThat(widget, is(notNullValue()));
        assertThat(widget.getLastModified(), is(notNullValue()));
    }

    @Test
    void updateAWidgetMovesOtherWidgetsToTheTop() {
        createWidget(WidgetFactory.buildWidgetRequest(1));
        createWidget(WidgetFactory.buildWidgetRequest(2));
        createWidget(WidgetFactory.buildWidgetRequest(3));
        createWidget(WidgetFactory.buildWidgetRequest(5));
        createWidget(WidgetFactory.buildWidgetRequest(2));

        ResponseEntity<WidgetsPage> response = getWidgets(PageRequest.of(0, 10));

        WidgetsPage widgetsPage = response.getBody();
        assertThat(widgetsPage, is(notNullValue()));

        List<Widget> widgets = widgetsPage.getContent();

        assertThat(widgets.get(0).getId(), is(1L));
        assertThat(widgets.get(0).getZ(), is(1));

        assertThat(widgets.get(1).getId(), is(5L));
        assertThat(widgets.get(1).getZ(), is(2));

        assertThat(widgets.get(2).getId(), is(2L));
        assertThat(widgets.get(2).getZ(), is(3));

        assertThat(widgets.get(3).getId(), is(3L));
        assertThat(widgets.get(3).getZ(), is(4));

        assertThat(widgets.get(4).getId(), is(4L));
        assertThat(widgets.get(4).getZ(), is(5));
    }

    @Test
    void getAWidget() {
        createWidget(WidgetFactory.buildWidgetRequest(Z_INDEX));
        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);

        ResponseEntity<Widget> response = getWidget(FIRST_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        Widget widget = response.getBody();
        WidgetAssertionUtil.assertWidget(widget, widgetRequest, FIRST_ID);
    }

    @Test
    void getANotFoundWidget() {
        ResponseEntity<Widget> response = getWidget(FIRST_ID);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteAWidget() {
        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);
        createWidget(widgetRequest);

        ResponseEntity<Widget> response = deleteWidget(FIRST_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        WidgetAssertionUtil.assertWidget(response.getBody(), widgetRequest, FIRST_ID);

        ResponseEntity<Widget> getResponse = getWidget(FIRST_ID);
        assertThat(getResponse.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void getAllWidgets() {
        createWidget(WidgetFactory.buildWidgetRequest(Z_INDEX));
        createWidget(WidgetFactory.buildWidgetRequest(Z_INDEX));
        createWidget(WidgetFactory.buildWidgetRequest(Z_INDEX));

        PageRequest pageRequest = PageRequest.of(0, 10);
        ResponseEntity<WidgetsPage> response = getWidgets(pageRequest);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        WidgetsPage widgetsPage = response.getBody();

        assertThat(widgetsPage, is(notNullValue()));
        assertThat(widgetsPage.getContent(), is(notNullValue()));
        assertThat(widgetsPage.getContent().size(), is(3));
        assertThat(widgetsPage.getTotalElements(), is(3));
        assertThat(widgetsPage.getTotalPages(), is(1));
        assertThat(widgetsPage.isLast(), is(true));

        PageRequest smallPageSizeRequest = PageRequest.of(0, 2);
        ResponseEntity<WidgetsPage> smallPageResponse = getWidgets(smallPageSizeRequest);

        WidgetsPage smallPageWidget = smallPageResponse.getBody();

        assertThat(smallPageWidget, is(notNullValue()));
        assertThat(smallPageWidget.getContent(), is(notNullValue()));
        assertThat(smallPageWidget.getContent().size(), is(2));
        assertThat(smallPageWidget.getTotalElements(), is(3));
        assertThat(smallPageWidget.getTotalPages(), is(2));
        assertThat(smallPageWidget.isLast(), is(false));
    }

    @Test
    public void getAllWidgetsWithInvalidPageSize() {
        PageRequest pageRequest = PageRequest.of(0, 501);

        ResponseEntity<WidgetsPage> response = getWidgets(pageRequest);

        assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    private ResponseEntity<Widget> createWidget(WidgetRequest widgetRequest) {
        return restTemplate.postForEntity(getUrl("widgets"), widgetRequest, Widget.class);
    }

    private ResponseEntity<Widget> getWidget(Long id) {
        return restTemplate.getForEntity(getUrl("widgets/" + id), Widget.class);
    }

    private ResponseEntity<Widget> updateWidget(Long id, WidgetRequest widgetRequest) {
        return restTemplate.exchange(getUrl("widgets/" + id), HttpMethod.PUT, new HttpEntity<>(widgetRequest), Widget.class);
    }

    private ResponseEntity<Widget> deleteWidget(Long id) {
        return restTemplate.exchange(getUrl("widgets/" + id), HttpMethod.DELETE, null, Widget.class);
    }

    private ResponseEntity<WidgetsPage> getWidgets(Pageable pageable) {
        String path = String.format("widgets?page=%d&size=%d", pageable.getPageNumber(), pageable.getPageSize());

        return restTemplate.getForEntity(getUrl(path), WidgetsPage.class);
    }

    private String getUrl(String path) {
        return String.format("http://localhost:%d/%s", port, path);
    }
}