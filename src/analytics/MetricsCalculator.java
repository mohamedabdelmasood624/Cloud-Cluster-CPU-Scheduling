package analytics;

import model.Process;

import java.util.List;

public class MetricsCalculator {

    public static void computeProcessMetrics(Process process) {
        int turnaroundTime = process.getFinishTime() - process.getArrivalTime();
        int waitingTime = turnaroundTime - process.getBurstTime();
        int responseTime = process.getStartTime() - process.getArrivalTime();

        process.setTurnaroundTime(Math.max(0, turnaroundTime));
        process.setWaitingTime(Math.max(0, waitingTime));
        process.setResponseTime(Math.max(0, responseTime));
    }

    public static double calculateAverageWaitingTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0.0;
        }
        double totalWait = 0.0;
        for (Process p : processes) {
            totalWait += p.getWaitingTime();
        }
        return totalWait / processes.size();
    }

    public static double calculateAverageTurnaroundTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0.0;
        }
        double totalTurnaround = 0.0;
        for (Process p : processes) {
            totalTurnaround += p.getTurnaroundTime();
        }
        return totalTurnaround / processes.size();
    }

    public static double calculateAverageResponseTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return 0.0;
        }
        double totalResponse = 0.0;
        for (Process p : processes) {
            totalResponse += p.getResponseTime();
        }
        return totalResponse / processes.size();
    }

    public static double calculateCpuUtilization(int busyTime, int totalTime) {
        if (totalTime <= 0) {
            return 0.0;
        }
        return ((double) busyTime / totalTime) * 100.0;
    }
}
