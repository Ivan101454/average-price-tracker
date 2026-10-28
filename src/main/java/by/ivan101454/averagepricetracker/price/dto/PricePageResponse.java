package by.ivan101454.averagepricetracker.price.dto;

import java.util.List;

public record PricePageResponse(
        List<PriceResponse> items,
        int page,
        int size,
        long totalElements,
        boolean hasNext
) {
}
