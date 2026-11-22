package schedulers;

import analytics.MetricsCalculator;
import model.Process;
import model.Scheduler;
import model.SimulationResult;
import model.TimeStepLog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SJFScheduler implements Scheduler {

    @Override
    public String getName() {
        return "Preemptive Shortest-Job First (SJF)";
    }

    @Override
    public SimulationResult schedule(List<Process> processes, int quantum, int contextSwitchTime) {
        if (processes == null || processes.isEmpty()) {
            return new SimulationResult(
                    getName(), quantum, contextSwitchTime,
                    new ArrayList<>(), new ArrayList<>(),
                    0.0, 0.0, 0.0, 0.0, 0
            );
        }

        // Copy input processes
        List<Process> simulationList = new ArrayList<>();
        for (Process p : processes) {
            simulationList.add(new Process(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getInitialPriority()));
        }

        List<TimeStepLog> timeStepLogs = new ArrayList<>();
        int currentTime = 0;
        int completedCount = 0;
        int totalProcesses = simulationList.size();
        int busyTime = 0;

        Process currentRunning = null;
        Process lastExecuted = null;

        // Comparator: shortest remaining time, then earlier arrival, then ID
        Comparator<Process> srtfComparator = (p1, p2) -> {
            int remComparison = Integer.compare(p1.getRemainingTime(), p2.getRemainingTime());
            if (remComparison != 0) return remComparison;
            int arrComparison = Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            if (arrComparison != 0) return arrComparison;
            return p1.getId().compareTo(p2.getId());
        };

        // Main simulation loop
        while (completedCount < totalProcesses) {
            // Find ready processes
            List<Process> readyPool = new ArrayList<>();
            for (Process p : simulationList) {
                if (p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0) {
                    readyPool.add(p);
                }
            }

            // CPU idle step
            if (readyPool.isEmpty()) {
                timeStepLogs.add(new TimeStepLog(currentTime, null, false, null, new ArrayList<>(), new ArrayList<>(), "CPU Idle"));
                currentTime++;
                continue;
            }

            // Pick shortest remaining process
            readyPool.sort(srtfComparator);
            Process bestCandidate = readyPool.get(0);

            Process nextToRun = bestCandidate;
            if (currentRunning != null && currentRunning.getRemainingTime() > 0) {
                if (currentRunning.getRemainingTime() <= bestCandidate.getRemainingTime()) {
                    nextToRun = currentRunning;
                }
            }

            // Context switch handling
            if (lastExecuted != null && lastExecuted != nextToRun && contextSwitchTime > 0) {
                String csMessage;
                if (lastExecuted.getRemainingTime() > 0) {
                    csMessage = "Context switch saves " + lastExecuted.getId() + " and loads " + nextToRun.getId();
                } else {
                    csMessage = "Context switch loads " + nextToRun.getId();
                }

                for (int cs = 0; cs < contextSwitchTime; cs++) {
                    List<String> readySnapshot = getReadySnapshot(simulationList, currentTime, null);
                    timeStepLogs.add(new TimeStepLog(currentTime, null, true, csMessage, readySnapshot, new ArrayList<>(), "Context Switch"));
                    currentTime++;
                }

                readyPool.clear();
                for (Process p : simulationList) {
                    if (p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0) {
                        readyPool.add(p);
                    }
                }
                readyPool.sort(srtfComparator);
                if (!readyPool.isEmpty()) {
                    nextToRun = readyPool.get(0);
                }
            }

            // Execute process for 1 time unit
            currentRunning = nextToRun;

            if (currentRunning.getStartTime() == -1) {
                currentRunning.setStartTime(currentTime);
            }

            List<String> readySnapshot = getReadySnapshot(simulationList, currentTime, currentRunning);
            timeStepLogs.add(new TimeStepLog(currentTime, currentRunning.getId(), false, null, readySnapshot, new ArrayList<>(), "Running " + currentRunning.getId()));

            currentRunning.setRemainingTime(currentRunning.getRemainingTime() - 1);
            busyTime++;
            currentTime++;

            // Check if process finished
            if (currentRunning.getRemainingTime() == 0) {
                currentRunning.setFinishTime(currentTime);
                completedCount++;
                lastExecuted = currentRunning;
                currentRunning = null;
            } else {
                lastExecuted = currentRunning;
            }
        }

        // Compute metrics
        for (Process p : simulationList) {
            MetricsCalculator.computeProcessMetrics(p);
        }

        for (Process original : processes) {
            for (Process simulated : simulationList) {
                if (original.getId().equals(simulated.getId())) {
                    original.setStartTime(simulated.getStartTime());
                    original.setFinishTime(simulated.getFinishTime());
                    original.setWaitingTime(simulated.getWaitingTime());
                    original.setTurnaroundTime(simulated.getTurnaroundTime());
                    original.setResponseTime(simulated.getResponseTime());
                    break;
                }
            }
        }

        double avgWaitingTime = MetricsCalculator.calculateAverageWaitingTime(simulationList);
        double avgTurnaroundTime = MetricsCalculator.calculateAverageTurnaroundTime(simulationList);
        double avgResponseTime = MetricsCalculator.calculateAverageResponseTime(simulationList);
        double cpuUtilization = MetricsCalculator.calculateCpuUtilization(busyTime, currentTime);

        return new SimulationResult(
                getName(), quantum, contextSwitchTime,
                simulationList, timeStepLogs,
                avgWaitingTime, avgTurnaroundTime, avgResponseTime,
                cpuUtilization, currentTime
        );
    }

    // Ready queue snapshot helper
    private List<String> getReadySnapshot(List<Process> processes, int currentTime, Process running) {
        List<String> snapshot = new ArrayList<>();
        for (Process p : processes) {
            if (p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0) {
                if (running == null || !p.getId().equals(running.getId())) {
                    snapshot.add(p.getId() + " (remaining: " + p.getRemainingTime() + ")");
                }
            }
        }
        return snapshot;
    }
}
