-- Function: create_partition_records
-- Purpose: creating daughter partition tables for parent price table
-- for given range of some year.

CREATE OR REPLACE FUNCTION create_partition_price(
    p_start_month INTEGER, --parameter starting month
    p_end_month INTEGER, --parameter end month
    p_year INTEGER --parameter year
) RETURNS void AS
$$
DECLARE
    i_month      INTEGER; -- iteration month variable
    v_start_date DATE; -- start date partition table
    v_end_date   DATE; -- end date partition table
    v_table_name TEXT; -- partition table name

BEGIN
    -- loop for creating number partition tables you need
    FOR i_month IN p_start_month..p_end_month
        LOOP
            -- create start data in DATA format
            v_start_date := make_date(p_year, i_month, 1);
            -- create end data in DATA format
            v_end_date := (v_start_date + INTERVAL '1 month')::DATE;
            -- create readable partition table name with assignment month and year
            v_table_name := format('price_%s_%s',
                                   p_year,
                                   lpad(i_month::TEXT, 2, '0'));

            -- Dynamically create partition table
            EXECUTE format(
                    'CREATE TABLE %I PARTITION OF price FOR VALUES FROM (%L) TO (%L)',
                    v_table_name, v_start_date, v_end_date
                    );
        END LOOP;
END ;
$$
    LANGUAGE plpgsql;

