package by.ivan101454.averagepricetracker.price.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Component
public class BinanceErrorDecoder implements ErrorDecoder {

    private final BinanceRateLimitManager rateLimitManager;
    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status == 429 || status == 418) {
            // Пытаемся достать заголовок Retry-After от Binance
            Collection<String> retryAfterHeaders = response.headers().get("Retry-After");
            long secondsToWait = 60; // Дефолтное время ожидания, если заголовка нет

            if (retryAfterHeaders != null && !retryAfterHeaders.isEmpty()) {
                try {
                    secondsToWait = Long.parseLong(retryAfterHeaders.iterator().next());
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse Retry-After header, using default 60s");
                }
            }

            rateLimitManager.applyBan(secondsToWait);

            if (status == 429) {
                log.error("HTTP 429: Exceeded Binance request weight! Rate limit back-off applied for {} seconds.", secondsToWait);
            } else {
                log.error("HTTP 418: IP BAN active! We are blocked by Binance. Waiting for {} seconds.", secondsToWait);
            }
        }

        // Возвращаем стандартное исключение Feign, чтобы цепочка прервалась корректно
        return defaultDecoder.decode(methodKey, response);
    }
}
