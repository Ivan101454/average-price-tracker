package by.ivan101454.averagepricetracker.price.repository;

import by.ivan101454.averagepricetracker.price.entity.Price;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findFirstBySymbolOrderByFetchedAtDesc(String symbol);

    List<Price> findBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(String symbol,
                                                                   Instant from, Instant to);

    Page<Price> findBySymbolAndFetchedAtBetween(String symbol,
                                                                   Instant from, Instant to, Pageable pageable);

    @Query(value = "SELECT AVG(p.price) FROM Price p WHERE p.symbol = :symbol AND p.fetchedAt BETWEEN :from AND :to")
    Optional<BigDecimal> findAveragePriceBySymbolAndFetchedAtBetween(
            @Param("symbol") String symbol,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

}
