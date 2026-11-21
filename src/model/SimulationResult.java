package model;

import java.util.List;

public class SimulationResult {
    private String schedulerName;
    private int quantum;
    private int contextSwitchTime;
    private List<Process> processes;
    private List<TimeStepLog> timeStepLogs;

    private double averageWaitingTime;
    private double averageTurnaroundTime;
    private double averageResponseTime;
    private double cpuUtilization;
    private int totalSimulationTime;

    public SimulationResult(String schedulerName,
                            int quantum,
                            int contextSwitchTime,
                            List<Process> processes,
                            List<TimeStepLog> timeStepLogs,
                            double averageWaitingTime,
                            double averageTurnaroundTime,
                            double averageResponseTime,
                            double cpuUtilization,
                            int totalSimulationTime) {
        this.schedulerName = schedulerName;
        this.quantum = quantum;
        this.contextSwitchTime = contextSwitchTime;
        this.processes = processes;
        this.timeStepLogs = timeStepLogs;
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnaroundTime = averageTurnaroundTime;
        this.averageResponseTime = averageResponseTime;
        this.cpuUtilization = cpuUtilization;
        this.totalSimulationTime = totalSimulationTime;
    }

    public String getSchedulerName() { return schedulerName; }
    public int getQuantum() { return quantum; }
    public int getContextSwitchTime() { return contextSwitchTime; }
    public List<Process> getProcesses() { return processes; }
    public List<TimeStepLog> getTimeStepLogs() { return timeStepLogs; }
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getAverageTurnaroundTime() { return averageTurnaroundTime; }
    public double getAverageResponseTime() { return averageResponseTime; }
    public double getCpuUtilization() { return cpuUtilization; }
    public int getTotalSimulationTime() { return totalSimulationTime; }
}
