package com.miro.board.widget.repository;

import com.miro.board.exception.NotFoundException;
import com.miro.board.widget.model.Widget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


/**
 * In-memory implementation of WidgetRepository. A cache is used to allow concurrent reads to the
 * set of widgets without being affected by updating the original set. The cache is refreshed after
 * a write operation to the set.
 * */
@Repository("InMemoryRepository")
public class WidgetInMemoryRepository implements WidgetRepository {

    private final NavigableSet<Widget> widgets = new TreeSet<>(Comparator.comparing(Widget::getZ));
    private Set<Widget> cache = Collections.emptySet();

    private long lastId = 1;

    @Override
    public Optional<Widget> findById(Long id) {
        return findWidgetInCache(id);
    }

    @Override
    public Page<Widget> findAll(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        if (cache.size() < offset) {
            return new PageImpl<>(Collections.emptyList(), pageable, cache.size());
        }

        int lastPageItem = Math.min(cache.size(), offset + pageSize);

        List<Widget> widgetSublist = new ArrayList<>(cache).subList(offset, lastPageItem);

        return new PageImpl<>(widgetSublist, pageable, cache.size());
    }

    @Override
    public int getMaxZIndex() {
        if (widgets.isEmpty()) {
            return 0;
        }

        return widgets.last().getZ();
    }

    @Override
    public Widget save(Widget widget) {
        // Create new widget if id is not present, otherwise update existing one
        if (widget.getId() == null) {
            widget.setId(getNextId());
        } else {
            Widget widgetToUpdate = findWidget(widget.getId()).orElseThrow(() -> new NotFoundException("Widget was not found."));
            widgets.remove(widgetToUpdate);
        }

        widgets.add(widget);
        updateCache();
        return widget;
    }

    @Override
    public void delete(Widget widget) {
        widgets.remove(widget);
        updateCache();
    }

    @Override
    public Collection<Widget> getWidgetsFromZIndex(int zIndex) {
        return widgets.tailSet(Widget.builder().z(zIndex).build(), true);
    }

    @Override
    public void increaseZIndex(List<Long> widgetIds) {
        if (widgetIds.isEmpty()) {
            return;
        }

        List<Widget> widgetsToUpdate = widgets.stream().filter(widget -> widgetIds.contains(widget.getId())).collect(Collectors.toList());

        List<Widget> updatedWidgets = widgetsToUpdate
                .stream()
                .map(widget -> Widget.builder()
                        .id(widget.getId())
                        .lastModified(widget.getLastModified())
                        .width(widget.getWidth())
                        .height(widget.getHeight())
                        .x(widget.getX())
                        .y(widget.getY())
                        .z(widget.getZ() + 1)
                        .build())
                .collect(Collectors.toList());

        widgets.removeAll(widgetsToUpdate);
        widgets.addAll(updatedWidgets);
    }

    private synchronized long getNextId() {
        return lastId++;
    }

    private Optional<Widget> findWidget(Long id) {
        return findWidget(widgets, id);
    }

    private Optional<Widget> findWidgetInCache(Long id) {
        return findWidget(cache, id);
    }

    private Optional<Widget> findWidget(Collection<Widget> collection, Long id) {
        return collection.stream().filter(widget -> widget.getId().equals(id)).findAny();
    }

    private void updateCache() {
        cache = new TreeSet<>(widgets);
    }

    @Override
    public Iterable<Widget> findAll(Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends Widget> Iterable<S> saveAll(Iterable<S> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsById(Long aLong) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Widget> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Widget> findAllById(Iterable<Long> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteById(Long aLong) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(Iterable<? extends Widget> iterable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }
}
