package co.com.bancolombia.model.common;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class Page<T> {
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private String sortBy;
    private SortDirection sortDirection;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
}
