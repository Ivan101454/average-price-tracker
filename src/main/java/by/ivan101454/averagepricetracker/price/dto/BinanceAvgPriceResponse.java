package by.ivan101454.averagepricetracker.price.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceAvgPriceResponse(
        BigDecimal price,
        long mins
) {
}
