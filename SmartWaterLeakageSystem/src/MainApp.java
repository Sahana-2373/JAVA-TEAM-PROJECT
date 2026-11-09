import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Smart Grid Municipal Water Loss & Leakage Diagnostics Network
 *
 * Telemetry file format (one row per line):
 * DMA-ABC-001,SEG-001,HIGH-PRESSURE TRUNK MAIN,8.0,20,100,85,70,1.2,50000,5000,35
 *
 * Fields: DMA, segment, category, nominal pressure bar, pipe age years,
 * inflow L/s, outflow L/s, acoustic dB, pressure differential bar,
 * billed liters, unaccounted liters, ground moisture score (0-100).
 */
public class MainApp {
    private static final Scanner sc = new Scanner(System.in);
    private static final WaterDAO dao = new WaterDAO();

    public static void main(String[] args) {
        System.out.println("=== SMART WATER LOSS & LEAKAGE DIAGNOSTICS ===");
        System.out.println("Before running, create the database using smart_water_schema.sql.");
        while (true) {
            System.out.println("\\n1. Add DMA / Pipe details (Phase 1)");
            System.out.println("2. Add telemetry (Phase 2)");
            System.out.println("3. Add water audit (Phase 3)");
            System.out.println("4. Import all phases from water_telemetry.txt");
            System.out.println("5. Run multithreaded leak analysis and export CSV");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": phase1(); break;
                    case "2": phase2(); break;
                    case "3": phase3(); break;
                    case "4": importFile(); break;
                    case "5": analyse(); break;
                    case "0": System.out.println("Thank you."); return;
                    default: System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
                System.out.println("Check the database connection and input values.");
            }
        }
    }

    private static void phase1() throws SQLException {
        System.out.print("DMA code (DMA-ABC-001): ");
        String dma = sc.nextLine().trim().toUpperCase();
        if (!dma.matches("DMA-[A-Z]{3}-\\d{3}")) {
            System.out.println("Invalid DMA code. Example: DMA-ABC-001");
            return;
        }
        System.out.print("Pipe segment code: ");
        String segment = sc.nextLine().trim();
        System.out.println("1. HIGH-PRESSURE TRUNK MAIN");
        System.out.println("2. RESIDENTIAL DISTRIBUTION PIPE");
        System.out.println("3. INDUSTRIAL FEEDER LINE");
        System.out.print("Category: ");
        String category = categoryFrom(sc.nextLine().trim());
        if (category == null) { System.out.println("Invalid category."); return; }
        System.out.print("Nominal pressure (bar): ");
        double pressure = Double.parseDouble(sc.nextLine());
        System.out.print("Pipe age (years): ");
        int age = Integer.parseInt(sc.nextLine());
        dao.addPipePhase1(dma, segment, category, pressure, age);
        System.out.println("Phase 1 details saved.");
    }

    private static void phase2() throws SQLException {
        String dma = askDma();
        if (dma == null) return;
        System.out.print("Inflow volume (L/s): "); double in = Double.parseDouble(sc.nextLine());
        System.out.print("Outflow volume (L/s): "); double out = Double.parseDouble(sc.nextLine());
        System.out.print("Acoustic noise (dB): "); double noise = Double.parseDouble(sc.nextLine());
        System.out.print("Pressure differential (bar): "); double diff = Double.parseDouble(sc.nextLine());
        dao.addTelemetryPhase2(dma, in, out, noise, diff);
        System.out.println("Phase 2 telemetry saved.");
    }

    private static void phase3() throws SQLException {
        String dma = askDma();
        if (dma == null) return;
        System.out.print("Billed consumption (liters): "); double billed = Double.parseDouble(sc.nextLine());
        System.out.print("Unaccounted water volume (liters): "); double unaccounted = Double.parseDouble(sc.nextLine());
        System.out.print("Ground moisture reading (0-100): "); double moisture = Double.parseDouble(sc.nextLine());
        dao.addAuditPhase3(dma, billed, unaccounted, moisture);
        System.out.println("Phase 3 audit saved.");
    }

    private static String askDma() {
        System.out.print("DMA code: ");
        String dma = sc.nextLine().trim().toUpperCase();
        if (!dma.matches("DMA-[A-Z]{3}-\\d{3}")) {
            System.out.println("Invalid DMA code. Example: DMA-ABC-001");
            return null;
        }
        return dma;
    }

    private static String categoryFrom(String value) {
        switch (value) {
            case "1": return "HIGH-PRESSURE TRUNK MAIN";
            case "2": return "RESIDENTIAL DISTRIBUTION PIPE";
            case "3": return "INDUSTRIAL FEEDER LINE";
            default: return null;
        }
    }

    private static void importFile() throws IOException, SQLException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("water_telemetry.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().replaceAll("\\s*,\\s*", ",");
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] a = line.split(",", -1);
                if (a.length != 12) {
                    System.out.println("Skipped invalid row: " + line);
                    continue;
                }
                String dma = a[0].toUpperCase();
                if (!dma.matches("DMA-[A-Z]{3}-\\d{3}")) {
                    System.out.println("Skipped invalid DMA code: " + dma);
                    continue;
                }
                String category = a[2].toUpperCase();
                if (!category.equals("HIGH-PRESSURE TRUNK MAIN") &&
                    !category.equals("RESIDENTIAL DISTRIBUTION PIPE") &&
                    !category.equals("INDUSTRIAL FEEDER LINE")) {
                    System.out.println("Skipped unknown category: " + category);
                    continue;
                }
                String segment = a[1].replaceAll("[^A-Za-z0-9_-]", "");
                // Demonstrate substring-based sensor ID extraction.
                String sensorId = segment.length() >= 3 ? segment.substring(0, 3) : segment;
                System.out.println(String.format("Reading sensor %s for %s", sensorId, dma));

                dao.addPipePhase1(dma, segment, category,
                        Double.parseDouble(a[3]), Integer.parseInt(a[4]));
                dao.addTelemetryPhase2(dma, Double.parseDouble(a[5]),
                        Double.parseDouble(a[6]), Double.parseDouble(a[7]),
                        Double.parseDouble(a[8]));
                dao.addAuditPhase3(dma, Double.parseDouble(a[9]),
                        Double.parseDouble(a[10]), Double.parseDouble(a[11]));
                count++;
            }
        }
        System.out.println("Imported " + count + " telemetry row(s).");
    }

    private static void analyse() throws SQLException, InterruptedException, ExecutionException, IOException {
        List<WaterDAO.PipeRecord> records = dao.getLatestRecords();
        if (records.isEmpty()) {
            System.out.println("No telemetry found. Enter data or import water_telemetry.txt first.");
            return;
        }

        ExecutorService pool = Executors.newFixedThreadPool(
                Math.min(records.size(), Runtime.getRuntime().availableProcessors() + 1));
        try {
            List<Future<WaterDAO.PipeRecord>> futures = new ArrayList<>();
            for (WaterDAO.PipeRecord p : records) {
                futures.add(pool.submit(new LeakDetectionWorker(p, dao)));
            }
            List<WaterDAO.PipeRecord> analysed = new ArrayList<>();
            for (Future<WaterDAO.PipeRecord> f : futures) analysed.add(f.get());
            AuditExporter.exportDailyBalance(analysed);
        } finally {
            pool.shutdown();
        }
    }
}
