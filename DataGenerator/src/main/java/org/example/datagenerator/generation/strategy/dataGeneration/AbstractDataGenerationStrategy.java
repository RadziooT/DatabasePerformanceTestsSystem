package org.example.datagenerator.generation.strategy.dataGeneration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.datagenerator.model.DataGenerationRequest;
import org.example.datagenerator.model.VolumeSize;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDataGenerationStrategy implements DataGenerationStrategy {

    protected static final int ITEM_COUNT = 100_000;
    protected static final int SMALL_WAREHOUSE_COUNT = 10;
    protected static final int MEDIUM_WAREHOUSE_COUNT = 25;
    protected static final int LARGE_WAREHOUSE_COUNT = 50;
    protected static final int DISTRICT_COUNT = 10;
    protected static final int CUSTOMER_PER_DISTRICT = 3_000;
    protected static final int ORDER_PER_DISTRICT = 3_000;
    protected static final int FIRST_UNDELIVERED_ORDER_ID = 2_101;
    protected static final int ORDER_LINES_PER_ORDER = 10;
    protected static final int NEW_ORDERS_PER_DISTRICT = ORDER_PER_DISTRICT - FIRST_UNDELIVERED_ORDER_ID + 1;
    protected static final int ORDER_LINES_PER_DISTRICT = ORDER_PER_DISTRICT * ORDER_LINES_PER_ORDER;
    protected static final long MIN_BULK_CHUNK_ROWS = 50_000L;
    protected static final long MAX_BULK_CHUNK_ROWS = 1_000_000L;
    private static final ProgressRange ITEM_RANGE = new ProgressRange(5, 6);
    private static final ProgressRange WAREHOUSE_RANGE = new ProgressRange(6, 7);
    private static final ProgressRange DISTRICT_RANGE = new ProgressRange(7, 8);
    private static final ProgressRange STOCK_RANGE = new ProgressRange(8, 25);
    private static final ProgressRange CUSTOMER_RANGE = new ProgressRange(25, 31);
    private static final ProgressRange HISTORY_RANGE = new ProgressRange(31, 37);
    private static final ProgressRange ORDERS_RANGE = new ProgressRange(37, 42);
    private static final ProgressRange NEW_ORDER_RANGE = new ProgressRange(42, 43);
    private static final ProgressRange ORDER_LINE_RANGE = new ProgressRange(43, 100);
    protected final DataSource dataSource;
    protected final int batchSize;

    @Override
    public void generateData(DataGenerationRequest request) {
        VolumeSize volumeType = request.volumeType();
        ProgressReporter reporter = request.progressReporter();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                runGenerationWithCurrentMode(conn, volumeType, reporter);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Data generation SQL error: sqlState={}, errorCode={}, message={}",
                    e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw new RuntimeException("Data generation failed", e);
        }
    }

    private void runGenerationWithCurrentMode(Connection conn, VolumeSize volumeType, ProgressReporter reporter) throws SQLException {
        int numWarehouses = calculateWarehouseCount(volumeType);
        Instant now = Instant.now();
        reporter.report(ITEM_RANGE.start(), "INIT", "Initializing data generation for volume " + volumeType + " with warehouses: " + numWarehouses);

        generateItemsBulk(conn, reporter, ITEM_RANGE);
        conn.commit();

        generateWarehousesBulk(conn, numWarehouses, reporter, WAREHOUSE_RANGE);
        conn.commit();

        generateDistrictsBulk(conn, numWarehouses, reporter, DISTRICT_RANGE);
        conn.commit();

        generateStockBulk(conn, numWarehouses, reporter, STOCK_RANGE);
        conn.commit();

        generateCustomersBulk(conn, numWarehouses, now, reporter, CUSTOMER_RANGE);
        conn.commit();

        generateHistoryBulk(conn, numWarehouses, now, reporter, HISTORY_RANGE);
        conn.commit();

        generateOrdersBulk(conn, numWarehouses, now, reporter, ORDERS_RANGE);
        conn.commit();

        generateNewOrdersBulk(conn, numWarehouses, reporter, NEW_ORDER_RANGE);
        conn.commit();

        generateOrderLinesBulk(conn, numWarehouses, now, reporter, ORDER_LINE_RANGE);
        reporter.report(100, "COMPLETE", "Data generation complete");
    }

    protected abstract void loadStagedFile(Connection conn, String table, String columnsCsv, Path stagedFile) throws SQLException;

    private void generateItemsBulk(Connection conn, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = ITEM_COUNT;
        long reportEvery = calculateReportEvery(totalRows);
        try (BulkLoadSession session = createBulkLoadSession(conn, "item", "i_id,i_im_id,i_name,i_price,i_data", totalRows, reportEvery, reporter, progressRange, "ITEM")) {
            for (int i = 1; i <= ITEM_COUNT; i++) {
                session.writeRow(tsvRow(i, (i % 10_000) + 1, "Item" + i, BigDecimal.valueOf(1 + (i % 100)).add(BigDecimal.valueOf(0.99)), "Data for item " + i));
            }
        }
    }

    private void generateWarehousesBulk(Connection conn, int numWarehouses, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = numWarehouses;
        long reportEvery = calculateReportEvery(totalRows);
        try (BulkLoadSession session = createBulkLoadSession(conn, "warehouse", "w_id,w_name,w_street_1,w_street_2,w_city,w_state,w_zip,w_tax,w_ytd", totalRows, reportEvery, reporter, progressRange, "WAREHOUSE")) {
            for (int w = 1; w <= numWarehouses; w++) {
                session.writeRow(tsvRow(w, "Wh" + w, "Street1-" + w, "Street2-" + w, "City" + w, "ST", "123456789", BigDecimal.valueOf(0.05), BigDecimal.valueOf(300_000.00)));
            }
        }
    }

    private void generateDistrictsBulk(Connection conn, int numWarehouses, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = (long) numWarehouses * DISTRICT_COUNT;
        long reportEvery = calculateReportEvery(totalRows);
        try (BulkLoadSession session = createBulkLoadSession(conn, "district", "d_id,d_w_id,d_name,d_street_1,d_street_2,d_city,d_state,d_zip,d_tax,d_ytd,d_next_o_id", totalRows, reportEvery, reporter, progressRange, "DISTRICT")) {
            for (int w = 1; w <= numWarehouses; w++) {
                for (int d = 1; d <= DISTRICT_COUNT; d++) {
                    session.writeRow(tsvRow(d, w, "District" + d, "Street1-" + d, "Street2-" + d, "City" + d, "ST", "123456789", BigDecimal.valueOf(0.05), BigDecimal.valueOf(30_000.00), ORDER_PER_DISTRICT + 1));
                }
            }
        }
    }

    private void generateStockBulk(Connection conn, int numWarehouses, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = (long) numWarehouses * ITEM_COUNT;
        long reportEvery = calculateReportEvery(totalRows);
        try (BulkLoadSession session = createBulkLoadSession(conn, "stock", "s_i_id,s_w_id,s_quantity,s_dist_01,s_dist_02,s_dist_03,s_dist_04,s_dist_05,s_dist_06,s_dist_07,s_dist_08,s_dist_09,s_dist_10,s_ytd,s_order_cnt,s_remote_cnt,s_data", totalRows, reportEvery, reporter, progressRange, "STOCK")) {
            for (int w = 1; w <= numWarehouses; w++) {
                for (int i = 1; i <= ITEM_COUNT; i++) {
                    session.writeRow(tsvRow(i, w, 10 + (i % 90), "DistInfo-1", "DistInfo-2", "DistInfo-3", "DistInfo-4", "DistInfo-5", "DistInfo-6", "DistInfo-7", "DistInfo-8", "DistInfo-9", "DistInfo-10", 0, 0, 0, "StockData-" + i));
                }
            }
        }
    }

    private void generateCustomersBulk(Connection conn, int numWarehouses, Instant now, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = (long) numWarehouses * DISTRICT_COUNT * CUSTOMER_PER_DISTRICT;
        long reportEvery = calculateReportEvery(totalRows);
        Timestamp since = Timestamp.from(now);
        try (BulkLoadSession session = createBulkLoadSession(conn, "customer", "c_id,c_d_id,c_w_id,c_first,c_middle,c_last,c_street_1,c_street_2,c_city,c_state,c_zip,c_phone,c_since,c_credit,c_credit_lim,c_discount,c_balance,c_ytd_payment,c_payment_cnt,c_delivery_cnt,c_data", totalRows, reportEvery, reporter, progressRange, "CUSTOMER")) {
            for (int w = 1; w <= numWarehouses; w++) {
                for (int d = 1; d <= DISTRICT_COUNT; d++) {
                    for (int c = 1; c <= CUSTOMER_PER_DISTRICT; c++) {
                        session.writeRow(tsvRow(c, d, w, "First" + c, "OE", "Last" + c, "Street1-C" + c, "Street2-C" + c, "City", "ST", "123456789", "5551234567890123", since, "GC", BigDecimal.valueOf(50_000.00), BigDecimal.valueOf(0.05), BigDecimal.valueOf(-10.00), BigDecimal.valueOf(10.00), 1, 0, "Customer data " + c));
                    }
                }
            }
        }
    }

    private void generateHistoryBulk(Connection conn, int numWarehouses, Instant now, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = (long) numWarehouses * DISTRICT_COUNT * CUSTOMER_PER_DISTRICT;
        long reportEvery = calculateReportEvery(totalRows);
        Timestamp paymentDate = Timestamp.from(now);
        try (BulkLoadSession session = createBulkLoadSession(conn, "history", "h_c_id,h_c_d_id,h_c_w_id,h_d_id,h_w_id,h_date,h_amount,h_data", totalRows, reportEvery, reporter, progressRange, "HISTORY")) {
            for (int w = 1; w <= numWarehouses; w++) {
                for (int d = 1; d <= DISTRICT_COUNT; d++) {
                    for (int c = 1; c <= CUSTOMER_PER_DISTRICT; c++) {
                        session.writeRow(tsvRow(c, d, w, d, w, paymentDate, BigDecimal.valueOf(10.00), historyData(c)));
                    }
                }
            }
        }
    }

    private void generateOrdersBulk(Connection conn, int numWarehouses, Instant now, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = (long) numWarehouses * DISTRICT_COUNT * ORDER_PER_DISTRICT;
        long reportEvery = calculateReportEvery(totalRows);
        Timestamp entryDate = Timestamp.from(now);
        try (BulkLoadSession session = createBulkLoadSession(conn, "orders", "o_id,o_d_id,o_w_id,o_c_id,o_entry_d,o_carrier_id,o_ol_cnt,o_all_local", totalRows, reportEvery, reporter, progressRange, "ORDERS")) {
            for (int w = 1; w <= numWarehouses; w++) {
                for (int d = 1; d <= DISTRICT_COUNT; d++) {
                    for (int o = 1; o <= ORDER_PER_DISTRICT; o++) {
                        Integer carrierId = o < FIRST_UNDELIVERED_ORDER_ID ? (o % 10) + 1 : null;
                        session.writeRow(tsvRow(o, d, w, (o % CUSTOMER_PER_DISTRICT) + 1, entryDate, carrierId, ORDER_LINES_PER_ORDER, 1));
                    }
                }
            }
        }
    }

    private void generateNewOrdersBulk(Connection conn, int numWarehouses, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = (long) numWarehouses * DISTRICT_COUNT * NEW_ORDERS_PER_DISTRICT;
        long reportEvery = calculateReportEvery(totalRows);
        try (BulkLoadSession session = createBulkLoadSession(conn, "new_order", "no_o_id,no_d_id,no_w_id", totalRows, reportEvery, reporter, progressRange, "NEW_ORDER")) {
            for (int w = 1; w <= numWarehouses; w++) {
                for (int d = 1; d <= DISTRICT_COUNT; d++) {
                    for (int o = FIRST_UNDELIVERED_ORDER_ID; o <= ORDER_PER_DISTRICT; o++) {
                        session.writeRow(tsvRow(o, d, w));
                    }
                }
            }
        }
    }

    private void generateOrderLinesBulk(Connection conn, int numWarehouses, Instant now, ProgressReporter reporter, ProgressRange progressRange) throws SQLException {
        long totalRows = (long) numWarehouses * DISTRICT_COUNT * ORDER_LINES_PER_DISTRICT;
        long reportEvery = calculateReportEvery(totalRows);
        Timestamp deliveredAt = Timestamp.from(now);
        try (BulkLoadSession session = createBulkLoadSession(conn, "order_line", "ol_o_id,ol_d_id,ol_w_id,ol_number,ol_i_id,ol_supply_w_id,ol_delivery_d,ol_quantity,ol_amount,ol_dist_info", totalRows, reportEvery, reporter, progressRange, "ORDER_LINE")) {
            for (int w = 1; w <= numWarehouses; w++) {
                for (int d = 1; d <= DISTRICT_COUNT; d++) {
                    for (int o = 1; o <= ORDER_PER_DISTRICT; o++) {
                        boolean delivered = o < FIRST_UNDELIVERED_ORDER_ID;
                        for (int ol = 1; ol <= ORDER_LINES_PER_ORDER; ol++) {
                            Timestamp deliveryTs = delivered ? deliveredAt : null;
                            BigDecimal amount = delivered ? BigDecimal.ZERO : BigDecimal.valueOf(100.00);
                            session.writeRow(tsvRow(o, d, w, ol, ((o + ol) % ITEM_COUNT) + 1, w, deliveryTs, 5, amount, "DistInfo"));
                        }
                    }
                }
            }
        }
    }

    private BulkLoadSession createBulkLoadSession(
            Connection conn,
            String table,
            String columnsCsv,
            long totalRows,
            long reportEvery,
            ProgressReporter reporter,
            ProgressRange progressRange,
            String step
    ) throws SQLException {
        long chunkRows = Math.max(MIN_BULK_CHUNK_ROWS, Math.min(MAX_BULK_CHUNK_ROWS, totalRows / 10L));
        return new BulkLoadSession(conn, table, columnsCsv, chunkRows, totalRows, reportEvery, reporter, progressRange, step);
    }

    private String tsvRow(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append('\t');
            }
            sb.append(toBulkValue(values[i]));
        }
        return sb.toString();
    }

    private String toBulkValue(Object value) {
        if (value == null) {
            return "\\N";
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toString().replace('T', ' ');
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        return escapeBulkText(String.valueOf(value));
    }

    private String escapeBulkText(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String historyData(int customerId) {
        return "History-" + customerId;
    }

    private int calculateWarehouseCount(VolumeSize volumeType) {
        return switch (volumeType) {
            case SMALL -> SMALL_WAREHOUSE_COUNT;
            case MEDIUM -> MEDIUM_WAREHOUSE_COUNT;
            case LARGE -> LARGE_WAREHOUSE_COUNT;
        };
    }

    private long calculateReportEvery(long totalRows) {
        return Math.max(1L, totalRows / 20L);
    }

    private void reportTableProgress(
            ProgressReporter reporter,
            ProgressRange progressRange,
            String step,
            long inserted,
            long totalRows,
            long reportEvery
    ) {
        if (inserted % reportEvery != 0 && inserted != totalRows) {
            return;
        }

        int percent = progressRange.percentFor(inserted, totalRows);
        reporter.report(percent, step, "Inserted " + inserted + " / " + totalRows + " rows");
    }

    private record ProgressRange(int start, int end) {
        int percentFor(long inserted, long totalRows) {
            int stepRange = Math.max(0, end - start);
            return start + (int) ((inserted * stepRange) / Math.max(1L, totalRows));
        }
    }

    private final class BulkLoadSession implements AutoCloseable {
        private final Connection conn;
        private final String table;
        private final String columnsCsv;
        private final long chunkRows;
        private final long totalRows;
        private final long reportEvery;
        private final ProgressReporter reporter;
        private final ProgressRange progressRange;
        private final String step;

        private Path stagedFile;
        private BufferedWriter writer;
        private long rowsInChunk;
        private long inserted;

        private BulkLoadSession(
                Connection conn,
                String table,
                String columnsCsv,
                long chunkRows,
                long totalRows,
                long reportEvery,
                ProgressReporter reporter,
                ProgressRange progressRange,
                String step
        ) throws SQLException {
            this.conn = conn;
            this.table = table;
            this.columnsCsv = columnsCsv;
            this.chunkRows = Math.max(1L, chunkRows);
            this.totalRows = totalRows;
            this.reportEvery = Math.max(1L, reportEvery);
            this.reporter = reporter;
            this.progressRange = progressRange;
            this.step = step;
            openChunk();
        }

        private void writeRow(String row) throws SQLException {
            try {
                writer.write(row);
                writer.newLine();
            } catch (IOException e) {
                throw new SQLException("Failed to write staged row for table " + table, e);
            }

            rowsInChunk++;
            inserted++;
            reportTableProgress(reporter, progressRange, step, inserted, totalRows, reportEvery);

            if (rowsInChunk >= chunkRows) {
                flushChunk();
            }
        }

        private void openChunk() throws SQLException {
            try {
                stagedFile = Files.createTempFile("tpcc-" + table + "-", ".tsv");
                writer = Files.newBufferedWriter(stagedFile, StandardCharsets.UTF_8);
                rowsInChunk = 0;
            } catch (IOException e) {
                throw new SQLException("Failed to create staged file for table " + table, e);
            }
        }

        private void flushChunk() throws SQLException {
            if (rowsInChunk == 0) {
                return;
            }

            try {
                writer.close();
            } catch (IOException e) {
                throw new SQLException("Failed to close staged file for table " + table, e);
            }

            try {
                loadStagedFile(conn, table, columnsCsv, stagedFile);
                conn.commit();
            } finally {
                try {
                    Files.deleteIfExists(stagedFile);
                } catch (IOException ignored) {
                    log.warn("Could not delete staged file: {}", stagedFile);
                }
            }

            openChunk();
        }

        @Override
        public void close() throws SQLException {
            try {
                flushChunk();
            } finally {
                try {
                    writer.close();
                } catch (IOException ignored) {
                    // no-op
                }
                try {
                    Files.deleteIfExists(stagedFile);
                } catch (IOException ignored) {
                    // no-op
                }
            }
        }
    }
}
