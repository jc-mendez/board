package com.miro.board.widget;

import com.miro.board.exception.WidgetNotFoundException;
import com.miro.board.widget.model.Widget;
import com.miro.board.widget.model.WidgetRequest;
import com.miro.board.widget.repository.WidgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
public class WidgetService {

    private static final Logger LOG = LoggerFactory.getLogger(WidgetService.class);

    private final WidgetRepository widgetRepository;

    public WidgetService(ApplicationContext context, @Value("${use-sql-repository}") boolean suseSQLRepository) {
        LOG.info("Using {} repository", suseSQLRepository ? "SQL" : "in-memory");

        widgetRepository = (WidgetRepository) context.getBean(suseSQLRepository ? "SQLRepository" : "InMemoryRepository");
    }

    @Transactional
    public synchronized Widget create(WidgetRequest request) {
        Widget widget = convertRequestToWidget(request, getZIndex(request));
        recalculateZIndexes(widget.getZ(), null);
        return widgetRepository.save(widget);
    }

    @Transactional
    public synchronized Widget update(Long id, WidgetRequest request) throws WidgetNotFoundException {
        Widget updatedWidget = convertRequestToWidget(request, getZIndex(request));
        widgetRepository.findById(id).orElseThrow(WidgetNotFoundException::new);
        recalculateZIndexes(updatedWidget.getZ(), id);

        updatedWidget.setId(id);
        updatedWidget.setLastModified(LocalDateTime.now());

        return widgetRepository.save(updatedWidget);
    }

    @Transactional
    public synchronized Widget delete(Long id) throws WidgetNotFoundException {
        Widget widgetToDelete = widgetRepository.findById(id).orElseThrow(WidgetNotFoundException::new);
        widgetRepository.delete(widgetToDelete);
        return widgetToDelete;
    }

    public Widget get(Long id) throws WidgetNotFoundException {
        return widgetRepository.findById(id).orElseThrow(WidgetNotFoundException::new);
    }

    public Page<Widget> getAll(Pageable pageable) {
        return widgetRepository.findAll(pageable);
    }

    private void recalculateZIndexes(int zIndex, Long id) {
        Iterator<Widget> iterator = widgetRepository.getWidgetsFromZIndex(zIndex).iterator();
        List<Long> widgetIdsToIncreaseZIndex = new ArrayList<>();

        boolean checkWidgetAbove = true;

        while (iterator.hasNext() && checkWidgetAbove) {
            Widget widget = iterator.next();

            if (widget.getZ() == zIndex && !widget.getId().equals(id)) {
                widgetIdsToIncreaseZIndex.add(widget.getId());
                zIndex++;
            } else {
                checkWidgetAbove = false;
            }
        }

        if (!widgetIdsToIncreaseZIndex.isEmpty()) {
            widgetRepository.increaseZIndex(widgetIdsToIncreaseZIndex);
        }
    }

    // Move the widget to the foreground or take it from the request if it is present
    private int getZIndex(WidgetRequest request) {
        return request.getZ() == null ? widgetRepository.getMaxZIndex() + 1 : request.getZ();
    }

    private Widget convertRequestToWidget(WidgetRequest request, int zIndex) {
        return Widget.builder()
                .height(request.getHeight())
                .width(request.getWidth())
                .x(request.getX())
                .y(request.getY())
                .z(zIndex)
                .build();
    }
}
