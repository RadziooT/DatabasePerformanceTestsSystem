package org.example.datagenerator.generation.strategy.schemaGeneration;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class MysqlSchemaGenerator extends AbstractSchemaGenerationStrategy {

    public MysqlSchemaGenerator(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void createSchema() {
        try (Connection conn = dataSource.getConnection()) {
            executeSql(conn, """
                    SET FOREIGN_KEY_CHECKS = 0;
                    DROP TABLE IF EXISTS order_line;
                    DROP TABLE IF EXISTS new_order;
                    DROP TABLE IF EXISTS orders;
                    DROP TABLE IF EXISTS history;
                    DROP TABLE IF EXISTS customer;
                    DROP TABLE IF EXISTS stock;
                    DROP TABLE IF EXISTS district;
                    DROP TABLE IF EXISTS warehouse;
                    DROP TABLE IF EXISTS item;
                    SET FOREIGN_KEY_CHECKS = 1;
                    
                    CREATE TABLE item (
                        i_id INT NOT NULL,
                        i_im_id INT NOT NULL,
                        i_name VARCHAR(24) NOT NULL,
                        i_price DECIMAL(5,2) NOT NULL,
                        i_data VARCHAR(50) NOT NULL,
                        PRIMARY KEY (i_id)
                    ) ENGINE=InnoDB;
                    
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
                        PRIMARY KEY (w_id)
                    ) ENGINE=InnoDB;
                    
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
                        PRIMARY KEY (d_w_id, d_id),
                        CONSTRAINT fk_district_warehouse FOREIGN KEY (d_w_id) REFERENCES warehouse(w_id)
                    ) ENGINE=InnoDB;
                    
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
                        PRIMARY KEY (s_w_id, s_i_id),
                        CONSTRAINT fk_stock_warehouse FOREIGN KEY (s_w_id) REFERENCES warehouse(w_id),
                        CONSTRAINT fk_stock_item FOREIGN KEY (s_i_id) REFERENCES item(i_id)
                    ) ENGINE=InnoDB;
                    
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
                        c_since TIMESTAMP NOT NULL,
                        c_credit CHAR(2) NOT NULL,
                        c_credit_lim DECIMAL(12,2) NOT NULL,
                        c_discount DECIMAL(4,4) NOT NULL,
                        c_balance DECIMAL(12,2) NOT NULL,
                        c_ytd_payment DECIMAL(12,2) NOT NULL,
                        c_payment_cnt INT NOT NULL,
                        c_delivery_cnt INT NOT NULL,
                        c_data VARCHAR(500),
                        PRIMARY KEY (c_w_id, c_d_id, c_id),
                        CONSTRAINT fk_customer_district FOREIGN KEY (c_w_id, c_d_id) REFERENCES district(d_w_id, d_id)
                    ) ENGINE=InnoDB;
                    
                    CREATE TABLE history (
                        h_id BIGINT NOT NULL AUTO_INCREMENT,
                        h_c_id INT NOT NULL,
                        h_c_d_id INT NOT NULL,
                        h_c_w_id INT NOT NULL,
                        h_d_id INT NOT NULL,
                        h_w_id INT NOT NULL,
                        h_date TIMESTAMP NOT NULL,
                        h_amount DECIMAL(6,2) NOT NULL,
                        h_data VARCHAR(24) NOT NULL,
                        PRIMARY KEY (h_id),
                        CONSTRAINT fk_history_customer FOREIGN KEY (h_c_w_id, h_c_d_id, h_c_id)
                            REFERENCES customer(c_w_id, c_d_id, c_id),
                        CONSTRAINT fk_history_district FOREIGN KEY (h_w_id, h_d_id)
                            REFERENCES district(d_w_id, d_id)
                    ) ENGINE=InnoDB;
                    
                    CREATE TABLE orders (
                        o_id INT NOT NULL,
                        o_d_id INT NOT NULL,
                        o_w_id INT NOT NULL,
                        o_c_id INT NOT NULL,
                        o_entry_d TIMESTAMP NOT NULL,
                        o_carrier_id INT,
                        o_ol_cnt DECIMAL(2,0) NOT NULL,
                        o_all_local DECIMAL(1,0) NOT NULL,
                        PRIMARY KEY (o_w_id, o_d_id, o_id),
                        CONSTRAINT fk_orders_customer FOREIGN KEY (o_w_id, o_d_id, o_c_id)
                            REFERENCES customer(c_w_id, c_d_id, c_id)
                    ) ENGINE=InnoDB;
                    
                    CREATE TABLE new_order (
                        no_o_id INT NOT NULL,
                        no_d_id INT NOT NULL,
                        no_w_id INT NOT NULL,
                        PRIMARY KEY (no_w_id, no_d_id, no_o_id),
                        CONSTRAINT fk_new_order_orders FOREIGN KEY (no_w_id, no_d_id, no_o_id)
                            REFERENCES orders(o_w_id, o_d_id, o_id)
                    ) ENGINE=InnoDB;
                    
                    CREATE TABLE order_line (
                        ol_o_id INT NOT NULL,
                        ol_d_id INT NOT NULL,
                        ol_w_id INT NOT NULL,
                        ol_number INT NOT NULL,
                        ol_i_id INT NOT NULL,
                        ol_supply_w_id INT NOT NULL,
                        ol_delivery_d TIMESTAMP NULL,
                        ol_quantity DECIMAL(2,0) NOT NULL,
                        ol_amount DECIMAL(6,2) NOT NULL,
                        ol_dist_info CHAR(24) NOT NULL,
                        PRIMARY KEY (ol_w_id, ol_d_id, ol_o_id, ol_number),
                        CONSTRAINT fk_order_line_orders FOREIGN KEY (ol_w_id, ol_d_id, ol_o_id)
                            REFERENCES orders(o_w_id, o_d_id, o_id),
                        CONSTRAINT fk_order_line_stock FOREIGN KEY (ol_supply_w_id, ol_i_id)
                            REFERENCES stock(s_w_id, s_i_id)
                    ) ENGINE=InnoDB;
                    
                    CREATE INDEX idx_customer_last_name ON customer (c_w_id, c_d_id, c_last, c_first);
                    CREATE INDEX idx_orders_customer ON orders (o_w_id, o_d_id, o_c_id, o_id);
                    CREATE INDEX idx_stock_item ON stock (s_i_id);
                    """);
            log.info("MySQL schema created");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create MySQL schema", e);
        }
    }
}
