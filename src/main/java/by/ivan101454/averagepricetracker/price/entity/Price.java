package by.ivan101454.averagepricetracker.price.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Price entity is a representation a table price
 * from a database.
 * GenerationType.IDENTITY is because id field as BIGSERIAL.
 */
@Entity
@Table(name = "price")
@Getter
@Setter
@NoArgsConstructor
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;
    @Column(name = "price", nullable = false, precision = 30, scale = 8)
    private BigDecimal price;
    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}
