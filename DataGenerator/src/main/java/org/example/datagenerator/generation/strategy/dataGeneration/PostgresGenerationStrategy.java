package org.example.datagenerator.generation.strategy.dataGeneration;

import org.postgresql.PGConnection;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgresGenerationStrategy extends AbstractDataGenerationStrategy {

    public PostgresGenerationStrategy(DataSource dataSource, int batchSize) {
        super(dataSource, batchSize);
    }

    @Override
    protected void loadStagedFile(Connection conn, String table, String columnsCsv, Path stagedFile) throws SQLException {
        String copySql = "COPY " + table + " (" + columnsCsv + ") FROM STDIN WITH (FORMAT text, DELIMITER E'\\t', NULL '\\N')";
        PGConnection pgConnection = conn.unwrap(PGConnection.class);

        try (Reader reader = Files.newBufferedReader(stagedFile, StandardCharsets.UTF_8)) {
            pgConnection.getCopyAPI().copyIn(copySql, reader);
        } catch (IOException e) {
            throw new SQLException("Failed to stream staged file for table " + table, e);
        }
    }
}
