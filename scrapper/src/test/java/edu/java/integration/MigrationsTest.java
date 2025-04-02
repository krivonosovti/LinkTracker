package edu.java.integration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

public class MigrationsTest extends IntegrationTest {

    private static final String TABLES_SQL = """
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
        """;
    private static final String COLUMNS_SQL = """
        SELECT column_name
        FROM information_schema.columns
        WHERE table_name = ?
        """;
    private static final String SEQUENCES_SQL = """
        SELECT sequence_name
        FROM information_schema.sequences
        """;

    @SneakyThrows
    @Test
    public void tablesTest() {
        List<String> expectedTables =
            List.of("chats", "links", "chats_links", "databasechangelog", "databasechangeloglock");

        ResultSet resultSet = POSTGRES.createConnection("")
            .createStatement()
            .executeQuery(TABLES_SQL);

        List<String> actualTables = new ArrayList<>();
        while (resultSet.next()) {
            actualTables.add(resultSet.getString("table_name"));
        }

        assertThat(actualTables.size()).isEqualTo(5);
        assertThat(actualTables).containsExactlyInAnyOrderElementsOf(expectedTables);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("edu.java.integration.MigrationsTest#tableColumns")
    public void tableColumnsTest(String table, List<String> expectedColumns) {
        PreparedStatement statement = POSTGRES.createConnection("")
            .prepareStatement(COLUMNS_SQL);
        statement.setString(1, table);

        ResultSet resultSet = statement.executeQuery();

        List<String> actualColumns = new ArrayList<>();
        while (resultSet.next()) {
            actualColumns.add(resultSet.getString("column_name"));
        }

        assertThat(actualColumns.size()).isEqualTo(expectedColumns.size());
        assertThat(actualColumns).containsExactlyInAnyOrderElementsOf(expectedColumns);
    }

    @SneakyThrows
    @Test
    public void sequencesTest() {
        List<String> expectedSequences = List.of("chats_id_seq", "links_id_seq");

        ResultSet resultSet = POSTGRES.createConnection("")
            .createStatement()
            .executeQuery(SEQUENCES_SQL);

        List<String> actualSequences = new ArrayList<>();
        while (resultSet.next()) {
            actualSequences.add(resultSet.getString("sequence_name"));
        }

        assertThat(actualSequences.size()).isEqualTo(2);
        assertThat(actualSequences).containsExactlyInAnyOrderElementsOf(expectedSequences);
    }

    static Stream<Arguments> tableColumns() {
        return Stream.of(
            Arguments.of("chats", List.of("id", "chat_id")),
            Arguments.of("links", List.of("id", "link_type", "url", "checked_at", "status")),
            Arguments.of("chats_links", List.of("chat_id", "link_id"))
        );
    }
}
