package schedulers;

import model.Process;

import java.util.Comparator;
import java.util.List;

/**
 * Member 3 Task: ACS Aging Strategy & Priority Comparator
 * 
 * Implements Tier 3 Aging and Priority Ordering for the Adaptive Container Scheduler (ACS):
 * 1. Aging mechanism to prevent low-priority batch jobs from starving in Tier 2.
 * 2. Priority comparator ordering processes by Effective Priority (lower numerical value = higher priority).
 * 3. Non-preemptive policy enforcement for Tier 2.
 */
public class ACSAgingStrategy {

    /**
     * Applies aging to all processes currently waiting in Tier 2 queue.
     * 
     * Requirement & Formula (as defined in Assignment Page 1 & Page 4):
     * - As time progresses, increase the priority of any process waiting in Tier 2.
     * - waitTime = currentTime - process.getTier2WaitStartTime()
     * - agingFactor = floor(waitTime / agingInterval)
     * - effectivePriority = initialPriority - agingFactor
     * 
     * In this system, lower numerical priority means higher priority.
     * Priority is clamped at 1 so it does not become non-positive.
     * 
     * @param tier2Queue    List of batch processes waiting in Tier 2.
     * @param currentTime   The current simulation time tick.
     * @param agingInterval The aging time threshold (e.g. 4 time units in Test Case 2).
     */
    public void applyAging(List<Process> tier2Queue, int currentTime, int agingInterval) {
        if (tier2Queue == null || tier2Queue.isEmpty() || agingInterval <= 0) {
            return;
        }

        for (Process process : tier2Queue) {
            int waitStartTime = process.getTier2WaitStartTime();
            
            // Only age processes that have a recorded Tier 2 wait start time
            if (waitStartTime != -1 && currentTime >= waitStartTime) {
                int waitTime = currentTime - waitStartTime;
                int agingFactor = waitTime / agingInterval; // integer division gives floor(waitTime / agingInterval)
                int newPriority = process.getInitialPriority() - agingFactor;

                // Priority 1 is the highest priority level; prevent priority < 1
                if (newPriority < 1) {
                    newPriority = 1;
                }
                process.setEffectivePriority(newPriority);
            }
        }

        // Keep the waiting processes sorted so the highest-priority process is at the front
        tier2Queue.sort(getTier2Comparator());
    }

    /**
     * Returns a comparator for ordering processes in Tier 2.
     * 
     * Priority Rules:
     * 1. Effective Priority: lower numerical value = higher priority (e.g. 1 is higher than 2).
     * 2. Tie-breaker 1: Tier 2 wait start time (FIFO order in Tier 2).
     * 3. Tie-breaker 2: Original arrival time.
     * 4. Tie-breaker 3: Process ID lexicographically.
     * 
     * @return Comparator for Tier 2 processes.
     */
    public Comparator<Process> getTier2Comparator() {
        return (p1, p2) -> {
            // 1. Primary order: lower effective priority value = higher priority
            int priorityComparison = Integer.compare(p1.getEffectivePriority(), p2.getEffectivePriority());
            if (priorityComparison != 0) {
                return priorityComparison;
            }

            // 2. Tie-breaker 1: who entered Tier 2 first (FIFO in Tier 2)
            int tier2WaitComparison = Integer.compare(p1.getTier2WaitStartTime(), p2.getTier2WaitStartTime());
            if (tier2WaitComparison != 0) {
                return tier2WaitComparison;
            }

            // 3. Tie-breaker 2: original process arrival time
            int arrivalComparison = Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            if (arrivalComparison != 0) {
                return arrivalComparison;
            }

            // 4. Tie-breaker 3: process ID
            return p1.getId().compareTo(p2.getId());
        };
    }

    /**
     * Checks whether a currently running batch process in Tier 2 can be preempted.
     * 
     * Requirement (Assignment Page 1, Tier 2 Demotion):
     * "When a process enters the state of running in this tier,
     * it cannot be preempted until it completes its service time."
     * 
     * @param runningProcess The process currently executing in Tier 2.
     * @return false always, because Tier 2 is non-preemptive.
     */
    public boolean canPreempt(Process runningProcess) {
        return false;
    }
}
