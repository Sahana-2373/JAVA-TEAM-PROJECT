SMART GRID MUNICIPAL WATER LOSS & LEAKAGE DIAGNOSTICS NETWORK
===========================================================

This is a beginner-friendly Java + MySQL starter project.

REQUIREMENTS
------------
1. JDK installed (Java 17 or later recommended).
2. MySQL Server installed and running.
3. MySQL Connector/J JAR downloaded.
4. VS Code or another Java IDE.

PROJECT FILES
-------------
src/DBConnection.java          MySQL JDBC connection
src/WaterDAO.java              Database insert and query methods
src/LeakDetectionWorker.java   Multi-threaded leakage analysis
src/AuditExporter.java         CSV report generation
src/MainApp.java               Console menu and file import
smart_water_schema.sql         Database and table creation
water_telemetry.txt            Sample input telemetry
reports/                       Generated CSV reports

STEP 1: CREATE DATABASE
-----------------------
Open MySQL Workbench or MySQL Command Line and execute:
    SOURCE C:/path/to/SmartWaterLeakageSystem/smart_water_schema.sql;

Or open the SQL file and run all its statements in Workbench.

STEP 2: ADD MYSQL CONNECTOR
---------------------------
Download MySQL Connector/J from the official MySQL website.
Copy the connector JAR into this project's lib folder.
The JAR filename may look like mysql-connector-j-9.x.x.jar.

STEP 3: SET DATABASE PASSWORD
-----------------------------
DBConnection.java uses these defaults:
    URL: jdbc:mysql://localhost:3306/smart_water_db
    USER: root
    PASSWORD: root

Change the default password in DBConnection.java to your own MySQL password,
or set DB_URL, DB_USER and DB_PASSWORD environment variables.

STEP 4: COMPILE AND RUN ON WINDOWS POWERSHELL
---------------------------------------------
Open PowerShell in the project folder. Replace the JAR name below with
the actual connector filename.

    mkdir out
    javac -cp "lib\mysql-connector-j-9.x.x.jar" -d out src\*.java
    java -cp "out;lib\mysql-connector-j-9.x.x.jar" MainApp

STEP 5: RUN THE PROJECT
-----------------------
Choose menu option 4 to import the sample water_telemetry.txt file.
Then choose option 5 to run concurrent analysis and create:
    reports/daily_water_balance.csv
    reports/emergency_shutoff_orders.csv

You can also enter Phase 1, Phase 2 and Phase 3 manually using menu options 1-3.

IMPORTANT ASSIGNMENT NOTES
--------------------------
1. DMA validation uses: DMA-[A-Z]{3}-\\d{3}
2. String.substring() extracts a short sensor identifier.
3. String.replaceAll() cleans input notes/CSV spacing.
4. String.format() formats analysis messages.
5. ExecutorService runs one analysis worker per DMA record.
6. The burst rule is implemented as flow variance above the category threshold
   AND acoustic noise greater than 65 dB.
7. The supplied assignment does not define normalization formulas for all
   sensor scores. The score calculation in LeakDetectionWorker.java is an
   illustrative implementation; confirm the scoring assumptions with your
   faculty before final submission.
8. The brief mentions a chemical sensor score for industrial pipes, but Phase 3
   lists ground moisture rather than a chemical reading. This starter uses the
   ground moisture value as a temporary proxy. Replace it with a chemical
   sensor field if your faculty expects a separate chemical sensor.
9. NRW formula in the assignment is:
   NRW % = ((System Input Volume - Billed Authorized Volume)
            / System Input Volume) * 100
   Ensure input and billed values use the same time period and units.
10. ILI = CARL / UARL. CARL and UARL are not included in the supplied phases,
    so ILI is not calculated until those fields are added.

OCI CLOUD DEPLOYMENT
--------------------
First test locally. For the two-VM setup:
- VM 1: Java application and MySQL Connector/J.
- VM 2: MySQL database.
- Configure MySQL bind-address to the private network interface on VM 2.
- Create a dedicated database user limited to VM 1's private IP.
- Allow TCP port 3306 only from VM 1's private IP in the VM 2 OS firewall
  and OCI security rules. Do not expose MySQL port 3306 to the public internet.
- Set DB_URL on VM 1 to jdbc:mysql://<VM2_PRIVATE_IP>:3306/smart_water_db
- Keep database credentials out of source control.

This ZIP does not create OCI resources or configure cloud networking automatically.
