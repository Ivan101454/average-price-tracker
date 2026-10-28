package by.ivan101454.averagepricetracker.price.service;

import by.ivan101454.averagepricetracker.price.dto.AveragePriceResponse;
import by.ivan101454.averagepricetracker.price.dto.PricePageResponse;
import by.ivan101454.averagepricetracker.price.dto.PriceResponse;
import by.ivan101454.averagepricetracker.price.dto.TrendResponse;

import java.time.Instant;

/**
 * Service for working with average price data:
 * provides methods for retrieving the latest price, average price, history and trend,
 * as well as for fetching and saving data from Binance.
 */
public interface PriceService {

    /**
     * Returns the latest saved price record for the specified symbol.
     *
     * @param symbol trading pair symbol (e.g., BTCUSDT)
     * @return the latest saved price record
     */
    PriceResponse getLatestPrice(String symbol);

    /**
     * Returns the average price for the specified period and symbol.
     *
     * @param symbol trading pair symbol
     * @param from   start of the period
     * @param to     end of the period
     * @return average price for the period
     */
    AveragePriceResponse getAveragePrice(String symbol, Instant from, Instant to);

    /**
     * Returns the price history for the specified period with pagination for the given symbol.
     *
     * @param symbol trading pair symbol
     * @param from   start of the period
     * @param to     end of the period
     * @param page   page number (starting from 0)
     * @param size   page size
     * @return page of price history
     */
    PricePageResponse getPriceHistory(String symbol, Instant from, Instant to, int page, int size);

    /**
     * Returns the price trend for the specified period and symbol.
     *
     * @param symbol trading pair symbol
     * @param from   start of the period
     * @param to     end of the period
     * @return trend for the period
     */
    TrendResponse getTrend(String symbol, Instant from, Instant to);

    /**
     * Fetches the average price from Binance for the specified symbol and saves it to the database.
     */
    void fetchAndSaveAveragePrice(String symbol);

    /**
     * No-arg overloaded method fetchAndSaveAveragePrice
     */
    void fetchAndSaveAveragePrice();
}
