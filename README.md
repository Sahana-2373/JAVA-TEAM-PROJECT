# 💧 SMART GRID MUNICIPAL WATER LOSS & LEAKAGE DIAGNOSTICS NETWORK

### A Java and MySQL Based Smart Water Management System

## 📌 PROJECT OVERVIEW

The **Smart Grid Municipal Water Loss & Leakage Diagnostics Network** is a Java-based application integrated with MySQL that helps identify water leakage, monitor water distribution, and analyze water loss in municipal water supply networks.

The system processes water telemetry data, performs multi-threaded leakage analysis, identifies abnormal water flow conditions, and generates CSV reports to support efficient water management.

It aims to reduce Non-Revenue Water (NRW), minimize water wastage, and improve the reliability of municipal water distribution systems.

## 🎯 PROJECT OBJECTIVES

- Detect water leakage in municipal water distribution networks.
- Monitor water flow and sensor readings.
- Identify abnormal flow variations and possible pipe bursts.
- Analyze water loss using water balance calculations.
- Generate automated reports for water management.
- Improve the efficiency and reliability of water distribution.
- Support future cloud deployment using Oracle Cloud Infrastructure (OCI).

## ✨ KEY FEATURES

1. **Water Leakage Detection** – Identifies possible leakage conditions using sensor readings.
2. **Water Telemetry Import** – Imports sample water sensor data from text files.
3. **Multi-threaded Analysis** – Uses Java ExecutorService to analyze multiple DMA records concurrently.
4. **Database Integration** – Stores and retrieves water monitoring information using MySQL.
5. **Water Loss Analysis** – Calculates Non-Revenue Water (NRW) percentage.
6. **Emergency Shutoff Reports** – Generates reports for possible emergency shutoff actions.
7. **CSV Report Generation** – Exports daily water balance and emergency shutoff reports.
8. **Data Validation** – Validates DMA identifiers and cleans input data.
9. **Cloud Deployment Support** – Provides configuration guidance for deploying the application using two OCI virtual machines.

## 🛠️ TECHNOLOGIES USED

| Technology | Purpose |
|---|---|
| Java 17+ | Application development |
| MySQL | Database management |
| JDBC | Java–MySQL connectivity |
| MySQL Connector/J | Database driver |
| ExecutorService | Multi-threaded processing |
| CSV | Report generation |
| Oracle Cloud Infrastructure (OCI) | Cloud deployment |

## 📂 PROJECT STRUCTURE

```text
SmartWaterLeakageSystem/
│
├── src/
│   ├── DBConnection.java
│   ├── WaterDAO.java
│   ├── LeakDetectionWorker.java
│   ├── AuditExporter.java
│   └── MainApp.java
│
├── lib/
│   └── mysql-connector-j-9.x.x.jar
│
├── reports/
│
├── smart_water_schema.sql
├── water_telemetry.txt
└── README.md
```

### Description of Java Files

- **DBConnection.java:** Establishes a connection between Java and MySQL using JDBC.
- **WaterDAO.java:** Handles database insertion and query operations.
- **LeakDetectionWorker.java:** Performs multi-threaded leakage analysis.
- **AuditExporter.java:** Generates CSV reports.
- **MainApp.java:** Provides the console menu and handles file import and application execution.

## 📋 REQUIREMENTS

Before running the project, make sure you have:

1. JDK 17 or later installed.
2. MySQL Server installed and running.
3. MySQL Workbench (recommended).
4. MySQL Connector/J downloaded.
5. VS Code or another Java IDE.
6. Windows PowerShell or a terminal.

## ⚙️ INSTALLATION AND EXECUTION

### STEP 1: Clone the Repository

Open PowerShell and execute:

```powershell
git clone https://github.com/Sahana-2373/JAVA-TEAM-PROJECT.git
```

Navigate to the project folder:

```powershell
cd JAVA-TEAM-PROJECT/SmartWaterLeakageSystem
```

### STEP 2: Create the Database

Open MySQL Workbench or MySQL Command Line.

Execute the SQL file:

```sql
SOURCE C:/path/to/SmartWaterLeakageSystem/smart_water_schema.sql;
```

Alternatively, open `smart_water_schema.sql` in MySQL Workbench and execute all statements.

This creates the database and tables required by the application.

### STEP 3: Configure MySQL Connector

Download MySQL Connector/J from the official MySQL website.

Place the downloaded JAR file inside the project's `lib` folder.

Example:

```text
lib/mysql-connector-j-9.x.x.jar
```

### STEP 4: Configure Database Connection

Open `DBConnection.java`.

Default configuration:

```java
URL = "jdbc:mysql://localhost:3306/smart_water_db";
USER = "root";
PASSWORD = "root";
```

Change the password according to your local MySQL configuration.

Alternatively, configure the following environment variables:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

Do not upload real database passwords to GitHub.

### STEP 5: Compile the Java Program

Open PowerShell inside the project folder.

Create an output directory:

```powershell
mkdir out
```

Compile the Java files:

```powershell
javac -cp "lib\mysql-connector-j-9.x.x.jar" -d out src\*.java
```

### STEP 6: Run the Application

Execute:

```powershell
java -cp "out;lib\mysql-connector-j-9.x.x.jar" MainApp
```

Replace the example JAR filename with your actual downloaded filename.

## ▶️ HOW TO USE THE APPLICATION

After running the program, a console menu will appear.

- **Option 1:** Enter Phase 1 details manually.
- **Option 2:** Enter Phase 2 details manually.
- **Option 3:** Enter Phase 3 details manually.
- **Option 4:** Import sample data from `water_telemetry.txt`.
- **Option 5:** Perform concurrent leakage analysis and generate reports.

For the sample execution:

1. Run the Java application.
2. Select menu option 4 to import telemetry data.
3. Select menu option 5 to perform leakage analysis.
4. Check the generated CSV reports inside the `reports` folder.

## 📊 GENERATED REPORTS

The application generates the following reports:

```text
reports/
│
├── daily_water_balance.csv
└── emergency_shutoff_orders.csv
```

**daily_water_balance.csv**

Contains water balance information used to analyze water distribution and water loss.

**emergency_shutoff_orders.csv**

Contains information about potential emergency shutoff actions identified during leakage analysis.

## 🧠 IMPORTANT ASSIGNMENT CONCEPTS

The project demonstrates the following Java concepts and assignment requirements:

1. **Regular Expressions:** DMA validation uses `DMA-[A-Z]{3}-\d{3}`.
2. **String.substring():** Extracts a short sensor identifier.
3. **String.replaceAll():** Cleans input notes and CSV spacing.
4. **String.format():** Formats analysis messages.
5. **ExecutorService:** Executes one analysis worker per DMA record.
6. **Leakage Detection Rule:** Identifies burst conditions when flow variance exceeds the category threshold AND acoustic noise is greater than 65 dB.
7. **Sensor Score Calculation:** Uses an illustrative scoring implementation because the assignment does not define all normalization formulas. Confirm the assumptions with faculty before final submission.
8. **Industrial Pipe Monitoring:** Ground moisture is temporarily used as a proxy for the chemical sensor score. A separate chemical sensor field should be added if required by the assignment.
9. **Non-Revenue Water (NRW):**

   NRW percentage is calculated using:

   NRW % = ((System Input Volume - Billed Authorized Volume) / System Input Volume) × 100

   Both values must use the same time period and measurement units.

10. **Infrastructure Leakage Index (ILI):**

    ILI = CARL / UARL

    CARL and UARL are not included in the supplied project phases. Therefore, ILI calculation requires these additional fields.

## ☁️ OCI CLOUD DEPLOYMENT

The application can be deployed using Oracle Cloud Infrastructure (OCI) with two virtual machines.

**VM 1 – Application Server**
- Java application.
- JDK installation.
- MySQL Connector/J.

**VM 2 – Database Server**
- MySQL Server.
- Project database.
- Required database tables.

### Deployment Configuration

1. Configure MySQL bind-address on VM 2 to use its private network interface.
2. Create a dedicated MySQL database user.
3. Allow database access only from VM 1's private IP address.
4. Configure TCP port 3306 in the VM 2 firewall and OCI security rules.
5. Do not expose MySQL port 3306 to the public internet.
6. Configure the database URL on VM 1:

```text
jdbc:mysql://<VM2_PRIVATE_IP>:3306/smart_water_db
```

7. Store database credentials securely using environment variables.

**Note:** The project provides deployment guidance but does not automatically create OCI resources or configure cloud networking.

## 🔮 FUTURE ENHANCEMENTS

- Real-time water leakage monitoring.
- IoT sensor integration.
- Automated SMS and email alerts.
- Web-based monitoring dashboard.
- Advanced machine learning-based leakage prediction.
- GIS-based water pipeline visualization.
- Automated cloud monitoring and reporting.

## 🎓 CONCLUSION

The Smart Grid Municipal Water Loss & Leakage Diagnostics Network provides a practical approach to monitoring municipal water distribution and identifying potential leakage problems.

By combining Java programming, MySQL database management, multi-threaded processing, and automated report generation, the system helps improve water management and reduce water wastage.

The project also provides a foundation for future IoT integration and cloud-based smart water management solutions.

---

## 👥 PROJECT INFORMATION

**Project Name:** Smart Grid Municipal Water Loss & Leakage Diagnostics Network

**Project Type:** Java and MySQL Team Project

**Repository:** JAVA-TEAM-PROJECT

**GitHub:** https://github.com/Sahana-2373/JAVA-TEAM-PROJECT
            https://github.com/sangavi2007-sd/JAVA-TEAM-PROJECT
---

*Developed as an academic team project.*
