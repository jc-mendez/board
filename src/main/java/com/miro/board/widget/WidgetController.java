package com.miro.board.widget;

import com.miro.board.exception.InvalidPageSizeException;
import com.miro.board.exception.WidgetNotFoundException;
import com.miro.board.widget.model.Widget;
import com.miro.board.widget.model.WidgetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/widgets")
public class WidgetController {

    private static final int MAX_PAGE_SIZE = 500;

    private final WidgetService widgetService;

    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Widget createWidget(@Valid @RequestBody WidgetRequest request) {
        return widgetService.create(request);
    }

    @PutMapping("{id}")
    public Widget update(@PathVariable Long id, @Valid @RequestBody WidgetRequest request) throws WidgetNotFoundException {
        return widgetService.update(id, request);
    }

    @DeleteMapping("{id}")
    public Widget delete(@PathVariable Long id) throws WidgetNotFoundException {
        return widgetService.delete(id);
    }

    @GetMapping("{id}")
    public Widget get(@PathVariable Long id) throws WidgetNotFoundException {
        return widgetService.get(id);
    }

    @GetMapping
    public Page<Widget> getAll(@SortDefault(sort = "z") @PageableDefault Pageable pageable) throws InvalidPageSizeException {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new InvalidPageSizeException(MAX_PAGE_SIZE);
        }

        return widgetService.getAll(pageable);
    }
}
