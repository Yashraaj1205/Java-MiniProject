package com.lendflow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SchemaFixer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Database Schema Fixer...");
        
        try {
            // Adding missing columns to 'loans' table if they don't exist
            executeSafe("ALTER TABLE loans ADD COLUMN IF NOT EXISTS repayment_type VARCHAR(255) DEFAULT 'EMI'");
            executeSafe("ALTER TABLE loans ADD COLUMN IF NOT EXISTS tenure_months INTEGER DEFAULT 12");
            executeSafe("ALTER TABLE loans ADD COLUMN IF NOT EXISTS late_fee_per_day DECIMAL DEFAULT 0");
            executeSafe("ALTER TABLE loans ADD COLUMN IF NOT EXISTS late_fee_flat DECIMAL DEFAULT 0");
            executeSafe("ALTER TABLE loans ADD COLUMN IF NOT EXISTS purpose VARCHAR(255)");
            executeSafe("ALTER TABLE loans ADD COLUMN IF NOT EXISTS end_date DATE");
            executeSafe("ALTER TABLE loans ADD COLUMN IF NOT EXISTS next_due_date DATE");
            
            // Adding missing columns to 'payments' table
            executeSafe("ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_method VARCHAR(255) DEFAULT 'Cash'");
            executeSafe("ALTER TABLE payments ADD COLUMN IF NOT EXISTS notes VARCHAR(500)");
            executeSafe("ALTER TABLE payments ADD COLUMN IF NOT EXISTS late_fee DECIMAL DEFAULT 0");
            executeSafe("ALTER TABLE payments ADD COLUMN IF NOT EXISTS interest_component DECIMAL DEFAULT 0");
            executeSafe("ALTER TABLE payments ADD COLUMN IF NOT EXISTS principal_component DECIMAL DEFAULT 0");

            // Fix existing NULL values
            jdbcTemplate.execute("UPDATE loans SET repayment_type = 'EMI' WHERE repayment_type IS NULL");
            jdbcTemplate.execute("UPDATE loans SET tenure_months = 12 WHERE tenure_months IS NULL");
            jdbcTemplate.execute("UPDATE loans SET late_fee_per_day = 0 WHERE late_fee_per_day IS NULL");
            jdbcTemplate.execute("UPDATE loans SET late_fee_flat = 0 WHERE late_fee_flat IS NULL");
            
            jdbcTemplate.execute("UPDATE payments SET payment_method = 'Cash' WHERE payment_method IS NULL");
            jdbcTemplate.execute("UPDATE payments SET late_fee = 0 WHERE late_fee IS NULL");
            jdbcTemplate.execute("UPDATE payments SET interest_component = 0 WHERE interest_component IS NULL");
            jdbcTemplate.execute("UPDATE payments SET principal_component = 0 WHERE principal_component IS NULL");
            
            // Adding missing columns to 'borrowers' table
            executeSafe("ALTER TABLE borrowers ADD COLUMN IF NOT EXISTS business_name VARCHAR(255)");
            executeSafe("ALTER TABLE borrowers ADD COLUMN IF NOT EXISTS business_type VARCHAR(255)");
            executeSafe("ALTER TABLE borrowers ADD COLUMN IF NOT EXISTS email VARCHAR(255)");
            executeSafe("ALTER TABLE borrowers ADD COLUMN IF NOT EXISTS identity_proof VARCHAR(255)");
            executeSafe("ALTER TABLE borrowers ADD COLUMN IF NOT EXISTS date_added DATE");

            log.info("Database Schema Fixer completed successfully!");
        } catch (Exception e) {
            log.error("Database Schema Fixer encountered an error: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeSafe(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            // Already caught by ADD COLUMN IF NOT EXISTS in most Postgres versions,
            // but we log just in case.
            log.warn("SQL Execution warning: {}", e.getMessage());
        }
    }
}
