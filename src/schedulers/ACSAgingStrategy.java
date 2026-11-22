package schedulers;

import model.Process;

import java.util.Comparator;
import java.util.List;

public class ACSAgingStrategy {

    // Apply aging formula to Tier 2 waiting processes
    public void applyAging(List<Process> tier2Queue, int currentTime, int agingInterval) {
        if (tier2Queue == null || tier2Queue.isEmpty() || agingInterval <= 0) {
            return;
        }

        for (Process process : tier2Queue) {
            int waitStartTime = process.getTier2WaitStartTime();
            if (waitStartTime != -1 && currentTime >= waitStartTime) {
                int waitTime = currentTime - waitStartTime;
                int agingFactor = waitTime / agingInterval;
                int newPriority = process.getInitialPriority() - agingFactor;

                if (newPriority < 1) {
                    newPriority = 1;
                }
                process.setEffectivePriority(newPriority);
            }
        }

        // Sort by priority after aging
        tier2Queue.sort(getTier2Comparator());
    }

    // Tier 2 priority comparator: lower number is higher priority, with FIFO tie-breaking
    public Comparator<Process> getTier2Comparator() {
        return (p1, p2) -> {
            int priorityComparison = Integer.compare(p1.getEffectivePriority(), p2.getEffectivePriority());
            if (priorityComparison != 0) return priorityComparison;

            int tier2WaitComparison = Integer.compare(p1.getTier2WaitStartTime(), p2.getTier2WaitStartTime());
            if (tier2WaitComparison != 0) return tier2WaitComparison;

            int arrivalComparison = Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            if (arrivalComparison != 0) return arrivalComparison;

            return p1.getId().compareTo(p2.getId());
        };
    }

    // Non-preemptive policy for Tier 2
    public boolean canPreempt(Process runningProcess) {
        return false;
    }
}
