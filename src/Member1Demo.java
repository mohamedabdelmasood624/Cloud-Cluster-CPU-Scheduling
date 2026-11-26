import analytics.InputParser;
import model.Process;
import model.SimulationResult;
import model.TimeStepLog;
import schedulers.SJFScheduler;

import java.io.File;

public class Member1Demo {

    public static void main(String[] args) {
        System.out.println("=== Member 1: Preemptive SJF Demo ===");
        runTestCase("test-cases/test_case_1.txt", "Test Case 1 (CS = 0)");
        runTestCase("test-cases/test_case_2.txt", "Test Case 2 (CS = 1)");
    }

    // Run and display test case results
    private static void runTestCase(String filePath, String title) {
        System.out.println("\n--- " + title + " ---");
        try {
            File file = new File(filePath);
            if (!file.exists()) return;

            InputParser.SimulationConfig config = InputParser.parseFile(file);
            SJFScheduler scheduler = new SJFScheduler();
            SimulationResult result = scheduler.schedule(config.processes, config.quantum, config.contextSwitch);

            // Print execution timeline
            for (TimeStepLog log : result.getTimeStepLogs()) {
                if (log.isContextSwitch()) {
                    System.out.printf("T=%2d | [CS] %s%n", log.getTime(), log.getContextSwitchMessage());
                } else if (log.getRunningProcessId() != null) {
                    System.out.printf("T=%2d | Running: %s | Ready: %s%n", log.getTime(), log.getRunningProcessId(), log.getTier1Snapshot());
                } else {
                    System.out.printf("T=%2d | [IDLE]%n", log.getTime());
                }
            }

            // Print metrics table
            System.out.printf("%n%-8s %-8s %-8s %-8s %-8s %-12s %-10s %-10s%n",
                    "PID", "Arrival", "Burst", "Start", "Finish", "Turnaround", "Waiting", "Response");
            for (Process p : result.getProcesses()) {
                System.out.printf("%-8s %-8d %-8d %-8d %-8d %-12d %-10d %-10d%n",
                        p.getId(), p.getArrivalTime(), p.getBurstTime(),
                        p.getStartTime(), p.getFinishTime(),
                        p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime());
            }

            // Print averages
            System.out.printf("Avg Turnaround: %.2f | Avg Waiting: %.2f | Avg Response: %.2f | CPU Util: %.2f%%%n",
                    result.getAverageTurnaroundTime(), result.getAverageWaitingTime(),
                    result.getAverageResponseTime(), result.getCpuUtilization());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
