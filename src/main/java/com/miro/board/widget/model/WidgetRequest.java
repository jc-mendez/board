package com.miro.board.widget.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class WidgetRequest {
    @Min(value = 1, message = "Width must be positive")
    @NotNull
    private Integer width;
    @Min(1)
    @NotNull
    private Integer height;
    @NotNull
    private Integer x;
    @NotNull
    private Integer y;
    private Integer z;
}
