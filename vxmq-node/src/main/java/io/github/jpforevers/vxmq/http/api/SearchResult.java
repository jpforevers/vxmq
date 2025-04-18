package io.github.jpforevers.vxmq.http.api;

import java.util.List;

public class SearchResult<T> {

    private List<T> data;
    private CursorPagination pagination; // 分页时存在，否则为null

    // 全量数据构造函数
    public SearchResult(List<T> data) {
        this.data = data;
        this.pagination = null;
    }

    // 分页数据构造函数
    public SearchResult(List<T> data, CursorPagination pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    // Getters
    public List<T> getData() { return data; }
    public CursorPagination getPagination() { return pagination; }

}
