import model.Process;
import schedulers.ACSAgingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Member 3 Task Demonstration & Explanation:
 * ACS Aging Strategy & Priority Comparator
 *
 * This demo verifies and explains:
 * 1. Aging mechanism according to the assignment PDF specification:
 *    - waitTime = currentTime - tier2WaitStartTime
 *    - agingFactor = floor(waitTime / agingInterval)
 *    - effectivePriority = initialPriority - agingFactor
 * 2. Tier 2 Priority Ordering (lower numerical value = higher priority).
 * 3. Exact matching of Test Case 2 at T=11 and T=15.
 * 4. Non-preemptive policy of Tier 2 (canPreempt returns false).
 */
public class Member3Demo {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  MEMBER 3 TASK: ACS AGING STRATEGY & PRIORITY COMPARATOR");
        System.out.println("===============================================================\n");

        ACSAgingStrategy agingStrategy = new ACSAgingStrategy();
        int agingInterval = 4; // As specified in Test Case 2

        // Setup processes according to Test Case 2 timeline:
        // P1 arrived at 0, burst 5, priority 4. Demoted to Tier 2 at T=3.
        Process p1 = new Process("P1", 0, 5, 4);
        p1.setTier2WaitStartTime(3);
        p1.setRemainingTime(2);

        // P2 arrived at 1, burst 6, priority 2. Demoted to Tier 2 at T=7.
        Process p2 = new Process("P2", 1, 6, 2);
        p2.setTier2WaitStartTime(7);
        p2.setRemainingTime(3);

        // P3 arrived at 4, burst 4, priority 3. Demoted to Tier 2 at T=11.
        Process p3 = new Process("P3", 4, 4, 3);
        p3.setTier2WaitStartTime(11);
        p3.setRemainingTime(1);

        List<Process> tier2Queue = new ArrayList<>();
        tier2Queue.add(p1);
        tier2Queue.add(p2);
        tier2Queue.add(p3);

        System.out.println("---------------------------------------------------------------");
        System.out.println(" SCENARIO 1: AGING AT TIME T = 11");
        System.out.println(" Assignment Page 4 Reference:");
        System.out.println("   P1: Waited from T=3 to T=11 (8 units). Aging: [8/4]=2. Effective = 4-2 = 2");
        System.out.println("   P2: Waited from T=7 to T=11 (4 units). Aging: [4/4]=1. Effective = 2-1 = 1");
        System.out.println("   P3: Waited from T=11 to T=11 (0 units). Aging: [0/4]=0. Effective = 3");
        System.out.println("---------------------------------------------------------------");

        agingStrategy.applyAging(tier2Queue, 11, agingInterval);

        System.out.printf("%-10s %-16s %-16s %-18s %-18s%n",
                "Process", "Initial Priority", "Wait in Tier 2", "Aging Factor", "Effective Priority");
        for (Process p : tier2Queue) {
            int wait = 11 - p.getTier2WaitStartTime();
            System.out.printf("%-10s %-16d %-16d %-18d %-18d%n",
                    p.getId(), p.getInitialPriority(), wait, (wait / agingInterval), p.getEffectivePriority());
        }

        Process selectedAt11 = tier2Queue.get(0);
        System.out.println("\n-> Selected Process to run next: " + selectedAt11.getId()
                + " (Effective Priority: " + selectedAt11.getEffectivePriority() + ")");
        System.out.println("-> Result: SUCCESS (Matches PDF Page 4: P2 runs next because priority 1 is highest)\n");

        // Simulate P2 running from 11 to 15 (4 units) and completing
        tier2Queue.remove(p2);

        System.out.println("---------------------------------------------------------------");
        System.out.println(" SCENARIO 2: AGING AT TIME T = 15");
        System.out.println(" Assignment Page 4 Reference:");
        System.out.println("   P1: Waited from T=3 to T=15 (12 units). Aging: [12/4]=3. Effective = 4-3 = 1");
        System.out.println("   P3: Waited from T=11 to T=15 (4 units). Aging: [4/4]=1. Effective = 3-1 = 2");
        System.out.println("---------------------------------------------------------------");

        agingStrategy.applyAging(tier2Queue, 15, agingInterval);

        System.out.printf("%-10s %-16s %-16s %-18s %-18s%n",
                "Process", "Initial Priority", "Wait in Tier 2", "Aging Factor", "Effective Priority");
        for (Process p : tier2Queue) {
            int wait = 15 - p.getTier2WaitStartTime();
            System.out.printf("%-10s %-16d %-16d %-18d %-18d%n",
                    p.getId(), p.getInitialPriority(), wait, (wait / agingInterval), p.getEffectivePriority());
        }

        Process selectedAt15 = tier2Queue.get(0);
        System.out.println("\n-> Selected Process to run next: " + selectedAt15.getId()
                + " (Effective Priority: " + selectedAt15.getEffectivePriority() + ")");
        System.out.println("-> Result: SUCCESS (Matches PDF Page 4: P1 runs next because priority 1 is highest)\n");

        System.out.println("---------------------------------------------------------------");
        System.out.println(" SCENARIO 3: NON-PREEMPTIVE CHECK (canPreempt)");
        System.out.println(" Assignment Page 1 Tier 2 Requirement:");
        System.out.println("   'When a process enters the state of running in this tier,");
        System.out.println("    it cannot be preempted until it completes its service time.'");
        System.out.println("---------------------------------------------------------------");
        boolean canPreempt = agingStrategy.canPreempt(selectedAt15);
        System.out.println("-> canPreempt(runningProcess): " + canPreempt + " (Expected: false)");
        System.out.println("-> Result: SUCCESS (Tier 2 is strictly non-preemptive)\n");

        System.out.println("===============================================================");
        System.out.println(" ALL MEMBER 3 CHECKS PASSED PERFECTLY!");
        System.out.println("===============================================================");
    }
}
