// application/dto/response/PagedResponse.java
package com.serviloc.mission.application.dto.response;

import java.util.List;

public class PagedResponse<T> {

    private boolean success;
    private List<T> data;
    private MetaDto meta;

    public static <T> PagedResponse<T> of(List<T> data, int page, int limit, long total) {
        PagedResponse<T> response = new PagedResponse<>();
        response.success = true;
        response.data = data;
        response.meta = new MetaDto(page, limit, total,
                (int) Math.ceil((double) total / limit));
        return response;
    }

    public boolean isSuccess() { return success; }
    public List<T> getData() { return data; }
    public MetaDto getMeta() { return meta; }

    public static class MetaDto {
        private int page;
        private int limit;
        private long total;
        private int totalPages;

        public MetaDto(int page, int limit, long total, int totalPages) {
            this.page = page;
            this.limit = limit;
            this.total = total;
            this.totalPages = totalPages;
        }

        public int getPage() { return page; }
        public int getLimit() { return limit; }
        public long getTotal() { return total; }
        public int getTotalPages() { return totalPages; }
    }
}