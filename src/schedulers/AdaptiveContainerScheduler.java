package schedulers;

import model.Process;
import model.Scheduler;
import model.SimulationResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class AdaptiveContainerScheduler implements Scheduler {

    private final ACSAgingStrategy agingStrategy;

    private Queue<Process> tier1Queue = new ArrayDeque<>();
    private List<Process> tier2Queue = new ArrayList<>();
    private Process currentlyRunningBatchProcess = null;

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
        return null;
    }

    public void handleTier1Arrival(Process process) {
    }

    public boolean executeTier1Step(Process process, int quantum) {
        return false;
    }

    public void demoteToTier2(Process process, int currentTime) {
    }

    public Process selectTier2Process() {
        return null;
    }

    public boolean executeTier2Step() {
        return false;
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

    public Process getCurrentlyRunningBatchProcess() {
        return currentlyRunningBatchProcess;
    }
}
