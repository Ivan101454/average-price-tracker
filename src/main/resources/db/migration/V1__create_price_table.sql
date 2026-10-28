CREATE TABLE price
(
    id         BIGSERIAL,
    symbol     VARCHAR(20)    NOT NULL,
    price      NUMERIC(30, 8) NOT NULL,
    fetched_at TIMESTAMPTZ    NOT NULL,
    CONSTRAINT pk_price PRIMARY KEY (id, fetched_at),
    CONSTRAINT uq_price_symbol_fetched_at UNIQUE (symbol, fetched_at)
) PARTITION BY RANGE (fetched_at);

CREATE INDEX idx_price_fetched_at ON price (fetched_at);

comment on table price is 'History of average prices fetched from Binance. One row = one successful poll of the /api/v3/avgPrice endpoint.';

COMMENT ON COLUMN price.id IS 'Unique price record identifier';
COMMENT ON COLUMN price.symbol IS 'Unique trading pair symbol, e.g. BNBUSDT';
COMMENT ON COLUMN price.price IS 'Average price value (exact decimal, no double)';
COMMENT ON COLUMN price.fetched_at IS 'UTC instant when Binance returned the price';