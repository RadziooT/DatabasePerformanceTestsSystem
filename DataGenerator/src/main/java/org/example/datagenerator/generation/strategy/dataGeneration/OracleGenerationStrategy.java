package org.example.datagenerator.generation.strategy.dataGeneration;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class OracleGenerationStrategy extends AbstractDataGenerationStrategy {

    public OracleGenerationStrategy(DataSource dataSource, int batchSize) {
        super(dataSource, batchSize);
    }

    @Override
    protected void loadStagedFile(Connection conn, String table, String columnsCsv, Path stagedFile) throws SQLException {
        String[] columns = columnsCsv.split(",");
        String sql = buildInsertSql(table, columns);
        int effectiveBatchSize = Math.max(1, batchSize);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int inBatch = 0;
            for (String line : Files.readAllLines(stagedFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] values = line.split("\\t", -1);
                bindValues(stmt, table, values);
                stmt.addBatch();
                inBatch++;
                if (inBatch >= effectiveBatchSize) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                    inBatch = 0;
                }
            }
            if (inBatch > 0) {
                stmt.executeBatch();
                stmt.clearBatch();
            }
        } catch (IOException e) {
            throw new SQLException("Failed to stream staged file for table " + table, e);
        }
    }

    private String buildInsertSql(String table, String[] columns) {
        String placeholders = String.join(",", java.util.Collections.nCopies(columns.length, "?"));
        return "INSERT /*+ APPEND */ INTO " + table + " (" + String.join(",", columns) + ") VALUES (" + placeholders + ")";
    }

    private void bindValues(PreparedStatement stmt, String table, String[] values) throws SQLException {
        List<Integer> jdbcTypes = columnTypes(table);
        if (values.length != jdbcTypes.size()) {
            throw new SQLException("Invalid staged row for table " + table + ": expected " + jdbcTypes.size() + " columns but got " + values.length);
        }

        for (int i = 0; i < values.length; i++) {
            String decoded = decode(values[i]);
            int jdbcType = jdbcTypes.get(i);
            int index = i + 1;
            if (decoded == null) {
                stmt.setNull(index, jdbcType);
                continue;
            }
            switch (jdbcType) {
                case Types.INTEGER -> stmt.setInt(index, Integer.parseInt(decoded));
                case Types.DECIMAL -> stmt.setBigDecimal(index, new BigDecimal(decoded));
                case Types.TIMESTAMP -> stmt.setTimestamp(index, Timestamp.valueOf(decoded));
                default -> stmt.setString(index, decoded);
            }
        }
    }

    private String decode(String raw) {
        if ("\\N".equals(raw)) {
            return null;
        }
        return raw
                .replace("\\t", "\t")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
    }

    private List<Integer> columnTypes(String table) {
        return switch (table) {
            case "item" -> List.of(Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.DECIMAL, Types.VARCHAR);
            case "warehouse" ->
                    List.of(Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.CHAR, Types.CHAR, Types.DECIMAL, Types.DECIMAL);
            case "district" ->
                    List.of(Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.CHAR, Types.CHAR, Types.DECIMAL, Types.DECIMAL, Types.INTEGER);
            case "stock" ->
                    List.of(Types.INTEGER, Types.INTEGER, Types.DECIMAL, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.DECIMAL, Types.DECIMAL, Types.DECIMAL, Types.VARCHAR);
            case "customer" ->
                    List.of(Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.CHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.CHAR, Types.CHAR, Types.CHAR, Types.TIMESTAMP, Types.CHAR, Types.DECIMAL, Types.DECIMAL, Types.DECIMAL, Types.DECIMAL, Types.INTEGER, Types.INTEGER, Types.VARCHAR);
            case "history" ->
                    List.of(Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.TIMESTAMP, Types.DECIMAL, Types.VARCHAR);
            case "orders" ->
                    List.of(Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.TIMESTAMP, Types.INTEGER, Types.DECIMAL, Types.DECIMAL);
            case "new_order" -> List.of(Types.INTEGER, Types.INTEGER, Types.INTEGER);
            case "order_line" ->
                    List.of(Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.TIMESTAMP, Types.DECIMAL, Types.DECIMAL, Types.CHAR);
            default -> throw new IllegalArgumentException("Unsupported table for Oracle load: " + table);
        };
    }
}
