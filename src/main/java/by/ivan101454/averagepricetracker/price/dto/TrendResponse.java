package by.ivan101454.averagepricetracker.price.dto;

import java.time.Instant;

public record TrendResponse(
        String symbol,
        Instant startDate,
        Instant endDate,
        Trend trend
) {
}
