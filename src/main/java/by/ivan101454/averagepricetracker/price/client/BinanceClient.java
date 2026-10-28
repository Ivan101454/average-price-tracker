package by.ivan101454.averagepricetracker.price.client;

import by.ivan101454.averagepricetracker.price.dto.BinanceAvgPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "binance-client", url = "${binance.api.url:https://api.binance.com}")
public interface BinanceClient {

    @GetMapping("/api/v3/avgPrice")
    BinanceAvgPriceResponse getAvgPrice(@RequestParam("symbol") String symbol);
}
