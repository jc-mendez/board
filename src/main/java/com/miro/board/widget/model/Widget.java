package com.miro.board.widget.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Widget {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime lastModified;
    private int width;
    private int height;
    private int x;
    private int y;
    private int z;
}
