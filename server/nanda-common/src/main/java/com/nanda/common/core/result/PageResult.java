package com.nanda.common.core.result;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class PageResult<T> {

    private List<T> items;
    private int page;
    private int size;
    private long total;

    public static <T> PageResult<T> of(List<T> items, int page, int size, long total) {
        PageResult<T> r = new PageResult<T>();
        r.setItems(items == null ? Collections.<T>emptyList() : items);
        r.setPage(page);
        r.setSize(size);
        r.setTotal(total);
        return r;
    }
}
