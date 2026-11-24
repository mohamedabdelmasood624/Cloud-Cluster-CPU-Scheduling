package schedulers;

import model.Process;
import model.Scheduler;
import model.SimulationResult;
import model.Process.Tier;
import model.TimeStepLog;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

import analytics.MetricsCalculator;

public class AdaptiveContainerScheduler implements Scheduler {
    private final ACSAgingStrategy agingStrategy;
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
        this.agingStrategy = new ACSAgingStrategy();
    }

    public AdaptiveContainerScheduler(ACSAgingStrategy agingStrategy) {
        this.agingStrategy = agingStrategy;
    }

    @Override
    public String getName() {
        return "Adaptive Container Scheduler (ACS)";
    }

    @Override
    public SimulationResult schedule(List<Process> processes, int quantum, int contextSwitchTime) {
        tier1Queue.clear();
        tier2Queue.clear();
        currentlyRunningBatchProcess = null;

        List<Process> simulist = new ArrayList<>();
        if (processes != null) {
            for (Process p : processes) {
                simulist.add(new Process(p.getId(), p.getArrivalTime(),
                        p.getBurstTime(), p.getInitialPriority()));
            }
        }

        List<TimeStepLog> executionLog = new ArrayList<>();
        int currentTime = 0, completed = 0, busyTime = 0;

        Process lst = null, currentRR = null;
        int currentquantums = 0;

        Consumer<Integer> handleArrivals = (time) -> {
            for (Process p : simulist) {
                if (p.getArrivalTime() == time)
                    handleTier1Arrival(p);

            }
        };

        while (completed < simulist.size()) {

            // newcomers
            handleArrivals.accept(currentTime);

            Process nextProcess = null;
            String stepNotes = null;

            if (currentlyRunningBatchProcess != null) {
                nextProcess = currentlyRunningBatchProcess;
            } else if (currentRR != null) {
                nextProcess = currentRR;
            } else if (!tier1Queue.isEmpty()) {
                currentRR = tier1Queue.poll();
                currentquantums = 0;
                nextProcess = currentRR;
            } else if (!tier2Queue.isEmpty()) {
                List<String> notes = agingStrategy.buildAgingNotes(tier2Queue, currentTime, agingInterval);
                if (!notes.isEmpty()) {
                    stepNotes = String.join("\n", notes);
                }
                nextProcess = selectTier2Process(currentTime, agingInterval);
            }

            if (nextProcess == null) {
                executionLog.add(new TimeStepLog(
                        currentTime,
                        "IDLE",
                        false,
                        null,
                        getTier1Snapshot(),
                        getTier2Snapshot(),
                        null));
                lst = null;
                currentTime++;
                continue;
            }

            if (lst != null && lst != nextProcess && contextSwitchTime > 0) {
                String csMsg = "Context Switch (" + lst.getId() + " -> " + nextProcess.getId() + ")";
                for (int cs = 0; cs < contextSwitchTime; cs++) {
                    executionLog.add(new TimeStepLog(
                            currentTime,
                            "CS",
                            true,
                            csMsg,
                            getTier1Snapshot(),
                            getTier2Snapshot(),
                            null));
                    currentTime++;
                    handleArrivals.accept(currentTime);
                }
            }

            if (nextProcess.getStartTime() == -1) {
                nextProcess.setStartTime(currentTime);
            }

            executionLog.add(new TimeStepLog(
                    currentTime,
                    nextProcess.getId(),
                    false,
                    null,
                    getTier1Snapshot(),
                    getTier2Snapshot(),
                    stepNotes));

            currentTime++;
            busyTime++;
            lst = nextProcess;

            if (currentlyRunningBatchProcess != null) {
                boolean done = executeTier2Step();
                if (done) {
                    nextProcess.setFinishTime(currentTime);
                    completed++;
                }
            } else {
                boolean turnEnded = executeTier1Step(nextProcess, quantum);
                if (turnEnded) {
                    if (nextProcess.getRemainingTime() == 0) {
                        nextProcess.setFinishTime(currentTime);
                        completed++;
                    } else {
                        demoteToTier2(nextProcess, currentTime);
                    }
                }
            }

            for (Process p : simulist) {
                MetricsCalculator.computeProcessMetrics(p);
            }

            double avgWaiting = MetricsCalculator.calculateAverageWaitingTime(simulist);
            double avgResponse = MetricsCalculator.calculateAverageResponseTime(simulist);
            double avgTurnaround = MetricsCalculator.calculateAverageTurnaroundTime(simulist);
            double cpuUtilization = MetricsCalculator.calculateCpuUtilization(busyTime, currentTime);

            return new SimulationResult(
                    getName(),
                    quantum,
                    contextSwitchTime,
                    simulist,
                    executionLog,
                    avgWaiting,
                    avgTurnaround,
                    avgResponse,
                    cpuUtilization,
                    currentTime);
        }

        return null;
    }

    public void handleTier1Arrival(Process process) {
        if (process == null)
            return;
        process.setCurrentTier(Process.Tier.TIER_1_RR);
        tier1Queue.add(process);
    }

    public Process selectTier1Process() {
        if (tier1Queue.isEmpty())
            return null;
        currentlyRunningInteractiveProcess = tier1Queue.poll();
        tier1QuantumUsed = 0;
        return currentlyRunningInteractiveProcess;
    }

    public boolean executeTier1Step(Process process, int quantum) {
        if (currentlyRunningInteractiveProcess == null)
            return false;

        currentlyRunningInteractiveProcess.setRemainingTime(currentlyRunningInteractiveProcess.getRemainingTime() - 1);
        tier1QuantumUsed++;

        if (currentlyRunningInteractiveProcess.getRemainingTime() == 0 || tier1QuantumUsed >= quantum) {
            currentlyRunningInteractiveProcess = null;
            tier1QuantumUsed = 0;
            return true;
        } else {
            return false;
        }

    }

    public List<String> getTier1Snapshot() {
        List<String> snapshot = new ArrayList<>();
        for (Process p : tier1Queue) {
            snapshot.add(p.getId() + " (remaining: " + p.getRemainingTime()
                    + ", Priority: " + p.getEffectivePriority() + ")");
        }
        return snapshot;
    }

    public void demoteToTier2(Process process, int currentTime) {
        if (process == null)
            return;
        process.setCurrentTier(Tier.TIER_2_PRIORITY);
        process.setTier2WaitStartTime(currentTime);
        tier2Queue.add(process);
    }

    public Process selectTier2Process(int currentTime, int agingInterval) {
        if (tier2Queue.isEmpty())
            return null;
        agingStrategy.applyAging(tier2Queue, currentTime, agingInterval);
        currentlyRunningBatchProcess = tier2Queue.remove(0);
        return currentlyRunningBatchProcess;
    }

    public boolean executeTier2Step() {
        if (currentlyRunningBatchProcess == null)
            return false;
        currentlyRunningBatchProcess.setRemainingTime(currentlyRunningBatchProcess.getRemainingTime() - 1);
        if (currentlyRunningBatchProcess.getRemainingTime() == 0) {
            currentlyRunningBatchProcess = null;
            return true;
        } else {
            return false;
        }
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
