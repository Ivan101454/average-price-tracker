package by.ivan101454.averagepricetracker.price.dto;

import java.math.BigDecimal;

public record AveragePriceResponse(
        BigDecimal averagePrice
) {
}
