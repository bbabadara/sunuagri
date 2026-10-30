package sn.sunagri.shared.api;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> de(List<T> contenu, int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(contenu, page, size, totalElements, totalPages);
    }

    public <R> PageResponse<R> map(Function<T, R> transformation) {
        return new PageResponse<>(content.stream().map(transformation).toList(), page, size, totalElements,
                totalPages);
    }
}
