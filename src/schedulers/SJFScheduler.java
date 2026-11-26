package schedulers;

import analytics.MetricsCalculator;
import model.Process;
import model.Scheduler;
import model.SimulationResult;
import model.TimeStepLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SJFScheduler implements Scheduler {

    @Override
    public String getName() {
        return "Preemptive Shortest-Job First (SJF)";
    }

    @Override
    public SimulationResult schedule(List<Process> processes, int quantum, int contextSwitchTime) {
        List<Process> allProcesses = new ArrayList<>();
        for (Process p : processes) {
            allProcesses.add(new Process(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getInitialPriority()));
        }

        List<Process> readyQueue = new ArrayList<>();
        List<TimeStepLog> logs = new ArrayList<>();
        Comparator<Process> srtfComparator = Comparator.comparingInt(Process::getRemainingTime)
                .thenComparingInt(Process::getArrivalTime)
                .thenComparing(Process::getId);

        int currentTime = 0;
        int completed = 0;
        int busyTime = 0;
        int totalProcesses = allProcesses.size();
        boolean[] arrived = new boolean[totalProcesses];

        Process currentRunning = null;
        Process lastExecuted = null;
        Process pendingNext = null;
        int csRemaining = 0;
        String pendingCsMsg = "";

        while (completed < totalProcesses) {
            for (int i = 0; i < totalProcesses; i++) {
                Process p = allProcesses.get(i);
                if (!arrived[i] && p.getArrivalTime() <= currentTime) {
                    arrived[i] = true;
                    readyQueue.add(p);
                }
            }

            if (csRemaining > 0) {
                readyQueue.sort(srtfComparator);
                logs.add(new TimeStepLog(
                        currentTime,
                        null,
                        true,
                        pendingCsMsg,
                        snapshotQueue(readyQueue),
                        Collections.emptyList(),
                        ""
                ));
                csRemaining--;
                currentTime++;
                if (csRemaining == 0) {
                    currentRunning = pendingNext;
                    pendingNext = null;
                    pendingCsMsg = "";
                }
                continue;
            }

            if (currentRunning != null && !readyQueue.isEmpty()) {
                readyQueue.sort(srtfComparator);
                Process shortestReady = readyQueue.get(0);
                if (shortestReady.getRemainingTime() < currentRunning.getRemainingTime()) {
                    Process preempted = currentRunning;
                    readyQueue.remove(0);
                    readyQueue.add(preempted);
                    currentRunning = null;
                    lastExecuted = preempted;
                    if (contextSwitchTime > 0) {
                        pendingNext = shortestReady;
                        csRemaining = contextSwitchTime;
                        pendingCsMsg = "Context switch saves " + preempted.getId() + " and loads " + shortestReady.getId();
                        continue;
                    } else {
                        currentRunning = shortestReady;
                    }
                }
            }

            if (currentRunning == null && !readyQueue.isEmpty()) {
                readyQueue.sort(srtfComparator);
                Process next = readyQueue.remove(0);
                if (lastExecuted != null && !lastExecuted.getId().equals(next.getId()) && contextSwitchTime > 0) {
                    pendingNext = next;
                    csRemaining = contextSwitchTime;
                    pendingCsMsg = "Context switch loads " + next.getId();
                    continue;
                } else {
                    currentRunning = next;
                }
            }

            if (currentRunning != null) {
                if (currentRunning.getStartTime() == -1) {
                    currentRunning.setStartTime(currentTime);
                }

                List<Process> displayReady = new ArrayList<>();
                displayReady.add(currentRunning);
                displayReady.addAll(readyQueue);
                logs.add(new TimeStepLog(
                        currentTime,
                        currentRunning.getId(),
                        false,
                        "",
                        snapshotQueue(displayReady),
                        Collections.emptyList(),
                        ""
                ));

                currentRunning.setRemainingTime(currentRunning.getRemainingTime() - 1);
                busyTime++;
                currentTime++;

                if (currentRunning.getRemainingTime() <= 0) {
                    currentRunning.setFinishTime(currentTime);
                    MetricsCalculator.computeProcessMetrics(currentRunning);
                    lastExecuted = currentRunning;
                    currentRunning = null;
                    completed++;
                }
            } else {
                logs.add(new TimeStepLog(
                        currentTime,
                        null,
                        false,
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "CPU Idle"
                ));
                currentTime++;
            }
        }

        logs.add(new TimeStepLog(
                currentTime,
                null,
                false,
                "",
                Collections.emptyList(),
                Collections.emptyList(),
                "All processes completed"
        ));

        double awt = MetricsCalculator.calculateAverageWaitingTime(allProcesses);
        double atat = MetricsCalculator.calculateAverageTurnaroundTime(allProcesses);
        double art = MetricsCalculator.calculateAverageResponseTime(allProcesses);
        double cpuUtil = MetricsCalculator.calculateCpuUtilization(busyTime, currentTime);

        return new SimulationResult(
                getName(),
                quantum,
                contextSwitchTime,
                allProcesses,
                logs,
                awt,
                atat,
                art,
                cpuUtil,
                currentTime
        );
    }

    private List<String> snapshotQueue(List<Process> queue) {
        List<String> list = new ArrayList<>();
        for (Process p : queue) {
            list.add(String.format("%s (remaining: %d, Priority: %d)",
                    p.getId(), p.getRemainingTime(), p.getEffectivePriority()));
        }
        return list;
    }
}
