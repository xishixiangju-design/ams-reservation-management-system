package com.trae.ams;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testConnection() throws SQLException {
        System.out.println("Connecting to database...");
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Database connection successful!");
            System.out.println("Database Product Name: " + connection.getMetaData().getDatabaseProductName());
            assertNotNull(connection);
            assertTrue(connection.isValid(2));
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
}
