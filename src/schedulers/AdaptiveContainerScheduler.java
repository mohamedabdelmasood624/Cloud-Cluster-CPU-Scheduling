package schedulers;

import model.Process;
import model.Scheduler;
import model.SimulationResult;
import model.Process.Tier;

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
        if(process == null) return;
        process.setCurrentTier(Tier.TIER_2_PRIORITY);
        process.setTier2WaitStartTime(currentTime);
        tier2Queue.add(process);
    }

    public Process selectTier2Process(int currentTime, int agingInterval) {
        if(tier2Queue.isEmpty()) return null;
        agingStrategy.applyAging(tier2Queue, currentTime, agingInterval);
        currentlyRunningBatchProcess = tier2Queue.remove(0);
        return currentlyRunningBatchProcess;
    }

    public boolean executeTier2Step() {
        if(currentlyRunningBatchProcess == null) return false;
        currentlyRunningBatchProcess.setRemainingTime(currentlyRunningBatchProcess.getRemainingTime()-1);
        if(currentlyRunningBatchProcess.getRemainingTime() == 0){
            currentlyRunningBatchProcess = null;
            return true;
        }else{
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

    public Process getCurrentlyRunningBatchProcess() {
        return currentlyRunningBatchProcess;
    }
}
