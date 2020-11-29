package com.miro.board.util;

import com.miro.board.widget.model.Widget;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WidgetsPage {
    private List<Widget> content;
    private int totalElements;
    private int totalPages;
    private boolean last;
}