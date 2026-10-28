package by.ivan101454.averagepricetracker.integration;

import by.ivan101454.averagepricetracker.price.client.BinanceRateLimitManager;
import by.ivan101454.averagepricetracker.price.repository.PriceRepository;
import by.ivan101454.averagepricetracker.price.service.PriceService;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0) // WireMock поднимется на случайном свободном порту
@Testcontainers // Активируем управление Docker контейнерами
class PriceTrackerIntegrationTest {

    // 1. Create and launch PostgreSQL container
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // DynamicPropertySource inserts the dynamic ports of the database and WireMock into the application configuration
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Redirecting the Feign client to the local WireMock server
registry.add("binance.api.url", () -> "http://localhost:${wiremock.server.port}/api/v3");
    }

    @Autowired
    private PriceService priceService;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private BinanceRateLimitManager rateLimitManager;

    @BeforeEach
    void cleanUp() {
        priceRepository.deleteAll();
        WireMock.reset();
        rateLimitManager.resetBan();
    }

    @Test
    void integration_ShouldFetchAndSavePriceFromWireMockDatabase() {
        // Given: Setting up WireMock for a successful JSON response
        stubFor(get(urlPathEqualTo("/api/v3/avgPrice"))
                .withQueryParam("symbol", equalTo("BNBUSDT"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"mins\": 5, \"price\": \"600.50000000\"}")
                        .withStatus(200)));

        // When: Calling the service method
        priceService.fetchAndSaveAveragePrice("BNBUSDT");

        // Then: We check that the record has been successfully saved to the Testcontainers database.
        long count = priceRepository.count();
        assertEquals(1, count, "Должна быть сохранена ровно одна запись в БД");

        var latestPrice = priceRepository.findFirstBySymbolOrderByFetchedAtDesc("BNBUSDT");
        assertTrue(latestPrice.isPresent());
        assertEquals(0, latestPrice.get().getPrice().compareTo(new java.math.BigDecimal("600.5")));
    }

    @Test
    void integration_ShouldTriggerRateLimitBan_WhenBinanceReturns429() {
        // Given: Setting up WireMock to return  HTTP 429 limit error
        stubFor(get(urlPathEqualTo("/api/v3/avgPrice"))
                .willReturn(aResponse()
                        .withHeader("Retry-After", "10") // Просим подождать 10 секунд
                        .withStatus(429)));

        // Initially, there is no ban
        assertFalse(rateLimitManager.isBanned());

        // When: Executing an error-provoking request
        priceService.fetchAndSaveAveragePrice("BNBUSDT");

        // Then: ErrorDecoder should work, and rateLimitManager should be banned.
        assertTrue(rateLimitManager.isBanned(), "Менеджер лимитов должен зафиксировать блокировку");
    }
}
