CREATE DATABASE IF NOT EXISTS smart_water_db;
USE smart_water_db;

CREATE TABLE IF NOT EXISTS dma_zones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dma_code VARCHAR(20) NOT NULL UNIQUE,
    pipe_segment_code VARCHAR(40) NOT NULL,
    pipe_category ENUM(
        'HIGH-PRESSURE TRUNK MAIN',
        'RESIDENTIAL DISTRIBUTION PIPE',
        'INDUSTRIAL FEEDER LINE'
    ) NOT NULL,
    nominal_pressure_bar DECIMAL(10,2) NOT NULL,
    pipe_age_years INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flow_telemetry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dma_code VARCHAR(20) NOT NULL,
    inflow_lps DECIMAL(12,3) NOT NULL,
    outflow_lps DECIMAL(12,3) NOT NULL,
    acoustic_noise_db DECIMAL(10,2) NOT NULL,
    pressure_differential_bar DECIMAL(10,3) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_telemetry_dma (dma_code),
    CONSTRAINT fk_telemetry_dma FOREIGN KEY (dma_code)
        REFERENCES dma_zones(dma_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS leak_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dma_code VARCHAR(20) NOT NULL,
    flow_variance_percent DECIMAL(10,2) NOT NULL,
    integrity_score DECIMAL(6,2) NOT NULL,
    alert_type VARCHAR(40) NOT NULL,
    recommended_action VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alert_dma (dma_code),
    CONSTRAINT fk_alert_dma FOREIGN KEY (dma_code)
        REFERENCES dma_zones(dma_code) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS water_audits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dma_code VARCHAR(20) NOT NULL,
    billed_consumption_liters DECIMAL(14,2) NOT NULL,
    unaccounted_water_liters DECIMAL(14,2) NOT NULL,
    ground_moisture_reading DECIMAL(10,2) NOT NULL,
    audit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_dma (dma_code),
    CONSTRAINT fk_audit_dma FOREIGN KEY (dma_code)
        REFERENCES dma_zones(dma_code) ON DELETE CASCADE
);
