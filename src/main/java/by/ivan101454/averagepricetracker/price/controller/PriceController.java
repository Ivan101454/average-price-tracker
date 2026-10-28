package by.ivan101454.averagepricetracker.price.controller;

import by.ivan101454.averagepricetracker.price.dto.AveragePriceResponse;
import by.ivan101454.averagepricetracker.price.dto.PricePageResponse;
import by.ivan101454.averagepricetracker.price.dto.PriceResponse;
import by.ivan101454.averagepricetracker.price.dto.TrendResponse;
import by.ivan101454.averagepricetracker.price.service.PriceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/prices")
public class PriceController {

    private final PriceService priceService;
    private final String defaultSymbol;

    public PriceController(@Value("${binance.default-symbol:BNBUSDT}") String defaultSymbol,
                           PriceService priceService) {
        this.defaultSymbol = defaultSymbol;
        this.priceService = priceService;
    }

    @GetMapping("/latest")
    public PriceResponse getLatestPrice(@RequestParam(required = false) String symbol) {
        return priceService.getLatestPrice(symbol == null ? defaultSymbol : symbol);
    }

    @GetMapping("/average")
    public AveragePriceResponse getAveragePrice(@RequestParam(required = false) String symbol,
                                                @RequestParam Instant from,
                                                @RequestParam Instant to) {
        return priceService.getAveragePrice(symbol == null ? defaultSymbol : symbol, from, to);
    }

    @GetMapping
    public PricePageResponse getPriceHistory(@RequestParam(required = false) String symbol,
                                             @RequestParam Instant from,
                                             @RequestParam Instant to,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return priceService.getPriceHistory(symbol == null ? defaultSymbol : symbol, from, to, page, size);
    }

    @GetMapping("/trend")
    public TrendResponse getTrend(@RequestParam(required = false) String symbol,
                                  @RequestParam Instant from,
                                  @RequestParam Instant to) {
        return priceService.getTrend(symbol == null ? defaultSymbol : symbol, from, to);
    }
}
