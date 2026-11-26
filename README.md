# Cloud-Cluster CPU Scheduling Simulator

**Faculty of Computers and Artificial Intelligence — Cairo University**  
**Course:** CS241: Operating Systems – 1  
**Assignment 2:** Cloud-Cluster CPU Scheduling

A modular Java simulator modeling a cloud-cluster node executing mixed containerized workloads (latency-sensitive interactive services and CPU-heavy batch jobs) using **Preemptive Shortest-Job First (SJF)** and the **Adaptive Container Scheduler (ACS)**, complete with a **Swing Desktop GUI**, **Heuristic Demotion Matrix**, **Automated Benchmarks**, **UML Diagrams**, and **PDF/HTML Analysis Reports**.

---

## Team Members & Responsibilities

| Name | Assigned Modules & Responsibilities |
| :--- | :--- |
| **Mohamed** | Project Skeleton & Shared Contract (`src/model/`), Analytics (`MetricsCalculator`, `InputParser`, `DemotionMatrixFormatter`), Test Cases (`test-cases/`), Benchmarks, CS/Q Graphs & Final Reports (`report/`) |
| **Sayed** | Preemptive Shortest-Job First / SRTF with Context Switching (`src/schedulers/SJFScheduler.java`) |
| **Hassib** | ACS Tier 1 — Interactive Round Robin Queue, Static Quantum & Context Switching (`src/schedulers/AdaptiveContainerScheduler.java`) |
| **Ali** | ACS Tier 2 — Batch Demotion & Non-Preemptive Priority Execution Lock (`src/schedulers/AdaptiveContainerScheduler.java`) |
| **Abdelsalam** | ACS Tier 3 — Dynamic Aging Mechanism, Starvation Prevention & Priority Comparator (`src/schedulers/ACSAgingStrategy.java`) |
| **All 5 Members** | Bonus Swing Desktop GUI (`src/gui/MainFrame.java`, `GanttChartPanel.java`, `ProcessTablePanel.java`, `QueueViewPanel.java`) |

---

## Implemented Schedulers & Features

1. **Preemptive Shortest-Job First (SJF / SRTF)**
   * Always schedules the ready process with the shortest remaining burst time.
   * Accounts for Context Switching (`CS`) overhead on every preemption and process switch.

2. **Adaptive Container Scheduler (ACS)**
   * **Tier 1 (Initial State — Round Robin):** All newly arriving processes enter Tier 1 with static quantum `Q` to guarantee quick response times for interactive workloads.
   * **Tier 2 (Demotion — Non-Preemptive Priority):** Any process consuming its entire Tier 1 quantum without finishing is flagged as a CPU-bound batch job and demoted to Tier 2. Once running in Tier 2, it executes non-preemptively until completion.
   * **Tier 3 (Aging):** Prevents starvation in Tier 2 by dynamically boosting waiting processes' priorities:
     $$\text{Effective Priority} = \max\left(1,\; \text{Initial Priority} - \left\lfloor\frac{\text{Wait Time}}{\text{Aging Interval}}\right\rfloor\right)$$

3. **Analytics & Heuristic Demotion Matrix**
   * Tick-by-tick Tier 1 / Tier 2 queue snapshots, Context Switch logs, and Aging calculation logs.
   * Per-process **Waiting Time**, **Turnaround Time**, **Response Time**, **Averages**, and **CPU Utilization**.

4. **Graphical User Interface (Bonus)**
   * Native-styled Java Swing desktop app with algorithm switcher (`ACS` / `SJF`), `Q`/`CS`/`Aging` controls, file loader, process editor (`+ Process`, `Remove`, `Clear`), live metrics table, scrollable Demotion Matrix, and color-coded Gantt Chart.

---

## Project Structure

```text
Cloud-Cluster-CPU-Scheduling/
├── src/
│   ├── Main.java
│   ├── model/
│   │   ├── Process.java
│   │   ├── TimeStepLog.java
│   │   ├── SimulationResult.java
│   │   └── Scheduler.java
│   ├── schedulers/
│   │   ├── SJFScheduler.java
│   │   ├── AdaptiveContainerScheduler.java
│   │   └── ACSAgingStrategy.java
│   ├── analytics/
│   │   ├── InputParser.java
│   │   ├── MetricsCalculator.java
│   │   └── DemotionMatrixFormatter.java
│   ├── gui/
│   │   ├── MainFrame.java
│   │   ├── GanttChartPanel.java
│   │   ├── ProcessTablePanel.java
│   │   └── QueueViewPanel.java
│   └── uml-diagrams/
│       ├── system-workflow.md
│       ├── use-case-diagram.md
│       ├── class-diagram.md
│       ├── state-diagram.md
│       └── sequence-diagram.md
├── test-cases/
│   ├── test_case_1.txt
│   ├── test_case_2.txt
│   ├── test_case_3.txt
│   └── test_case_4.txt
└── report/
    ├── benchmark_results.csv
    ├── build_report.py
    ├── cs_vs_performance.png
    ├── q_vs_performance.png
    ├── Final_Report.html
    └── Final_Report.pdf
```

---

## Verified Test Case Results

### Test Case 1 (`Q = 3`, `CS = 0`)
| Process | Arrival | Burst | Priority | ACS Finish | ACS TAT | ACS WT | ACS RT | SJF TAT | SJF WT |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **P1** | 0 | 4 | 2 | 9 | 9 | 5 | 0 | 4 | 0 |
| **P2** | 1 | 7 | 3 | 13 | 12 | 5 | 2 | 12 | 5 |
| **P3** | 3 | 2 | 1 | 8 | 5 | 3 | 3 | 3 | 1 |
| **Average** | — | — | — | — | **8.67** | **4.33** | **1.67** | **6.33** | **2.00** |

### Test Case 2 (`Q = 3`, `CS = 1`, `Aging = 4`)
| Process | Arrival | Burst | Priority | ACS Finish | ACS TAT | ACS WT | ACS RT | SJF TAT | SJF WT |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **P1** | 0 | 5 | 4 | 18 | 18 | 13 | 0 | 5 | 0 |
| **P2** | 1 | 6 | 2 | 15 | 14 | 8 | 3 | 16 | 10 |
| **P3** | 4 | 4 | 3 | 20 | 16 | 12 | 4 | 6 | 2 |
| **Average** | — | — | — | — | **16.00** | **11.00** | **2.33** | **9.00** | **4.00** |

---

## How to Compile & Run (Windows Command Prompt)

### 1. Compile
```cmd
if not exist bin mkdir bin
javac -d bin src\model\*.java src\analytics\*.java src\schedulers\*.java src\gui\*.java src\Main.java
```

### 2. Run Simulator
```cmd
:: CLI Mode (Runs Test Cases 1 & 2 with full Demotion Matrix + Summary Tables)
java -cp bin Main

:: GUI Mode (Launches the Swing Desktop Simulator)
java -cp bin Main --gui

:: Run a Specific Test Case File
java -cp bin Main --file test-cases\test_case_2.txt

:: Export Benchmarks CSV (report\benchmark_results.csv)
java -cp bin Main --benchmark
```

### 3. View Graphs & Reports
```cmd
:: Regenerate CS vs Q Graphs
python report\build_report.py

:: Open Final Reports
start report\Final_Report.pdf
start report\Final_Report.html
```
