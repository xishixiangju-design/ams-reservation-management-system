package com.trae.ams;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootTest
public class SchemaCheckTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void checkTransactionTable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("DESCRIBE ams_transaction");
            System.out.println("--- ams_transaction columns ---");
            while (rs.next()) {
                System.out.println(rs.getString("Field") + " - " + rs.getString("Type"));
            }
            System.out.println("-------------------------------");
        }
    }
}
