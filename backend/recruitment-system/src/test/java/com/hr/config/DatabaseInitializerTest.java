package com.hr.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DatabaseInitializerTest {

    @Test
    void runCreatesMessageTable() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DatabaseInitializer initializer = new DatabaseInitializer();
        ReflectionTestUtils.setField(initializer, "jdbcTemplate", jdbcTemplate);

        initializer.run();

        verify(jdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS message"));
    }
}
