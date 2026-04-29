package org.example.datagenerator.generation.strategy.schemaGeneration;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class SqlServerSchemaGenerator extends AbstractSchemaGenerationStrategy {

    public SqlServerSchemaGenerator(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void createSchema() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("""
                    ALTER DATABASE testDb 
                    SET READ_COMMITTED_SNAPSHOT ON
                    WITH ROLLBACK IMMEDIATE
                """);
            }
            log.info("SQL Server READ_COMMITTED defined");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to define SQL Server READ_COMMITTED", e);
        }

        try (Connection conn = dataSource.getConnection()) {
            executeSql(conn, """
                    IF OBJECT_ID('order_line', 'U') IS NOT NULL DROP TABLE order_line;
                    IF OBJECT_ID('new_order', 'U') IS NOT NULL DROP TABLE new_order;
                    IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
                    IF OBJECT_ID('history', 'U') IS NOT NULL DROP TABLE history;
                    IF OBJECT_ID('customer', 'U') IS NOT NULL DROP TABLE customer;
                    IF OBJECT_ID('stock', 'U') IS NOT NULL DROP TABLE stock;
                    IF OBJECT_ID('district', 'U') IS NOT NULL DROP TABLE district;
                    IF OBJECT_ID('warehouse', 'U') IS NOT NULL DROP TABLE warehouse;
                    IF OBJECT_ID('item', 'U') IS NOT NULL DROP TABLE item;
                    
                    CREATE TABLE item (
                        i_id INT NOT NULL,
                        i_im_id INT NOT NULL,
                        i_name VARCHAR(24) NOT NULL,
                        i_price DECIMAL(5,2) NOT NULL,
                        i_data VARCHAR(50) NOT NULL,
                        CONSTRAINT pk_item PRIMARY KEY (i_id)
                    );
                    
                    CREATE TABLE warehouse (
                        w_id INT NOT NULL,
                        w_name VARCHAR(10) NOT NULL,
                        w_street_1 VARCHAR(20) NOT NULL,
                        w_street_2 VARCHAR(20) NOT NULL,
                        w_city VARCHAR(20) NOT NULL,
                        w_state CHAR(2) NOT NULL,
                        w_zip CHAR(9) NOT NULL,
                        w_tax DECIMAL(4,4) NOT NULL,
                        w_ytd DECIMAL(12,2) NOT NULL,
                        CONSTRAINT pk_warehouse PRIMARY KEY (w_id)
                    );
                    
                    CREATE TABLE district (
                        d_id INT NOT NULL,
                        d_w_id INT NOT NULL,
                        d_name VARCHAR(10) NOT NULL,
                        d_street_1 VARCHAR(20) NOT NULL,
                        d_street_2 VARCHAR(20) NOT NULL,
                        d_city VARCHAR(20) NOT NULL,
                        d_state CHAR(2) NOT NULL,
                        d_zip CHAR(9) NOT NULL,
                        d_tax DECIMAL(4,4) NOT NULL,
                        d_ytd DECIMAL(12,2) NOT NULL,
                        d_next_o_id INT NOT NULL,
                        CONSTRAINT pk_district PRIMARY KEY (d_w_id, d_id),
                        CONSTRAINT fk_district_warehouse FOREIGN KEY (d_w_id) REFERENCES warehouse(w_id)
                    );
                    
                    CREATE TABLE stock (
                        s_i_id INT NOT NULL,
                        s_w_id INT NOT NULL,
                        s_quantity DECIMAL(4,0) NOT NULL,
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
                        s_ytd DECIMAL(8,0) NOT NULL,
                        s_order_cnt DECIMAL(4,0) NOT NULL,
                        s_remote_cnt DECIMAL(4,0) NOT NULL,
                        s_data VARCHAR(50) NOT NULL,
                        CONSTRAINT pk_stock PRIMARY KEY (s_w_id, s_i_id),
                        CONSTRAINT fk_stock_warehouse FOREIGN KEY (s_w_id) REFERENCES warehouse(w_id),
                        CONSTRAINT fk_stock_item FOREIGN KEY (s_i_id) REFERENCES item(i_id)
                    );
                    
                    CREATE TABLE customer (
                        c_id INT NOT NULL,
                        c_d_id INT NOT NULL,
                        c_w_id INT NOT NULL,
                        c_first VARCHAR(16) NOT NULL,
                        c_middle CHAR(2) NOT NULL,
                        c_last VARCHAR(16) NOT NULL,
                        c_street_1 VARCHAR(20) NOT NULL,
                        c_street_2 VARCHAR(20) NOT NULL,
                        c_city VARCHAR(20) NOT NULL,
                        c_state CHAR(2) NOT NULL,
                        c_zip CHAR(9) NOT NULL,
                        c_phone CHAR(16) NOT NULL,
                        c_since DATETIME2 NOT NULL,
                        c_credit CHAR(2) NOT NULL,
                        c_credit_lim DECIMAL(12,2) NOT NULL,
                        c_discount DECIMAL(4,4) NOT NULL,
                        c_balance DECIMAL(12,2) NOT NULL,
                        c_ytd_payment DECIMAL(12,2) NOT NULL,
                        c_payment_cnt INT NOT NULL,
                        c_delivery_cnt INT NOT NULL,
                        c_data VARCHAR(500),
                        CONSTRAINT pk_customer PRIMARY KEY (c_w_id, c_d_id, c_id),
                        CONSTRAINT fk_customer_district FOREIGN KEY (c_w_id, c_d_id) REFERENCES district(d_w_id, d_id)
                    );
                    
                    CREATE TABLE history (
                        h_id BIGINT IDENTITY(1,1) NOT NULL,
                        h_c_id INT NOT NULL,
                        h_c_d_id INT NOT NULL,
                        h_c_w_id INT NOT NULL,
                        h_d_id INT NOT NULL,
                        h_w_id INT NOT NULL,
                        h_date DATETIME2 NOT NULL,
                        h_amount DECIMAL(6,2) NOT NULL,
                        h_data VARCHAR(24) NOT NULL,
                        CONSTRAINT pk_history PRIMARY KEY (h_id),
                        CONSTRAINT fk_history_customer FOREIGN KEY (h_c_w_id, h_c_d_id, h_c_id)
                            REFERENCES customer(c_w_id, c_d_id, c_id),
                        CONSTRAINT fk_history_district FOREIGN KEY (h_w_id, h_d_id)
                            REFERENCES district(d_w_id, d_id)
                    );
                    
                    CREATE TABLE orders (
                        o_id INT NOT NULL,
                        o_d_id INT NOT NULL,
                        o_w_id INT NOT NULL,
                        o_c_id INT NOT NULL,
                        o_entry_d DATETIME2 NOT NULL,
                        o_carrier_id INT,
                        o_ol_cnt DECIMAL(2,0) NOT NULL,
                        o_all_local DECIMAL(1,0) NOT NULL,
                        CONSTRAINT pk_orders PRIMARY KEY (o_w_id, o_d_id, o_id),
                        CONSTRAINT fk_orders_customer FOREIGN KEY (o_w_id, o_d_id, o_c_id)
                            REFERENCES customer(c_w_id, c_d_id, c_id)
                    );
                    
                    CREATE TABLE new_order (
                        no_o_id INT NOT NULL,
                        no_d_id INT NOT NULL,
                        no_w_id INT NOT NULL,
                        CONSTRAINT pk_new_order PRIMARY KEY (no_w_id, no_d_id, no_o_id),
                        CONSTRAINT fk_new_order_orders FOREIGN KEY (no_w_id, no_d_id, no_o_id)
                            REFERENCES orders(o_w_id, o_d_id, o_id)
                    );
                    
                    CREATE TABLE order_line (
                        ol_o_id INT NOT NULL,
                        ol_d_id INT NOT NULL,
                        ol_w_id INT NOT NULL,
                        ol_number INT NOT NULL,
                        ol_i_id INT NOT NULL,
                        ol_supply_w_id INT NOT NULL,
                        ol_delivery_d DATETIME2 NULL,
                        ol_quantity DECIMAL(2,0) NOT NULL,
                        ol_amount DECIMAL(6,2) NOT NULL,
                        ol_dist_info CHAR(24) NOT NULL,
                        CONSTRAINT pk_order_line PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number),
                        CONSTRAINT fk_order_line_orders FOREIGN KEY (ol_w_id, ol_d_id, ol_o_id)
                            REFERENCES orders(o_w_id, o_d_id, o_id),
                        CONSTRAINT fk_order_line_stock FOREIGN KEY (ol_supply_w_id, ol_i_id)
                            REFERENCES stock(s_w_id, s_i_id)
                    );
                    
                    CREATE INDEX idx_customer_last_name ON customer (c_w_id, c_d_id, c_last, c_first);
                    CREATE INDEX idx_orders_customer ON orders (o_w_id, o_d_id, o_c_id, o_id);
                    CREATE INDEX idx_stock_item ON stock (s_i_id);
                    """);
            log.info("SQL Server schema created");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create SQL Server schema", e);
        }
    }
}

