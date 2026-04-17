package org.example.datagenerator.generation.strategy.schemaGeneration;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class OracleSchemaGenerator extends AbstractSchemaGenerationStrategy {

    public OracleSchemaGenerator(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void createSchema() {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            dropIfExists(stmt, "order_line");
            dropIfExists(stmt, "new_order");
            dropIfExists(stmt, "orders");
            dropIfExists(stmt, "history");
            dropIfExists(stmt, "customer");
            dropIfExists(stmt, "stock");
            dropIfExists(stmt, "district");
            dropIfExists(stmt, "warehouse");
            dropIfExists(stmt, "item");

            executeSql(conn, """
                    CREATE TABLE item (
                        i_id NUMBER(10) NOT NULL PRIMARY KEY,
                        i_im_id NUMBER(10) NOT NULL,
                        i_name VARCHAR2(24) NOT NULL,
                        i_price DECIMAL(5,2) NOT NULL,
                        i_data VARCHAR2(50) NOT NULL
                    );
                    
                    CREATE TABLE warehouse (
                        w_id NUMBER(10) NOT NULL PRIMARY KEY,
                        w_name VARCHAR2(10) NOT NULL,
                        w_street_1 VARCHAR2(20) NOT NULL,
                        w_street_2 VARCHAR2(20) NOT NULL,
                        w_city VARCHAR2(20) NOT NULL,
                        w_state CHAR(2) NOT NULL,
                        w_zip CHAR(9) NOT NULL,
                        w_tax DECIMAL(4,4) NOT NULL,
                        w_ytd DECIMAL(12,2) NOT NULL
                    );
                    
                    CREATE TABLE district (
                        d_id NUMBER(10) NOT NULL,
                        d_w_id NUMBER(10) NOT NULL,
                        d_name VARCHAR2(10) NOT NULL,
                        d_street_1 VARCHAR2(20) NOT NULL,
                        d_street_2 VARCHAR2(20) NOT NULL,
                        d_city VARCHAR2(20) NOT NULL,
                        d_state CHAR(2) NOT NULL,
                        d_zip CHAR(9) NOT NULL,
                        d_tax DECIMAL(4,4) NOT NULL,
                        d_ytd DECIMAL(12,2) NOT NULL,
                        d_next_o_id NUMBER(10) NOT NULL,
                        PRIMARY KEY (d_w_id, d_id),
                        CONSTRAINT fk_district_warehouse FOREIGN KEY (d_w_id) REFERENCES warehouse(w_id)
                    );
                    
                    CREATE TABLE stock (
                        s_i_id NUMBER(10) NOT NULL,
                        s_w_id NUMBER(10) NOT NULL,
                        s_quantity NUMBER(4) NOT NULL,
                        s_dist_01 CHAR(24) NOT NULL,
                        s_dist_02 CHAR(24) NOT NULL,
                        s_dist_03 CHAR(24) NOT NULL,
                        s_dist_04 CHAR(24) NOT NULL,
                        s_dist_05 CHAR(24) NOT NULL,
                        s_dist_06 CHAR(24) NOT NULL,
                        s_dist_07 CHAR(24) NOT NULL,
                        s_dist_08 CHAR(24) NOT NULL,
                        s_dist_09 CHAR(24) NOT NULL,
                        s_dist_10 CHAR(24) NOT NULL,
                        s_ytd NUMBER(8) NOT NULL,
                        s_order_cnt NUMBER(4) NOT NULL,
                        s_remote_cnt NUMBER(4) NOT NULL,
                        s_data VARCHAR2(50) NOT NULL,
                        PRIMARY KEY (s_w_id, s_i_id),
                        CONSTRAINT fk_stock_warehouse FOREIGN KEY (s_w_id) REFERENCES warehouse(w_id),
                        CONSTRAINT fk_stock_item FOREIGN KEY (s_i_id) REFERENCES item(i_id)
                    );
                    
                    CREATE TABLE customer (
                        c_id NUMBER(10) NOT NULL,
                        c_d_id NUMBER(10) NOT NULL,
                        c_w_id NUMBER(10) NOT NULL,
                        c_first VARCHAR2(16) NOT NULL,
                        c_middle CHAR(2) NOT NULL,
                        c_last VARCHAR2(16) NOT NULL,
                        c_street_1 VARCHAR2(20) NOT NULL,
                        c_street_2 VARCHAR2(20) NOT NULL,
                        c_city VARCHAR2(20) NOT NULL,
                        c_state CHAR(2) NOT NULL,
                        c_zip CHAR(9) NOT NULL,
                        c_phone CHAR(16) NOT NULL,
                        c_since TIMESTAMP NOT NULL,
                        c_credit CHAR(2) NOT NULL,
                        c_credit_lim DECIMAL(12,2) NOT NULL,
                        c_discount DECIMAL(4,4) NOT NULL,
                        c_balance DECIMAL(12,2) NOT NULL,
                        c_ytd_payment DECIMAL(12,2) NOT NULL,
                        c_payment_cnt NUMBER(10) NOT NULL,
                        c_delivery_cnt NUMBER(10) NOT NULL,
                        c_data VARCHAR2(500),
                        PRIMARY KEY (c_w_id, c_d_id, c_id),
                        CONSTRAINT fk_customer_district FOREIGN KEY (c_w_id, c_d_id) REFERENCES district(d_w_id, d_id)
                    );
                    
                    CREATE TABLE history (
                        h_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        h_c_id NUMBER(10) NOT NULL,
                        h_c_d_id NUMBER(10) NOT NULL,
                        h_c_w_id NUMBER(10) NOT NULL,
                        h_d_id NUMBER(10) NOT NULL,
                        h_w_id NUMBER(10) NOT NULL,
                        h_date TIMESTAMP NOT NULL,
                        h_amount DECIMAL(6,2) NOT NULL,
                        h_data VARCHAR2(24) NOT NULL,
                        CONSTRAINT fk_history_customer FOREIGN KEY (h_c_w_id, h_c_d_id, h_c_id)
                            REFERENCES customer(c_w_id, c_d_id, c_id),
                        CONSTRAINT fk_history_district FOREIGN KEY (h_w_id, h_d_id)
                            REFERENCES district(d_w_id, d_id)
                    );
                    
                    CREATE TABLE orders (
                        o_id NUMBER(10) NOT NULL,
                        o_d_id NUMBER(10) NOT NULL,
                        o_w_id NUMBER(10) NOT NULL,
                        o_c_id NUMBER(10) NOT NULL,
                        o_entry_d TIMESTAMP NOT NULL,
                        o_carrier_id NUMBER(10),
                        o_ol_cnt NUMBER(2) NOT NULL,
                        o_all_local NUMBER(1) NOT NULL,
                        PRIMARY KEY (o_w_id, o_d_id, o_id),
                        CONSTRAINT fk_orders_customer FOREIGN KEY (o_w_id, o_d_id, o_c_id)
                            REFERENCES customer(c_w_id, c_d_id, c_id)
                    );
                    
                    CREATE TABLE new_order (
                        no_o_id NUMBER(10) NOT NULL,
                        no_d_id NUMBER(10) NOT NULL,
                        no_w_id NUMBER(10) NOT NULL,
                        PRIMARY KEY (no_w_id, no_d_id, no_o_id),
                        CONSTRAINT fk_new_order_orders FOREIGN KEY (no_w_id, no_d_id, no_o_id)
                            REFERENCES orders(o_w_id, o_d_id, o_id)
                    );
                    
                    CREATE TABLE order_line (
                        ol_o_id NUMBER(10) NOT NULL,
                        ol_d_id NUMBER(10) NOT NULL,
                        ol_w_id NUMBER(10) NOT NULL,
                        ol_number NUMBER(10) NOT NULL,
                        ol_i_id NUMBER(10) NOT NULL,
                        ol_supply_w_id NUMBER(10) NOT NULL,
                        ol_delivery_d TIMESTAMP,
                        ol_quantity NUMBER(2) NOT NULL,
                        ol_amount DECIMAL(6,2) NOT NULL,
                        ol_dist_info CHAR(24) NOT NULL,
                        PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number),
                        CONSTRAINT fk_order_line_orders FOREIGN KEY (ol_w_id, ol_d_id, ol_o_id)
                            REFERENCES orders(o_w_id, o_d_id, o_id),
                        CONSTRAINT fk_order_line_stock FOREIGN KEY (ol_supply_w_id, ol_i_id)
                            REFERENCES stock(s_w_id, s_i_id)
                    );
                    
                    CREATE INDEX idx_customer_last_name ON customer (c_w_id, c_d_id, c_last, c_first);
                    CREATE INDEX idx_orders_customer ON orders (o_w_id, o_d_id, o_c_id, o_id);
                    CREATE INDEX idx_stock_item ON stock (s_i_id);
                    """);

            log.info("Oracle schema created");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create Oracle schema", e);
        }
    }

    private void dropIfExists(Statement stmt, String table) {
        try {
            stmt.execute("DROP TABLE " + table + " CASCADE CONSTRAINTS PURGE");
        } catch (SQLException ignored) {
            // ignore missing tables during recreation
        }
    }
}
