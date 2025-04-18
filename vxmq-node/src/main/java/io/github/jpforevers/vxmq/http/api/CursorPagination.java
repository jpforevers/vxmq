package io.github.jpforevers.vxmq.http.api;

public class CursorPagination {

    public static final String FIELD_NAME_NEXT_CURSOR = "nextCursor";
    public static final String FIELD_NAME_SIZE = "size";

    private Long totalItems;   // 仅第一页返回
    private String nextCursor; // 存在下一页时返回
    private Integer size;      // 分页大小时存在

    // 全量数据无分页元数据，故不需要构造函数
    // 分页构造函数（第一页）
    public CursorPagination(Long totalItems, String nextCursor, Integer size) {
        this.totalItems = totalItems;
        this.nextCursor = nextCursor;
        this.size = size;
    }

    // 分页构造函数（后续页）
    public CursorPagination(String nextCursor, Integer size) {
        this.nextCursor = nextCursor;
        this.size = size;
    }

    // Getters
    public Long getTotalItems() { return totalItems; }
    public String getNextCursor() { return nextCursor; }
    public Integer getSize() { return size; }
    
}
