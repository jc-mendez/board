package com.miro.board.widget.repository;

import com.miro.board.widget.model.Widget;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository("SQLRepository")
public interface WidgetRepository extends PagingAndSortingRepository<Widget, Long> {
    @Query("SELECT COALESCE(MAX(w.z), 0) FROM Widget w")
    int getMaxZIndex();

    @Query("SELECT w FROM Widget w where w.z >= ?1 ORDER BY w.z")
    Collection<Widget> getWidgetsFromZIndex(int z);

    @Modifying
    @Query("UPDATE Widget w SET w.z = w.z + 1 WHERE w.id in ?1")
    void increaseZIndex(List<Long> ids);

}
