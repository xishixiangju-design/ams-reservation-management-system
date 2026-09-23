package com.trae.ams.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private DataSource dataSource;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Add total_consumption column if not exists
            try {
                stmt.execute("SELECT total_consumption FROM sys_user LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding total_consumption column to sys_user...");
                stmt.execute("ALTER TABLE sys_user ADD COLUMN total_consumption DECIMAL(10, 2) DEFAULT 0.00 COMMENT 'Total cumulative consumption'");
            }

            // 2. Create ams_member_level table
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_member_level (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL COMMENT 'Level Name'," +
                    "code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Level Code'," +
                    "min_consumption DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'Threshold'," +
                    "discount_rate DECIMAL(3, 2) NOT NULL DEFAULT 1.00 COMMENT 'Discount'," +
                    "icon_url VARCHAR(255) COMMENT 'Icon URL'," +
                    "rights_desc TEXT COMMENT 'Rights Description'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Membership Level Rules'");

            // 3. Create ams_member_level_log table
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_member_level_log (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id BIGINT NOT NULL COMMENT 'User ID'," +
                    "old_level VARCHAR(50) COMMENT 'Old Level Code'," +
                    "new_level VARCHAR(50) NOT NULL COMMENT 'New Level Code'," +
                    "trigger_amount DECIMAL(10, 2) COMMENT 'Consumption amount that triggered this'," +
                    "total_consumption DECIMAL(10, 2) COMMENT 'Total consumption at that time'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_user_id (user_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Membership Level Change Log'");

            // 4. Insert initial data if empty
            var rs = stmt.executeQuery("SELECT count(*) FROM ams_member_level");
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Initializing member levels...");
                stmt.execute("INSERT INTO ams_member_level (name, code, min_consumption, discount_rate) VALUES " +
                        "('普通会员', 'NORMAL', 0.00, 1.00)," +
                        "('铜牌会员', 'BRONZE', 1000.00, 0.98)," +
                        "('银牌会员', 'SILVER', 5000.00, 0.95)," +
                        "('金牌会员', 'GOLD', 20000.00, 0.90)," +
                        "('钻石会员', 'DIAMOND', 100000.00, 0.85)");
            }

            // 5. Create ams_waiting_list table if not exists
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_waiting_list (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "store_id BIGINT NOT NULL COMMENT 'Store ID'," +
                    "customer_id BIGINT NOT NULL COMMENT 'Customer ID'," +
                    "service_id BIGINT NOT NULL COMMENT 'Service ID'," +
                    "tech_id BIGINT COMMENT 'Technician ID'," +
                    "expected_date DATE NOT NULL COMMENT 'Expected Date'," +
                    "time_range VARCHAR(50) NOT NULL COMMENT 'Time Range'," +
                    "people_count INT DEFAULT 1 COMMENT 'People Count'," +
                    "status VARCHAR(20) DEFAULT 'WAITING' COMMENT 'Status: WAITING, NOTIFIED, CONVERTED, EXPIRED, CANCELLED'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "expiry_time DATETIME COMMENT 'Expiry Time'," +
                    "INDEX idx_store_date_status (store_id, expected_date, status)," +
                    "INDEX idx_customer (customer_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Waiting List'");

            // 6. Add service_id to ams_waiting_list if missing (for existing tables)
            try {
                stmt.execute("SELECT service_id FROM ams_waiting_list LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding service_id column to ams_waiting_list...");
                stmt.execute("ALTER TABLE ams_waiting_list ADD COLUMN service_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Service ID' AFTER customer_id");
            }

            // 7. Create sys_notification table if not exists
            stmt.execute("CREATE TABLE IF NOT EXISTS sys_notification (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "store_id BIGINT COMMENT 'Store ID'," +
                    "user_id BIGINT NOT NULL COMMENT 'User ID'," +
                    "type VARCHAR(50) COMMENT 'Type'," +
                    "title VARCHAR(100) COMMENT 'Title'," +
                    "content TEXT COMMENT 'Content'," +
                    "target VARCHAR(50) COMMENT 'Target'," +
                    "status INT DEFAULT 0 COMMENT 'Status'," +
                    "read_status INT DEFAULT 0 COMMENT 'Read Status'," +
                    "error_msg TEXT COMMENT 'Error Message'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "send_time DATETIME COMMENT 'Send Time'," +
                    "INDEX idx_user (user_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Notification'");

            // 8. Add user_id to sys_notification if missing
            try {
                stmt.execute("SELECT user_id FROM sys_notification LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding user_id column to sys_notification...");
                stmt.execute("ALTER TABLE sys_notification ADD COLUMN user_id BIGINT NOT NULL DEFAULT 0 COMMENT 'User ID' AFTER store_id");
            }

            // 9. Add read_status to sys_notification if missing
            try {
                stmt.execute("SELECT read_status FROM sys_notification LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding read_status column to sys_notification...");
                stmt.execute("ALTER TABLE sys_notification ADD COLUMN read_status INT DEFAULT 0 COMMENT 'Read Status' AFTER status");
            }

            // 10. Add target to sys_notification if missing
            try {
                stmt.execute("SELECT target FROM sys_notification LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding target column to sys_notification...");
                stmt.execute("ALTER TABLE sys_notification ADD COLUMN target VARCHAR(50) COMMENT 'Target' AFTER content");
            }

            // 11. Add error_msg to sys_notification if missing
            try {
                stmt.execute("SELECT error_msg FROM sys_notification LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding error_msg column to sys_notification...");
                stmt.execute("ALTER TABLE sys_notification ADD COLUMN error_msg TEXT COMMENT 'Error Message' AFTER read_status");
            }

            // 12. Add send_time to sys_notification if missing
            try {
                stmt.execute("SELECT send_time FROM sys_notification LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding send_time column to sys_notification...");
                stmt.execute("ALTER TABLE sys_notification ADD COLUMN send_time DATETIME COMMENT 'Send Time' AFTER create_time");
            }

            // 13. Fix recipient column in sys_notification if exists (legacy column)
            try {
                stmt.execute("SELECT recipient FROM sys_notification LIMIT 1");
                System.out.println("Modifying recipient column in sys_notification to be nullable...");
                stmt.execute("ALTER TABLE sys_notification MODIFY COLUMN recipient VARCHAR(50) NULL COMMENT 'Recipient'");
            } catch (Exception e) {
                // recipient column does not exist, which is good
            }

            // 13.5 Fix status column type in sys_notification if it is VARCHAR (legacy)
            try {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rsCol = meta.getColumns(null, null, "sys_notification", "status");
                if (rsCol.next()) {
                    String typeName = rsCol.getString("TYPE_NAME");
                    int dataType = rsCol.getInt("DATA_TYPE"); // java.sql.Types
                    
                    // Check if it's a string type (VARCHAR = 12, CHAR = 1, LONGVARCHAR = -1)
                    if (dataType == 12 || dataType == 1 || dataType == -1 || typeName != null && typeName.toUpperCase().contains("CHAR")) {
                        System.out.println("Detected sys_notification.status is VARCHAR (" + typeName + "). Migrating to INT...");
                        // Update legacy values first
                        try {
                            stmt.execute("UPDATE sys_notification SET status = '0' WHERE status = 'PENDING'");
                            stmt.execute("UPDATE sys_notification SET status = '1' WHERE status = 'SENT'");
                            stmt.execute("UPDATE sys_notification SET status = '2' WHERE status = 'FAILED'");
                        } catch (Exception ex) {
                            System.out.println("Warning during status value update: " + ex.getMessage());
                        }
                        // Modify column type
                        stmt.execute("ALTER TABLE sys_notification MODIFY COLUMN status INT DEFAULT 0 COMMENT 'Status: 0 Pending, 1 Sent, 2 Failed'");
                        System.out.println("sys_notification.status migrated to INT successfully.");
                    }
                }
                rsCol.close();
            } catch (Exception e) {
                System.out.println("Error migrating sys_notification.status: " + e.getMessage());
            }

            // 14. Add creator_id to ams_appointment if missing
            try {
                stmt.execute("SELECT creator_id FROM ams_appointment LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding creator_id column to ams_appointment...");
                stmt.execute("ALTER TABLE ams_appointment ADD COLUMN creator_id BIGINT COMMENT 'Creator User ID' AFTER customer_id");
            }

            // 15. Add new columns to ams_service
            try {
                stmt.execute("SELECT category FROM ams_service LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding new columns to ams_service...");
                stmt.execute("ALTER TABLE ams_service ADD COLUMN category VARCHAR(50) COMMENT 'Category'");
                stmt.execute("ALTER TABLE ams_service ADD COLUMN scenario VARCHAR(100) COMMENT 'Applicable Scenario'");
                stmt.execute("ALTER TABLE ams_service ADD COLUMN recommendation_score DECIMAL(5, 2) DEFAULT 0.00 COMMENT 'Recommendation Score'");
                stmt.execute("ALTER TABLE ams_service ADD COLUMN mutex_group VARCHAR(50) COMMENT 'Mutually Exclusive Group'");
            }

            // 16. Create ams_combo_rule table
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_combo_rule (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "store_id BIGINT COMMENT 'Store ID'," +
                    "name VARCHAR(100) NOT NULL COMMENT 'Rule Name'," +
                    "condition_json TEXT COMMENT 'Condition JSON (e.g. required service IDs)'," +
                    "discount_type INT COMMENT 'Discount Type (1: Rate, 2: Fixed Amount)'," +
                    "discount_value DECIMAL(10, 2) COMMENT 'Discount Value'," +
                    "status INT DEFAULT 1 COMMENT 'Status'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Combo Discount Rules'");

            // 17. Create ams_service_relation table
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_service_relation (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "parent_service_id BIGINT NOT NULL COMMENT 'Parent Service ID'," +
                    "child_service_id BIGINT NOT NULL COMMENT 'Child Service ID'," +
                    "sort_order INT DEFAULT 0 COMMENT 'Sort Order'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_parent (parent_service_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Service Relations'");

            // 18. Create ams_transaction table
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_transaction (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "store_id BIGINT COMMENT 'Store ID'," +
                    "appointment_id BIGINT NOT NULL COMMENT 'Appointment ID'," +
                    "trade_no VARCHAR(64) COMMENT 'Third-party Trade No'," +
                    "out_trade_no VARCHAR(64) NOT NULL UNIQUE COMMENT 'Out Trade No'," +
                    "amount DECIMAL(10, 2) NOT NULL COMMENT 'Payment Amount'," +
                    "payment_method VARCHAR(20) COMMENT 'ALIPAY, WECHAT, CASH, BALANCE'," +
                    "status VARCHAR(20) NOT NULL COMMENT 'PENDING, SUCCESS, FAILED, REFUNDED'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "pay_time DATETIME COMMENT 'Payment Time'," +
                    "INDEX idx_appointment (appointment_id)," +
                    "INDEX idx_out_trade_no (out_trade_no)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Transaction Records'");

            // 18.1 Add appointment_id to ams_transaction if missing (fix for existing table)
            try {
                stmt.execute("SELECT appointment_id FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding appointment_id column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN appointment_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Appointment ID' AFTER store_id");
                try {
                    stmt.execute("CREATE INDEX idx_appointment ON ams_transaction(appointment_id)");
                } catch (Exception ex) {
                    // Index might exist
                }
            }

            // 18.1 Add appointment_id to ams_transaction if missing (fix for existing table)
            try {
                stmt.execute("SELECT appointment_id FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding appointment_id column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN appointment_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Appointment ID' AFTER store_id");
                stmt.execute("CREATE INDEX idx_appointment ON ams_transaction(appointment_id)");
            }

            // 18.2 Add trade_no to ams_transaction if missing
            try {
                stmt.execute("SELECT trade_no FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding trade_no column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN trade_no VARCHAR(64) COMMENT 'Third-party Trade No' AFTER appointment_id");
            }

            // 18.3 Add out_trade_no to ams_transaction if missing
            try {
                stmt.execute("SELECT out_trade_no FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding out_trade_no column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN out_trade_no VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'Out Trade No' AFTER trade_no");
                try {
                    stmt.execute("CREATE UNIQUE INDEX idx_out_trade_no ON ams_transaction(out_trade_no)");
                } catch (Exception ex) {
                    // Index might already exist or data might duplicate
                    System.out.println("Warning: Could not create unique index on out_trade_no: " + ex.getMessage());
                }
            }

            // 18.4 Add payment_method to ams_transaction if missing
            try {
                stmt.execute("SELECT payment_method FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding payment_method column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN payment_method VARCHAR(20) COMMENT 'ALIPAY, WECHAT, CASH, BALANCE' AFTER amount");
            }

            // 18.5 Add status to ams_transaction if missing
            try {
                stmt.execute("SELECT status FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding status column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SUCCESS, FAILED, REFUNDED' AFTER payment_method");
            }

            // 18.6 Add pay_time to ams_transaction if missing
            try {
                stmt.execute("SELECT pay_time FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding pay_time column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN pay_time DATETIME COMMENT 'Payment Time' AFTER create_time");
            }

            // 18.7 Fix legacy columns in ams_transaction (make nullable)
            try {
                stmt.execute("SELECT appt_id FROM ams_transaction LIMIT 1");
                System.out.println("Making legacy column appt_id nullable...");
                stmt.execute("ALTER TABLE ams_transaction MODIFY COLUMN appt_id BIGINT NULL");
            } catch (Exception e) {
                // Column doesn't exist, which is fine
            }
            
            try {
                stmt.execute("SELECT type FROM ams_transaction LIMIT 1");
                System.out.println("Making legacy column type nullable...");
                stmt.execute("ALTER TABLE ams_transaction MODIFY COLUMN type VARCHAR(50) NULL");
            } catch (Exception e) {
                // Column doesn't exist, which is fine
            }
            
            try {
                stmt.execute("SELECT pay_method FROM ams_transaction LIMIT 1");
                System.out.println("Making legacy column pay_method nullable...");
                stmt.execute("ALTER TABLE ams_transaction MODIFY COLUMN pay_method VARCHAR(50) NULL");
            } catch (Exception e) {
                // Column doesn't exist, which is fine
            }



            // 19. Add payment_status to ams_appointment
            try {
                stmt.execute("SELECT payment_status FROM ams_appointment LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding payment_status column to ams_appointment...");
                stmt.execute("ALTER TABLE ams_appointment ADD COLUMN payment_status VARCHAR(20) DEFAULT 'UNPAID' COMMENT 'UNPAID, PAID, REFUNDED' AFTER status");
            }

            // 20. Add refund fields to ams_appointment
            try {
                stmt.execute("SELECT refund_status FROM ams_appointment LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding refund_status column to ams_appointment...");
                stmt.execute("ALTER TABLE ams_appointment ADD COLUMN refund_status INT DEFAULT 0 COMMENT '0-None, 1-Processing, 2-Completed, 3-Failed' AFTER payment_status");
            }

            // 21. Add new fields to ams_transaction
            // type
            try {
                stmt.execute("SELECT type FROM ams_transaction LIMIT 1");
                // Ensure column exists
            } catch (Exception e) {
                 System.out.println("Adding type column to ams_transaction...");
                 stmt.execute("ALTER TABLE ams_transaction ADD COLUMN type VARCHAR(50) DEFAULT 'PAYMENT' COMMENT 'PAYMENT, REFUND' AFTER status");
            }
            
            // related_transaction_id
            try {
                stmt.execute("SELECT related_transaction_id FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding related_transaction_id column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN related_transaction_id BIGINT COMMENT 'Related Original Transaction ID' AFTER type");
            }

            // reason
            try {
                stmt.execute("SELECT reason FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding reason column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN reason VARCHAR(255) COMMENT 'Refund Reason' AFTER related_transaction_id");
            }

            // operator
            try {
                stmt.execute("SELECT operator FROM ams_transaction LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding operator column to ams_transaction...");
                stmt.execute("ALTER TABLE ams_transaction ADD COLUMN operator VARCHAR(50) COMMENT 'Operator' AFTER reason");
            }

            // 22. Create ams_technician_shift table if not exists
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_technician_shift (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "tech_id BIGINT NOT NULL COMMENT 'Technician ID (sys_user.id)'," +
                    "shift_date DATE NOT NULL COMMENT 'Shift Date'," +
                    "start_time TIME NOT NULL COMMENT 'Start Time'," +
                    "end_time TIME NOT NULL COMMENT 'End Time'," +
                    "type VARCHAR(20) NOT NULL DEFAULT 'WORK' COMMENT 'Type: WORK, LEAVE, BREAK'," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "INDEX idx_tech_date (tech_id, shift_date)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Technician Shift'");

            // 23. Add bilingual columns to ams_service
            try {
                stmt.execute("SELECT name_cn FROM ams_service LIMIT 1");
            } catch (Exception e) {
                System.out.println("Adding bilingual columns to ams_service...");
                try {
                    stmt.execute("ALTER TABLE ams_service ADD COLUMN name_cn VARCHAR(255) COMMENT 'Service Name (CN)' AFTER name");
                    stmt.execute("ALTER TABLE ams_service ADD COLUMN name_jp VARCHAR(255) COMMENT 'Service Name (JP)' AFTER name_cn");
                    stmt.execute("ALTER TABLE ams_service ADD COLUMN description_cn TEXT COMMENT 'Description (CN)' AFTER description");
                    stmt.execute("ALTER TABLE ams_service ADD COLUMN description_jp TEXT COMMENT 'Description (JP)' AFTER description_cn");
                } catch (Exception ex) {
                    System.out.println("Failed to add bilingual columns: " + ex.getMessage());
                }
            }

            // 24. Create ams_leave_request table if not exists
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_leave_request (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "store_id BIGINT," +
                    "tech_id BIGINT NOT NULL," +
                    "type VARCHAR(20) NOT NULL COMMENT 'SICK, CASUAL, etc'," +
                    "start_time DATETIME NOT NULL," +
                    "end_time DATETIME NOT NULL," +
                    "reason VARCHAR(255)," +
                    "status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED'," +
                    "audit_by BIGINT," +
                    "audit_time DATETIME," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Technician Leave Request'");

            // 25. Create ams_attendance table if not exists
            stmt.execute("CREATE TABLE IF NOT EXISTS ams_attendance (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "store_id BIGINT," +
                    "tech_id BIGINT NOT NULL," +
                    "type VARCHAR(20) NOT NULL COMMENT 'CLOCK_IN, CLOCK_OUT'," +
                    "time DATETIME NOT NULL," +
                    "location VARCHAR(255)," +
                    "status VARCHAR(20)," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Technician Attendance'");

            // 26. Insert dummy data for leave request if empty
            var rsLeave = stmt.executeQuery("SELECT count(*) FROM ams_leave_request");
            if (rsLeave.next() && rsLeave.getInt(1) == 0) {
                System.out.println("Initializing leave request dummy data...");
                // Try to find a valid user ID (e.g. 1 or any existing ID)
                long techId = 1;
                try {
                    var rsUser = stmt.executeQuery("SELECT id FROM sys_user LIMIT 1");
                    if (rsUser.next()) {
                        techId = rsUser.getLong(1);
                    }
                } catch (Exception e) {
                    System.out.println("Could not find any user, using default ID 1");
                }
                
                stmt.execute("INSERT INTO ams_leave_request (store_id, tech_id, type, start_time, end_time, reason, status, create_time) VALUES " +
                        "(1, " + techId + ", 'SICK', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'Feeling unwell', 'PENDING', NOW())," +
                        "(1, " + techId + ", 'CASUAL', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 'Personal matter', 'APPROVED', NOW())");
            }

            System.out.println("Database initialized successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
