package by.ivan101454.averagepricetracker.price.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class BinanceRateLimitManager {

    private final AtomicLong banUntil = new AtomicLong(0);

    public boolean isBanned() {
        return Instant.now().toEpochMilli() < banUntil.get();
    }

    public Instant getBanEndTime() {
        return Instant.ofEpochMilli(banUntil.get());
    }

    public void applyBan(long secondsToWait) {
        long endTime = Instant.now().toEpochMilli() + (secondsToWait * 1000);
        banUntil.set(endTime);
    }

    public void resetBan() {
        banUntil.set(0); // Reset the lock time to zero
    }
}
