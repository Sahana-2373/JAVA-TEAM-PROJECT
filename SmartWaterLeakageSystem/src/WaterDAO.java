import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object: all database operations are kept here.
 */
public class WaterDAO {

    public static class PipeRecord {
        public int id;
        public String dmaCode, segmentCode, category;
        public double nominalPressure;
        public int pipeAge;
        public double inflow, outflow, acousticNoise, pressureDifferential;
        public double billedConsumption, unaccountedWater, groundMoisture;
        public double flowVariance, score;
        public String status;
    }

    public void addPipePhase1(String dma, String segment, String category,
                              double nominalPressure, int pipeAge) throws SQLException {
        String sql = "INSERT INTO dma_zones " +
                "(dma_code, pipe_segment_code, pipe_category, nominal_pressure_bar, pipe_age_years) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE pipe_segment_code=VALUES(pipe_segment_code), " +
                "pipe_category=VALUES(pipe_category), nominal_pressure_bar=VALUES(nominal_pressure_bar), " +
                "pipe_age_years=VALUES(pipe_age_years)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dma);
            ps.setString(2, segment);
            ps.setString(3, category);
            ps.setDouble(4, nominalPressure);
            ps.setInt(5, pipeAge);
            ps.executeUpdate();
        }
    }

    public void addTelemetryPhase2(String dma, double in, double out,
                                   double noise, double pressureDiff) throws SQLException {
        String sql = "INSERT INTO flow_telemetry " +
                "(dma_code, inflow_lps, outflow_lps, acoustic_noise_db, pressure_differential_bar) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dma);
            ps.setDouble(2, in);
            ps.setDouble(3, out);
            ps.setDouble(4, noise);
            ps.setDouble(5, pressureDiff);
            ps.executeUpdate();
        }
    }

    public void addAuditPhase3(String dma, double billed, double unaccounted,
                               double moisture) throws SQLException {
        String sql = "INSERT INTO water_audits " +
                "(dma_code, billed_consumption_liters, unaccounted_water_liters, ground_moisture_reading) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dma);
            ps.setDouble(2, billed);
            ps.setDouble(3, unaccounted);
            ps.setDouble(4, moisture);
            ps.executeUpdate();
        }
    }

    public List<PipeRecord> getLatestRecords() throws SQLException {
        String sql = "SELECT z.id, z.dma_code, z.pipe_segment_code, z.pipe_category, " +
                "z.nominal_pressure_bar, z.pipe_age_years, t.inflow_lps, t.outflow_lps, " +
                "t.acoustic_noise_db, t.pressure_differential_bar, " +
                "COALESCE(a.billed_consumption_liters,0) billed, " +
                "COALESCE(a.unaccounted_water_liters,0) unaccounted, " +
                "COALESCE(a.ground_moisture_reading,0) moisture " +
                "FROM dma_zones z " +
                "JOIN (SELECT dma_code, MAX(id) id FROM flow_telemetry GROUP BY dma_code) x " +
                "ON x.dma_code=z.dma_code " +
                "JOIN flow_telemetry t ON t.id=x.id " +
                "LEFT JOIN (SELECT dma_code, MAX(id) id FROM water_audits GROUP BY dma_code) y " +
                "ON y.dma_code=z.dma_code " +
                "LEFT JOIN water_audits a ON a.id=y.id ORDER BY z.dma_code";
        List<PipeRecord> records = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PipeRecord p = new PipeRecord();
                p.id = rs.getInt("id");
                p.dmaCode = rs.getString("dma_code");
                p.segmentCode = rs.getString("pipe_segment_code");
                p.category = rs.getString("pipe_category");
                p.nominalPressure = rs.getDouble("nominal_pressure_bar");
                p.pipeAge = rs.getInt("pipe_age_years");
                p.inflow = rs.getDouble("inflow_lps");
                p.outflow = rs.getDouble("outflow_lps");
                p.acousticNoise = rs.getDouble("acoustic_noise_db");
                p.pressureDifferential = rs.getDouble("pressure_differential_bar");
                p.billedConsumption = rs.getDouble("billed");
                p.unaccountedWater = rs.getDouble("unaccounted");
                p.groundMoisture = rs.getDouble("moisture");
                p.flowVariance = p.inflow <= 0 ? 0 :
                        Math.abs(p.inflow - p.outflow) / p.inflow * 100.0;
                records.add(p);
            }
        }
        return records;
    }

    public void saveAlert(PipeRecord p, String alertType, String action) throws SQLException {
        String sql = "INSERT INTO leak_alerts " +
                "(dma_code, flow_variance_percent, integrity_score, alert_type, recommended_action) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.dmaCode);
            ps.setDouble(2, p.flowVariance);
            ps.setDouble(3, p.score);
            ps.setString(4, alertType);
            ps.setString(5, action);
            ps.executeUpdate();
        }
    }
}
