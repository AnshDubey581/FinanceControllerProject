Finance Controller Project
📌 Overview
A Java + JavaFX based reconciliation tool that detects mismatches between transaction and settlement files, and generates exception reports.
This project helps finance teams save time and improve accuracy by automating manual reconciliation.

🎯 Objectives
Automate reconciliation of finance records.

Detect missing settlements, duplicate entries, and amount mismatches.

Generate clear exception reports for quick resolution.

✨ Features
CSV file input support (transactions.csv, settlements.csv).

Exception report generation in Excel/CSV format.

JavaFX UI (FinanceUI) for visualization.

Extendable for API integration (future scope).

🪜 How to Run
bash
# 1. Clone the repo
git clone https://github.com/AnshDubey581/FinanceControllerProject.git

# 2. Go inside project folder
cd FinanceControllerProject/Finance-Controller-Project

# 3. Compile with JavaFX + OpenCSV
javac --module-path "libraryfx\javafx-sdk-26.0.2\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "library/*" -d out *.java

# 4. Run the project (JavaFX UI)
java --enable-native-access=javafx.graphics ^
     --module-path "libraryfx\javafx-sdk-26.0.2\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     -Dprism.order=sw ^
     -cp "library/*;out;." FinanceUI
⚠️ Notes:

Agar entry point Main.java hai, to last command me FinanceUI ki jagah Main likhna hoga.

libraryfx/ folder me JavaFX SDK hona chahiye, aur library/ folder me external JARs (jaise OpenCSV).

out/ folder compile step ke baad automatic generate ho jaayega, GitHub pe upload karna zaruri nahi hai.

📂 Sample Input/Output
Input: transactions.csv, settlements.csv

Output: Exception_Report.xlsx

👉 Add a samples/ folder in repo with dummy input/output files.

🔮 Future Scope
Spring Boot REST API integration.

Frontend dashboard for visualization.

Real-time reconciliation pipeline.
