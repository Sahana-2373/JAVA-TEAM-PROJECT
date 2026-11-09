import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/** Writes the analysed water balance and emergency orders to CSV files. */
public class AuditExporter {

    public static void exportDailyBalance(List<WaterDAO.PipeRecord> records) throws IOException {
        File dir = new File("reports");
        if (!dir.exists()) dir.mkdirs();

        try (FileWriter writer = new FileWriter(new File(dir, "daily_water_balance.csv"))) {
            writer.write("DMA Zone,Pipe Segment,Category,Inflow Lps,Outflow Lps,Flow Variance %,Integrity Score,Status,NRW %\\n");
            for (WaterDAO.PipeRecord p : records) {
                double nrw = p.inflow <= 0 ? 0 :
                        Math.max(0, (p.inflow - p.billedConsumption) / p.inflow * 100.0);
                writer.write(String.format("%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%s,%.2f%n",
                        csv(p.dmaCode), csv(p.segmentCode), csv(p.category),
                        p.inflow, p.outflow, p.flowVariance, p.score, csv(p.status), nrw));
            }
        }

        try (FileWriter writer = new FileWriter(new File(dir, "emergency_shutoff_orders.csv"))) {
            writer.write("DMA Zone,Alert,Required Action,Flow Variance %,Acoustic Noise dB\\n");
            for (WaterDAO.PipeRecord p : records) {
                if ("BURST ISOLATION".equals(p.status)) {
                    writer.write(String.format("%s,PIPE_BURST_EMERGENCY,Automated valve shutoff and dispatch repair crew,%.2f,%.2f%n",
                            csv(p.dmaCode), p.flowVariance, p.acousticNoise));
                }
            }
        }
        System.out.println("CSV reports created inside the reports folder.");
    }

    private static String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
