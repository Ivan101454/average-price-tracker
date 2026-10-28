package by.ivan101454.averagepricetracker.price.service;

import by.ivan101454.averagepricetracker.price.mapper.AveragePriceMapper;
import by.ivan101454.averagepricetracker.price.client.BinanceClient;
import by.ivan101454.averagepricetracker.price.client.BinanceRateLimitManager;
import by.ivan101454.averagepricetracker.price.dto.AveragePriceResponse;
import by.ivan101454.averagepricetracker.price.dto.BinanceAvgPriceResponse;
import by.ivan101454.averagepricetracker.price.dto.PricePageResponse;
import by.ivan101454.averagepricetracker.price.dto.PriceResponse;
import by.ivan101454.averagepricetracker.price.dto.Trend;
import by.ivan101454.averagepricetracker.price.dto.TrendResponse;
import by.ivan101454.averagepricetracker.price.entity.Price;
import by.ivan101454.averagepricetracker.price.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class PriceServiceImpl implements PriceService {

    private final String defaultSymbol;
    private final PriceRepository priceRepository;
    private final BinanceClient binanceClient;
    private final AveragePriceMapper mapper;
    private final BinanceRateLimitManager rateLimitManager;

    public PriceServiceImpl(@Value("${binance.default-symbol:BNBUSDT}") String defaultSymbol,
                            PriceRepository priceRepository,
                            BinanceClient binanceClient,
                            AveragePriceMapper mapper,
                            BinanceRateLimitManager rateLimitManager) {
        this.defaultSymbol = defaultSymbol;
        this.priceRepository = priceRepository;
        this.binanceClient = binanceClient;
        this.mapper = mapper;
        this.rateLimitManager = rateLimitManager;
    }

    @Override
    public PriceResponse getLatestPrice(String symbol) {
        Price price = priceRepository.findFirstBySymbolOrderByFetchedAtDesc(symbol)
                .orElseThrow(() -> new NoSuchElementException("No price records found"));
        return mapper.toResponse(price);
    }

    @Override
    public AveragePriceResponse getAveragePrice(String symbol, Instant from, Instant to) {
        BigDecimal average = priceRepository.findAveragePriceBySymbolAndFetchedAtBetween(
                        symbol, from, to)
                .orElseThrow(() -> new NoSuchElementException("No price data for the specified period"));
        return new AveragePriceResponse(average.setScale(8, RoundingMode.HALF_UP));
    }

    @Override
    public PricePageResponse getPriceHistory(String symbol, Instant from, Instant to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fetchedAt"));
        Page<Price> pricePage = priceRepository.findBySymbolAndFetchedAtBetween(
                symbol, from, to, pageable);

        List<PriceResponse> items = pricePage.getContent().stream()
                .map(mapper::toResponse)
                .toList();

        return new PricePageResponse(
                items,
                pricePage.getNumber(),
                pricePage.getSize(),
                pricePage.getTotalElements(),
                pricePage.hasNext()
        );
    }

    @Override
    public TrendResponse getTrend(String symbol, Instant from, Instant to) {
        List<Price> prices = priceRepository.findBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(
                symbol, from, to);
        if (prices.isEmpty()) {
            throw new NoSuchElementException("No price data for the specified period");
        }

        BigDecimal startPrice = prices.getFirst().getPrice();
        BigDecimal endPrice = prices.getLast().getPrice();
        Trend trend = determineTrend(startPrice, endPrice);

        return new TrendResponse(symbol, from, to, trend);
    }

    @Override
    public void fetchAndSaveAveragePrice(String symbol) {
        if (rateLimitManager.isBanned()) {
            // Just exit the method, preventing spam requests towards Binance
            // This protects us from passing error 429 (Too Many Requests) to hard ban 418 (Teapot)
            return;
        }
        try {
            BinanceAvgPriceResponse response = binanceClient.getAvgPrice(symbol);
            if (response == null || response.price() == null) {
                throw new IllegalStateException("Invalid response from Binance API");
            }
            Price price = new Price();
            price.setSymbol(symbol);
            price.setPrice(response.price());
            price.setFetchedAt(Instant.now());
            priceRepository.save(price);
        } catch (feign.FeignException e) {
            log.error("Network or communication error with Binance API: {}", e.getMessage());
        }
        catch (Exception e) {
            log.error("Unexpected error in price fetch service: {}", e.getMessage(), e);
        }
    }

    @Override
    public void fetchAndSaveAveragePrice() {
        fetchAndSaveAveragePrice(defaultSymbol);
    }

    private Trend determineTrend(BigDecimal start, BigDecimal end) {
        if (start == null || end == null) {
            return Trend.FLAT;
        }
        int comparison = end.compareTo(start);
        if (comparison > 0) {
            return Trend.UPWARD;
        } else if (comparison < 0) {
            return Trend.DOWNWARD;
        } else {
            return Trend.FLAT;
        }
    }
}
