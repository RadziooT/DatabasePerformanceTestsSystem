package org.example.datagenerator.generation.strategy.dataGeneration;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlGenerationStrategy extends AbstractDataGenerationStrategy {

    public MysqlGenerationStrategy(DataSource dataSource, int batchSize) {
        super(dataSource, batchSize);
    }

    @Override
    protected void loadStagedFile(Connection conn, String table, String columnsCsv, Path stagedFile) throws SQLException {
        String escapedPath = stagedFile.toAbsolutePath().toString().replace("\\", "\\\\");
        String loadSql = "LOAD DATA LOCAL INFILE '" + escapedPath + "' INTO TABLE " + table
                + " FIELDS TERMINATED BY '\\t' ESCAPED BY '\\\\' LINES TERMINATED BY '\\n' (" + columnsCsv + ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(loadSql);
        } catch (SQLException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("local") && message.contains("infile") && message.contains("disabled")) {
                throw new SQLException(
                        "MySQL rejected LOAD DATA LOCAL INFILE. Enable server variable local_infile=ON and keep JDBC allowLoadLocalInfile=true.",
                        e
                );
            }
            throw e;
        }
    }
}
