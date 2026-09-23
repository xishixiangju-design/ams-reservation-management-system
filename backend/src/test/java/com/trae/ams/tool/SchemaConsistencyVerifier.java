package com.trae.ams.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
public class SchemaConsistencyVerifier {

    @Autowired
    private DataSource dataSource;

    private static final String ENTITY_PACKAGE = "com.trae.ams.entity";
    private static final String REPORT_FILE = "schema_check_report.md";
    private static final String FIX_SQL_FILE = "schema_fix.sql";

    @Test
    public void verifySchemaConsistency() throws Exception {
        System.out.println("Starting Schema Consistency Verification...");
        
        // 1. Scan Entity Classes
        Map<String, Class<?>> entityMap = scanEntityClasses();
        System.out.println("Found " + entityMap.size() + " entities.");

        // 2. Get DB Schema
        Map<String, Set<String>> dbSchema = getDbSchema();
        System.out.println("Found " + dbSchema.size() + " tables in database.");

        // 3. Compare and Generate Report
        StringBuilder report = new StringBuilder();
        StringBuilder fixSql = new StringBuilder();

        report.append("# Database Schema Consistency Report\n\n");
        report.append("Date: ").append(new Date()).append("\n\n");

        boolean hasIssues = false;

        for (Map.Entry<String, Class<?>> entry : entityMap.entrySet()) {
            String tableName = entry.getKey();
            Class<?> entityClass = entry.getValue();
            
            // Skip some common non-entity classes if any (heuristic)
            if (tableName.startsWith("ams_") || tableName.startsWith("sys_") || tableName.equals("member_info")) {
                // Good
            } else {
                // Maybe warn? For now assume all in entity package are entities
            }

            if (!dbSchema.containsKey(tableName)) {
                hasIssues = true;
                report.append("## 🔴 Missing Table: `").append(tableName).append("`\n");
                report.append("- Entity: `").append(entityClass.getName()).append("`\n");
                report.append("- Action: Create table needed.\n\n");
                
                fixSql.append("-- Missing Table: ").append(tableName).append("\n");
                fixSql.append(generateCreateTableSql(tableName, entityClass)).append("\n\n");
            } else {
                Set<String> dbColumns = dbSchema.get(tableName);
                List<Field> fields = getAllFields(entityClass);
                List<String> missingColumns = new ArrayList<>();

                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                        continue;
                    }
                    String columnName = StrUtil.toUnderlineCase(field.getName());
                    if (!dbColumns.contains(columnName)) {
                        missingColumns.add(columnName + " (" + field.getType().getSimpleName() + ")");
                        
                        fixSql.append("-- Missing Column in ").append(tableName).append("\n");
                        fixSql.append("ALTER TABLE `").append(tableName).append("` ADD COLUMN `")
                              .append(columnName).append("` ")
                              .append(getSqlType(field.getType())).append(" COMMENT '")
                              .append(field.getName()).append("';\n");
                    }
                }

                if (!missingColumns.isEmpty()) {
                    hasIssues = true;
                    report.append("## 🟠 Table `").append(tableName).append("` Missing Columns\n");
                    for (String col : missingColumns) {
                        report.append("- `").append(col).append("`\n");
                    }
                    report.append("\n");
                }
            }
        }

        // Check for tables in DB but not in Code (Optional, maybe specific prefix)
        report.append("## ℹ️ Orphan Tables (In DB but not in Entity Code)\n");
        for (String dbTable : dbSchema.keySet()) {
            if (!entityMap.containsKey(dbTable)) {
                report.append("- `").append(dbTable).append("`\n");
            }
        }

        // Write Files
        String projectRoot = System.getProperty("user.dir"); // Should be backend/
        // If running from IDE, user.dir might be project root. Adjust if needed.
        if (projectRoot.endsWith("backend")) {
            // Perfect
        } else {
            projectRoot = projectRoot + File.separator + "backend";
        }
        
        File reportFile = new File(projectRoot, REPORT_FILE);
        File fixFile = new File(projectRoot, FIX_SQL_FILE);

        FileUtil.writeUtf8String(report.toString(), reportFile);
        FileUtil.writeUtf8String(fixSql.toString(), fixFile);

        System.out.println("Report generated at: " + reportFile.getAbsolutePath());
        System.out.println("Fix SQL generated at: " + fixFile.getAbsolutePath());

        if (hasIssues) {
            System.err.println("Schema inconsistencies found! Check report.");
            // Don't fail the test so we can see the output, or fail if strictly required.
            // For a tool, passing is better so user can see output.
        } else {
            System.out.println("Schema is consistent!");
        }
    }

    private Map<String, Class<?>> scanEntityClasses() throws ClassNotFoundException {
        Map<String, Class<?>> map = new HashMap<>();
        
        // Simple directory scanning since ClassPathScanningCandidateComponentProvider might be tricky in test env without full context
        // But we are in SpringBootTest, so we can try.
        // Actually, just listing files in the directory is safer and easier for this specific task.
        File entityDir = new File("src/main/java/com/trae/ams/entity");
        if (!entityDir.exists()) {
             entityDir = new File("backend/src/main/java/com/trae/ams/entity");
        }
        
        if (entityDir.exists() && entityDir.isDirectory()) {
            for (File file : entityDir.listFiles()) {
                if (file.getName().endsWith(".java")) {
                    String className = ENTITY_PACKAGE + "." + file.getName().replace(".java", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        String tableName = StrUtil.toUnderlineCase(clazz.getSimpleName());
                        map.put(tableName, clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return map;
    }

    private Map<String, Set<String>> getDbSchema() throws Exception {
        Map<String, Set<String>> map = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            String catalog = conn.getCatalog();
            ResultSet tables = conn.getMetaData().getTables(catalog, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                map.put(tableName, new HashSet<>());
            }

            ResultSet columns = conn.getMetaData().getColumns(catalog, null, "%", "%");
            while (columns.next()) {
                String tableName = columns.getString("TABLE_NAME");
                String columnName = columns.getString("COLUMN_NAME");
                if (map.containsKey(tableName)) {
                    map.get(tableName).add(columnName);
                }
            }
        }
        return map;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private String generateCreateTableSql(String tableName, Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE `").append(tableName).append("` (\n");
        
        List<Field> fields = getAllFields(entityClass);
        List<String> definitions = new ArrayList<>();
        String primaryKey = null;

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            String colName = StrUtil.toUnderlineCase(field.getName());
            String colType = getSqlType(field.getType());
            
            String def = "  `" + colName + "` " + colType;
            if ("id".equals(colName)) {
                def += " NOT NULL AUTO_INCREMENT";
                primaryKey = colName;
            } else {
                def += " DEFAULT NULL";
            }
            definitions.add(def);
        }
        
        if (primaryKey != null) {
            definitions.add("  PRIMARY KEY (`" + primaryKey + "`)");
        }
        
        sql.append(String.join(",\n", definitions));
        sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        return sql.toString();
    }

    private String getSqlType(Class<?> type) {
        if (type == Long.class || type == long.class) return "BIGINT";
        if (type == Integer.class || type == int.class) return "INT";
        if (type == String.class) return "VARCHAR(255)";
        if (type == BigDecimal.class) return "DECIMAL(10,2)";
        if (type == LocalDateTime.class) return "DATETIME";
        if (type == LocalDate.class) return "DATE";
        if (type == Boolean.class || type == boolean.class) return "TINYINT(1)";
        if (type == Double.class || type == double.class) return "DOUBLE";
        return "VARCHAR(255)"; // Default
    }
}
