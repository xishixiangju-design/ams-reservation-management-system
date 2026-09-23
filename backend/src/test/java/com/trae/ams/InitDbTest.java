package com.trae.ams;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;

@SpringBootTest
public class InitDbTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void initDatabase() throws Exception {
        // Path to the SQL file
        String schemaPath = "C:\\Users\\DELL\\Desktop\\Trae kasa\\docs\\DB_SCHEMA_v2.sql";
        String dataPath = "C:\\Users\\DELL\\Desktop\\Trae kasa\\docs\\DB_DATA_v2.sql";
        
        File schemaFile = new File(schemaPath);
        File dataFile = new File(dataPath);
        
        if (!schemaFile.exists()) {
            throw new RuntimeException("Schema file not found: " + schemaPath);
        }
        if (!dataFile.exists()) {
            throw new RuntimeException("Data file not found: " + dataPath);
        }

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Executing Schema script: " + schemaPath);
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(schemaFile));
            
            System.out.println("Executing Data script: " + dataPath);
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(dataFile));
            
            System.out.println("Database initialized successfully!");
        }
    }
}
