package schedulers;

import analytics.MetricsCalculator;
import model.Process;
import model.Scheduler;
import model.SimulationResult;
import model.Process.Tier;
import model.TimeStepLog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

import analytics.MetricsCalculator;

public class AdaptiveContainerScheduler implements Scheduler {
    private final ACSAgingStrategy agingStrategy;
    private int agingInterval;

    private Queue<Process> tier1Queue = new ArrayDeque<>();
    private List<Process> tier2Queue = new ArrayList<>();
    private Process currentlyRunningBatchProcess = null;
    private int currentQuantumUsed = 0;
    private Queue<Process> tier1Queue = new ArrayDeque<>();
    private List<Process> tier2Queue = new ArrayList<>();
    private Process currentlyRunningBatchProcess = null;
    private Process currentlyRunningInteractiveProcess = null;
    private int tier1QuantumUsed = 0;
    private int agingInterval = 4;

    public void setAgingInterval(int agingInterval) {
        this.agingInterval = agingInterval;
    }

    public AdaptiveContainerScheduler() {
        this(new ACSAgingStrategy(), 4);
    }

    public AdaptiveContainerScheduler(ACSAgingStrategy agingStrategy) {
        this(agingStrategy, 4);
    }

    public AdaptiveContainerScheduler(ACSAgingStrategy agingStrategy, int agingInterval) {
        this.agingStrategy = agingStrategy;
        this.agingInterval = agingInterval;
    }

    @Override
    public String getName() {
        return "Adaptive Container Scheduler (ACS)";
    }

    public void setAgingInterval(int agingInterval) {
        this.agingInterval = agingInterval;
    }

    public int getAgingInterval() {
        return agingInterval;
    }

    @Override
    public SimulationResult schedule(List<Process> processes, int quantum, int contextSwitchTime) {
        tier1Queue = new ArrayDeque<>();
        tier2Queue = new ArrayList<>();
        currentlyRunningBatchProcess = null;
        currentQuantumUsed = 0;

        List<Process> allProcesses = new ArrayList<>();
        for (Process p : processes) {
            allProcesses.add(new Process(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getInitialPriority()));
        }

        List<TimeStepLog> logs = new ArrayList<>();
        int totalProcesses = allProcesses.size();
        boolean[] arrived = new boolean[totalProcesses];

        int currentTime = 0;
        int completed = 0;
        int busyTime = 0;

        Process currentTier1Process = null;
        Process lastExecuted = null;
        Process pendingNext = null;
        boolean pendingIsTier2 = false;
        int csRemaining = 0;
        String pendingCsMsg = "";
        String pendingAgingNotes = "";

        while (completed < totalProcesses) {
            for (int i = 0; i < totalProcesses; i++) {
                Process p = allProcesses.get(i);
                if (!arrived[i] && p.getArrivalTime() <= currentTime) {
                    arrived[i] = true;
                    handleTier1Arrival(p);
                }
            }

            if (csRemaining > 0) {
                List<String> t1Snap = snapshotTier1(pendingNext != null && !pendingIsTier2 ? pendingNext : null);
                List<String> t2Snap = snapshotTier2(pendingNext != null && pendingIsTier2 ? pendingNext : null);
                logs.add(new TimeStepLog(
                        currentTime,
                        null,
                        true,
                        pendingCsMsg,
                        t1Snap,
                        t2Snap,
                        pendingAgingNotes
                ));
                pendingAgingNotes = "";
                csRemaining--;
                currentTime++;
                if (csRemaining == 0) {
                    if (pendingIsTier2) {
                        currentlyRunningBatchProcess = pendingNext;
                    } else {
                        currentTier1Process = pendingNext;
                        currentQuantumUsed = 0;
                    }
                    pendingNext = null;
                    pendingCsMsg = "";
                }
                continue;
            }

            if (currentlyRunningBatchProcess == null && currentTier1Process == null) {
                if (!tier1Queue.isEmpty()) {
                    Process next = tier1Queue.poll();
                    if (lastExecuted != null && !lastExecuted.getId().equals(next.getId()) && contextSwitchTime > 0) {
                        pendingNext = next;
                        pendingIsTier2 = false;
                        csRemaining = contextSwitchTime;
                        pendingCsMsg = "Context switch saves " + lastExecuted.getId() + " and loads " + next.getId();
                        continue;
                    } else {
                        currentTier1Process = next;
                        currentQuantumUsed = 0;
                    }
                } else if (!tier2Queue.isEmpty()) {
                    agingStrategy.applyAging(tier2Queue, currentTime, agingInterval);
                    String agingNote = agingStrategy.describeAging(tier2Queue, currentTime, agingInterval);
                    Process next = selectTier2Process();
                    if (lastExecuted != null && !lastExecuted.getId().equals(next.getId()) && contextSwitchTime > 0) {
                        pendingNext = next;
                        pendingIsTier2 = true;
                        currentlyRunningBatchProcess = null;
                        csRemaining = contextSwitchTime;
                        pendingCsMsg = "Context switch loads " + next.getId();
                        pendingAgingNotes = agingNote;
                        continue;
                    } else {
                        currentlyRunningBatchProcess = next;
                        pendingAgingNotes = agingNote;
                    }
                }
            }

            if (currentlyRunningBatchProcess != null) {
                if (currentlyRunningBatchProcess.getStartTime() == -1) {
                    currentlyRunningBatchProcess.setStartTime(currentTime);
                }

                logs.add(new TimeStepLog(
                        currentTime,
                        currentlyRunningBatchProcess.getId(),
                        false,
                        "",
                        snapshotTier1(null),
                        snapshotTier2(currentlyRunningBatchProcess),
                        pendingAgingNotes
                ));
                pendingAgingNotes = "";

                Process activeBatch = currentlyRunningBatchProcess;
                boolean finished = executeTier2Step();
                busyTime++;
                currentTime++;

                if (finished) {
                    activeBatch.setFinishTime(currentTime);
                    MetricsCalculator.computeProcessMetrics(activeBatch);
                    lastExecuted = activeBatch;
                    completed++;
                }
            } else if (currentTier1Process != null) {
                if (currentTier1Process.getStartTime() == -1) {
                    currentTier1Process.setStartTime(currentTime);
                }

                logs.add(new TimeStepLog(
                        currentTime,
                        currentTier1Process.getId(),
                        false,
                        "",
                        snapshotTier1(currentTier1Process),
                        snapshotTier2(null),
                        ""
                ));

                boolean shouldDemote = executeTier1Step(currentTier1Process, quantum);
                busyTime++;
                currentTime++;

                if (currentTier1Process.getRemainingTime() <= 0) {
                    currentTier1Process.setFinishTime(currentTime);
                    MetricsCalculator.computeProcessMetrics(currentTier1Process);
                    lastExecuted = currentTier1Process;
                    currentTier1Process = null;
                    currentQuantumUsed = 0;
                    completed++;
                } else if (shouldDemote) {
                    demoteToTier2(currentTier1Process, currentTime);
                    lastExecuted = currentTier1Process;
                    currentTier1Process = null;
                    currentQuantumUsed = 0;
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
                "Tier 1: [Empty] | Tier 2: [Empty]"
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

    public void handleTier1Arrival(Process process) {
        process.setCurrentTier(Process.Tier.TIER_1_RR);
        tier1Queue.add(process);
    }

    public boolean executeTier1Step(Process process, int quantum) {
        process.setRemainingTime(process.getRemainingTime() - 1);
        currentQuantumUsed++;
        if (process.getRemainingTime() <= 0) {
            currentQuantumUsed = 0;
            return false;
        }
        if (currentQuantumUsed >= quantum) {
            currentQuantumUsed = 0;
            return true;
        }
        return false;
    }

    public void demoteToTier2(Process process, int currentTime) {
        process.setCurrentTier(Process.Tier.TIER_2_PRIORITY);
        process.setTier2WaitStartTime(currentTime);
        tier2Queue.add(process);
    }

    public Process selectTier2Process() {
        if (currentlyRunningBatchProcess != null) {
            return currentlyRunningBatchProcess;
        }
        if (tier2Queue.isEmpty()) {
            return null;
        }
        tier2Queue.sort(agingStrategy.getTier2Comparator());
        currentlyRunningBatchProcess = tier2Queue.remove(0);
        return currentlyRunningBatchProcess;
    }

    public boolean executeTier2Step() {
        if (currentlyRunningBatchProcess == null) {
            return false;
        }
        currentlyRunningBatchProcess.setRemainingTime(currentlyRunningBatchProcess.getRemainingTime() - 1);
        if (currentlyRunningBatchProcess.getRemainingTime() <= 0) {
            currentlyRunningBatchProcess = null;
            return true;
        }
        return false;
    }

    private List<String> snapshotTier1(Process active) {
        List<String> list = new ArrayList<>();
        if (active != null) {
            list.add(formatProcess(active));
        }
        for (Process p : tier1Queue) {
            list.add(formatProcess(p));
        }
        return list;
    }

    private List<String> snapshotTier2(Process active) {
        List<String> list = new ArrayList<>();
        if (active != null) {
            list.add(formatProcess(active));
        }
        for (Process p : tier2Queue) {
            list.add(formatProcess(p));
        }
        return list;
    }

    private String formatProcess(Process p) {
        return String.format("%s (remaining: %d, Priority: %d)",
                p.getId(), p.getRemainingTime(), p.getEffectivePriority());
    }

    public ACSAgingStrategy getAgingStrategy() {
        return agingStrategy;
    }

    public Queue<Process> getTier1Queue() {
        return tier1Queue;
    }

    public List<Process> getTier2Queue() {
        return tier2Queue;
    }

    public List<String> getTier2Snapshot() {
        List<String> snapshot = new ArrayList<>();
        for (Process p : tier2Queue) {
            snapshot.add(p.getId() + " (remaining: " + p.getRemainingTime()
                    + ", Priority: " + p.getEffectivePriority() + ")");
        }
        return snapshot;
    }

    public Process getCurrentlyRunningBatchProcess() {
        return currentlyRunningBatchProcess;
    }
}
