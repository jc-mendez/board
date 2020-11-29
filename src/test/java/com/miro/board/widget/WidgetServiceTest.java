package com.miro.board.widget;

import com.miro.board.exception.WidgetNotFoundException;
import com.miro.board.util.WidgetAssertionUtil;
import com.miro.board.util.WidgetFactory;
import com.miro.board.widget.model.Widget;
import com.miro.board.widget.model.WidgetRequest;
import com.miro.board.widget.repository.WidgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WidgetServiceTest {

    private static final int Z_INDEX = 2;
    private static final long WIDGET_ID = 2;

    private WidgetService widgetService;

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Captor
    private ArgumentCaptor<Widget> widgetArgumentCaptor;

    @BeforeEach
    void beforeAll() {
        given(applicationContext.getBean("InMemoryRepository")).willReturn(widgetRepository);
        widgetService = new WidgetService(applicationContext, false);
    }

    @Test
    void createAWidgetWithoutMovingOtherWidgets() {
        Widget expectedWidget = new Widget();

        given(widgetRepository.getWidgetsFromZIndex(Z_INDEX)).willReturn(Collections.emptyList());
        given(widgetRepository.save(any())).willReturn(expectedWidget);

        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);

        Widget actualWidget = widgetService.create(widgetRequest);

        assertThat(actualWidget, is(expectedWidget));
        verify(widgetRepository).getWidgetsFromZIndex(Z_INDEX);
        verify(widgetRepository).save(widgetArgumentCaptor.capture());
        verify(widgetRepository, times(0)).increaseZIndex(anyList());
        verify(widgetRepository, times(0)).getMaxZIndex();

        Widget savedWidget = widgetArgumentCaptor.getValue();

        WidgetAssertionUtil.assertWidget(savedWidget, widgetRequest, null);
    }

    @Test
    void createAWidgetWithExistentZIndexMovesOtherWidgets() {
        List<Widget> widgetsFromZIndex = Arrays.asList(buildWidget(2L, 2), buildWidget(3L, 3));
        given(widgetRepository.getWidgetsFromZIndex(Z_INDEX)).willReturn(widgetsFromZIndex);

        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);

        widgetService.create(widgetRequest);

        verify(widgetRepository).increaseZIndex(Arrays.asList(2L, 3L));
    }

    @Test
    void createAWidgetWithoutZIndexMovesToTheForeground() {
        int maxZIndex = 5;
        int nextZIndex = maxZIndex + 1;

        given(widgetRepository.getWidgetsFromZIndex(nextZIndex)).willReturn(Collections.emptyList());
        given(widgetRepository.getMaxZIndex()).willReturn(maxZIndex);

        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);
        widgetRequest.setZ(null);

        widgetService.create(widgetRequest);

        verify(widgetRepository).save(widgetArgumentCaptor.capture());

        Widget savedWidget = widgetArgumentCaptor.getValue();

        assertThat(savedWidget, is(notNullValue()));
        assertThat(savedWidget.getZ(), is(nextZIndex));
    }

    @Test
    void updateAWidgetAndItIsFound() throws WidgetNotFoundException {
        Widget expectedWidget = new Widget();

        given(widgetRepository.findById(WIDGET_ID)).willReturn(Optional.of(new Widget()));
        given(widgetRepository.getWidgetsFromZIndex(Z_INDEX)).willReturn(Collections.emptyList());
        given(widgetRepository.save(any())).willReturn(expectedWidget);

        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);

        Widget actualWidget = widgetService.update(WIDGET_ID, widgetRequest);

        assertThat(actualWidget, is(expectedWidget));
        verify(widgetRepository).getWidgetsFromZIndex(Z_INDEX);
        verify(widgetRepository).save(widgetArgumentCaptor.capture());

        Widget savedWidget = widgetArgumentCaptor.getValue();

        WidgetAssertionUtil.assertWidget(savedWidget, widgetRequest, WIDGET_ID);
    }

    @Test
    void updateAWidgetAndItIsNotFound() {
        given(widgetRepository.findById(WIDGET_ID)).willReturn(Optional.empty());

        WidgetRequest widgetRequest = WidgetFactory.buildWidgetRequest(Z_INDEX);

        assertThrows(
                WidgetNotFoundException.class,
                () -> widgetService.update(WIDGET_ID, widgetRequest),
                "WidgetNotFoundException was expected"
        );
    }

    @Test
    void deleteAWidgetAndItIsFound() throws WidgetNotFoundException {
        Widget expectedWidget = new Widget();

        given(widgetRepository.findById(WIDGET_ID)).willReturn(Optional.of(expectedWidget));

        Widget actualWidget = widgetService.delete(WIDGET_ID);

        assertThat(actualWidget, is(expectedWidget));
        verify(widgetRepository).delete(expectedWidget);
    }

    @Test
    void deleteAWidgetAndItIsNotFound() {
        given(widgetRepository.findById(WIDGET_ID)).willReturn(Optional.empty());

        assertThrows(
                WidgetNotFoundException.class,
                () -> widgetService.delete(WIDGET_ID),
                "WidgetNotFoundException was expected"
        );
    }

    @Test
    void getAWidgetAndItIsFound() throws WidgetNotFoundException {
        Widget expectedWidget = new Widget();

        given(widgetRepository.findById(WIDGET_ID)).willReturn(Optional.of(expectedWidget));

        Widget actualWidget = widgetService.get(WIDGET_ID);

        assertThat(actualWidget, is(expectedWidget));
    }

    @Test
    void getAWidgetAndItIsNotFound() {
        given(widgetRepository.findById(WIDGET_ID)).willReturn(Optional.empty());

        assertThrows(
                WidgetNotFoundException.class,
                () -> widgetService.get(WIDGET_ID),
                "WidgetNotFoundException was expected"
        );
    }

    @Test
    void getAllWidgets() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Widget> expectedWidgets = new PageImpl<>(new ArrayList<>(), pageable, 20);

        given(widgetRepository.findAll(pageable)).willReturn(expectedWidgets);

        Page<Widget> actualWidgets = widgetService.getAll(pageable);

        assertThat(actualWidgets, is(expectedWidgets));
    }

    private Widget buildWidget(long id, int zIndex) {
        return Widget.builder()
                .id(id)
                .z(zIndex)
                .build();
    }
}