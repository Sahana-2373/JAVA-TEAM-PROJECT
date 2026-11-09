import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * One worker analyses one DMA record. Multiple workers run concurrently.
 */
public class LeakDetectionWorker implements Callable<WaterDAO.PipeRecord> {
    private final WaterDAO.PipeRecord pipe;
    private final WaterDAO dao;

    public LeakDetectionWorker(WaterDAO.PipeRecord pipe, WaterDAO dao) {
        this.pipe = pipe;
        this.dao = dao;
    }

    @Override
    public WaterDAO.PipeRecord call() throws Exception {
        double threshold;
        switch (pipe.category) {
            case "HIGH-PRESSURE TRUNK MAIN": threshold = 10.0; break;
            case "RESIDENTIAL DISTRIBUTION PIPE": threshold = 15.0; break;
            case "INDUSTRIAL FEEDER LINE": threshold = 12.0; break;
            default: threshold = 15.0;
        }

        boolean burst = pipe.flowVariance > threshold && pipe.acousticNoise > 65.0;
        pipe.score = calculateIntegrityScore(pipe, threshold);

        String alertType;
        String action;
        if (burst) {
            pipe.status = "BURST ISOLATION";
            alertType = "PIPE_BURST_EMERGENCY";
            action = "Automated valve shutoff; dispatch repair crew";
        } else if (pipe.score < 60) {
            pipe.status = "BURST ISOLATION";
            alertType = "CRITICAL_LEAK";
            action = "Isolate line and inspect immediately";
        } else if (pipe.score < 75) {
            pipe.status = "BAND C - HIGH NRW";
            alertType = "HIGH_NRW";
            action = "Apply pressure reduction and inspect";
        } else if (pipe.score < 90) {
            pipe.status = "BAND B - MINOR LOSS";
            alertType = "MINOR_LOSS";
            action = "Schedule maintenance inspection";
        } else {
            pipe.status = "BAND A - ZERO/MINIMAL LEAKAGE";
            alertType = "NORMAL";
            action = "Continue routine monitoring";
        }

        dao.saveAlert(pipe, alertType, action);
        System.out.println(Thread.currentThread().getName() + " analysed " +
                pipe.dmaCode + " | variance=" + String.format("%.2f", pipe.flowVariance) +
                "% | score=" + String.format("%.2f", pipe.score) +
                " | " + pipe.status);
        return pipe;
    }

    private double calculateIntegrityScore(WaterDAO.PipeRecord p, double threshold) {
        // Each risk is normalized to a 0-100 scale. These are practical
        // assignment assumptions because all sensor scales are not specified.
        double flowRisk = Math.min(100, (p.flowVariance / Math.max(threshold, 0.1)) * 50);
        double pressureRisk = Math.min(100, Math.abs(p.pressureDifferential) * 20);
        double acousticRisk = Math.min(100, Math.max(0, p.acousticNoise - 30) * 2);
        double meterRisk = p.inflow <= 0 ? 0 :
                Math.min(100, Math.abs(p.inflow - p.outflow) / p.inflow * 100);
        double chemicalRisk = Math.min(100, Math.max(0, p.groundMoisture));
        double risk;

        switch (p.category) {
            case "HIGH-PRESSURE TRUNK MAIN":
                risk = 0.50 * flowRisk + 0.30 * acousticRisk + 0.20 * pressureRisk;
                break;
            case "INDUSTRIAL FEEDER LINE":
                risk = 0.45 * flowRisk + 0.35 * chemicalRisk + 0.20 * pressureRisk;
                break;
            default:
                risk = 0.40 * flowRisk + 0.40 * pressureRisk + 0.20 * meterRisk;
        }
        return Math.max(0, Math.min(100, 100 - risk));
    }
}
