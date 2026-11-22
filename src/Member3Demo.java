import model.Process;
import schedulers.ACSAgingStrategy;

import java.util.ArrayList;
import java.util.List;

public class Member3Demo {

    public static void main(String[] args) {
        System.out.println("=== Member 3: ACS Aging & Priority Demo ===");

        ACSAgingStrategy agingStrategy = new ACSAgingStrategy();
        int agingInterval = 4;

        // Setup processes from Test Case 2
        Process p1 = new Process("P1", 0, 5, 4);
        p1.setTier2WaitStartTime(3);
        p1.setRemainingTime(2);

        Process p2 = new Process("P2", 1, 6, 2);
        p2.setTier2WaitStartTime(7);
        p2.setRemainingTime(3);

        Process p3 = new Process("P3", 4, 4, 3);
        p3.setTier2WaitStartTime(11);
        p3.setRemainingTime(1);

        List<Process> tier2Queue = new ArrayList<>();
        tier2Queue.add(p1);
        tier2Queue.add(p2);
        tier2Queue.add(p3);

        // Test aging at T=11
        System.out.println("\n--- Aging at T = 11 ---");
        agingStrategy.applyAging(tier2Queue, 11, agingInterval);

        for (Process p : tier2Queue) {
            int wait = 11 - p.getTier2WaitStartTime();
            System.out.printf("%s: Wait=%d, Aging=%d, EffectivePriority=%d%n",
                    p.getId(), wait, (wait / agingInterval), p.getEffectivePriority());
        }
        System.out.println("Selected next: " + tier2Queue.get(0).getId());

        // Test aging at T=15
        tier2Queue.remove(p2);
        System.out.println("\n--- Aging at T = 15 ---");
        agingStrategy.applyAging(tier2Queue, 15, agingInterval);

        for (Process p : tier2Queue) {
            int wait = 15 - p.getTier2WaitStartTime();
            System.out.printf("%s: Wait=%d, Aging=%d, EffectivePriority=%d%n",
                    p.getId(), wait, (wait / agingInterval), p.getEffectivePriority());
        }
        System.out.println("Selected next: " + tier2Queue.get(0).getId());

        // Test non-preemptive policy
        System.out.println("\ncanPreempt: " + agingStrategy.canPreempt(tier2Queue.get(0)));
    }
}
