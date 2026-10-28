package by.ivan101454.averagepricetracker.price.scheduler;

import by.ivan101454.averagepricetracker.price.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceScheduler.class);

    private final PriceService priceService;

    public PriceScheduler(PriceService priceService) {
        this.priceService = priceService;
    }

    @Scheduled(fixedDelayString = "${binance.poll-interval:PT30S}",
            initialDelayString = "${binance.price-fetch-initial-delay-ms:PT5S}")
    public void fetchAndSaveAveragePrice() {
        try {
            priceService.fetchAndSaveAveragePrice();
            log.info("Successfully fetched and saved average price");
        } catch (Exception e) {
            log.error("Failed to fetch average price from Binance", e);
        }
    }
}
