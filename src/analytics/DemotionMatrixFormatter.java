package analytics;

import model.Process;
import model.SimulationResult;
import model.TimeStepLog;

import java.util.List;

public class DemotionMatrixFormatter {

    public static String formatDemotionMatrix(List<TimeStepLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s | %-8s | %s%n", "Time", "Process", "Tier Queues & System Events"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (TimeStepLog log : logs) {
            String processName = log.getRunningProcessId() != null ? log.getRunningProcessId() : "";

            if (log.isContextSwitch() && log.getContextSwitchMessage() != null && !log.getContextSwitchMessage().isEmpty()) {
                sb.append(String.format("%-6d | %-8s | [%s]%n", log.getTime(), "", log.getContextSwitchMessage()));
            }

            if (log.getNotes() != null && !log.getNotes().isEmpty()) {
                sb.append(String.format("%-6s | %-8s | %s%n", "", "", log.getNotes()));
            }

            StringBuilder tierSnapshot = new StringBuilder();
            if (log.getTier1Snapshot() != null && !log.getTier1Snapshot().isEmpty()) {
                tierSnapshot.append("Tier 1: ").append(log.getTier1Snapshot()).append(" ");
            }
            if (log.getTier2Snapshot() != null && !log.getTier2Snapshot().isEmpty()) {
                tierSnapshot.append("Tier 2: ").append(log.getTier2Snapshot());
            }

            if (tierSnapshot.length() > 0 || !processName.isEmpty()) {
                sb.append(String.format("%-6d | %-8s | %s%n", log.getTime(), processName, tierSnapshot.toString().trim()));
            }
        }
        return sb.toString();
    }

    public static String formatSummaryTable(SimulationResult result) {
        if (result == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append(String.format(" Scheduler: %s (Quantum: %d, Context Switch: %d)%n",
                result.getSchedulerName(), result.getQuantum(), result.getContextSwitchTime()));
        sb.append("========================================================================================\n");
        sb.append(String.format("%-10s %-14s %-13s %-17s %-14s %-14s%n",
                "Process", "Arrival Time", "Finish Time", "Turnaround Time", "Waiting Time", "Response Time"));
        sb.append("----------------------------------------------------------------------------------------\n");

        if (result.getProcesses() != null) {
            for (Process p : result.getProcesses()) {
                sb.append(String.format("%-10s %-14d %-13d %-17d %-14d %-14d%n",
                        p.getId(),
                        p.getArrivalTime(),
                        p.getFinishTime(),
                        p.getTurnaroundTime(),
                        p.getWaitingTime(),
                        p.getResponseTime()));
            }
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("Average Turnaround Time: %.2f time units%n", result.getAverageTurnaroundTime()));
        sb.append(String.format("Average Waiting Time:    %.2f time units%n", result.getAverageWaitingTime()));
        sb.append(String.format("Average Response Time:   %.2f time units%n", result.getAverageResponseTime()));
        sb.append(String.format("CPU Utilization:         %.2f%%%n", result.getCpuUtilization()));
        sb.append("========================================================================================\n");
        return sb.toString();
    }
}
