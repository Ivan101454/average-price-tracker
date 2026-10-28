package by.ivan101454.averagepricetracker.price.service;

import by.ivan101454.averagepricetracker.price.mapper.AveragePriceMapper;
import by.ivan101454.averagepricetracker.price.client.BinanceClient;
import by.ivan101454.averagepricetracker.price.client.BinanceRateLimitManager;
import by.ivan101454.averagepricetracker.price.dto.BinanceAvgPriceResponse;
import by.ivan101454.averagepricetracker.price.dto.Trend;
import by.ivan101454.averagepricetracker.price.dto.TrendResponse;
import by.ivan101454.averagepricetracker.price.entity.Price;
import by.ivan101454.averagepricetracker.price.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceImplTest {

    @Mock
    private PriceRepository priceRepository;
    @Mock
    private BinanceClient binanceClient;
    @Mock
    private AveragePriceMapper mapper;
    @Mock
    private BinanceRateLimitManager rateLimitManager;

    private PriceServiceImpl priceService;
    private final String symbol = "BNBUSDT";

    @BeforeEach
    void setUp() {
        priceService = new PriceServiceImpl(symbol, priceRepository, binanceClient, mapper, rateLimitManager);
    }

    @Test
    void getTrend_ShouldReturnUpward_WhenPriceIncreased() {
        // Given
        Instant from = Instant.now().minusSeconds(60);
        Instant to = Instant.now();

        Price first = new Price(); first.setPrice(new BigDecimal("500.0"));
        Price last = new Price(); last.setPrice(new BigDecimal("510.0"));

        when(priceRepository.findBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(symbol, from, to))
                .thenReturn(List.of(first, last));

        // When
        TrendResponse response = priceService.getTrend(symbol, from, to);

        // Then
        assertEquals(Trend.UPWARD, response.trend());
    }

    @Test
    void getTrend_ShouldThrowException_WhenNoDataFound() {
        Instant from = Instant.now().minusSeconds(60);
        Instant to = Instant.now();

        when(priceRepository.findBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(symbol, from, to))
                .thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () -> priceService.getTrend(symbol, from, to));
    }

    @Test
    void fetchAndSaveAveragePrice_ShouldNotCallBinance_WhenRateLimitIsBanned() {
        // Given
        when(rateLimitManager.isBanned()).thenReturn(true);

        // When
        priceService.fetchAndSaveAveragePrice(symbol);

        // Then
        verifyNoInteractions(binanceClient);
        verifyNoInteractions(priceRepository);
    }

    @Test
    void fetchAndSaveAveragePrice_ShouldSavePrice_WhenRequestIsSuccessful() {
        // Given
        when(rateLimitManager.isBanned()).thenReturn(false);
        BinanceAvgPriceResponse mockResponse = new BinanceAvgPriceResponse(new BigDecimal("550.25"), 5L);
        when(binanceClient.getAvgPrice(symbol)).thenReturn(mockResponse);

        // When
        priceService.fetchAndSaveAveragePrice(symbol);

        // Then
        verify(priceRepository, times(1)).save(any(Price.class));
    }
}
