package schedulers;

import model.Process;

import java.util.Comparator;
import java.util.List;

public class ACSAgingStrategy {

    public void applyAging(List<Process> tier2Queue, int currentTime, int agingInterval) {
        if (tier2Queue == null || agingInterval <= 0) {
            return;
        }
        for (Process p : tier2Queue) {
            if (p.getTier2WaitStartTime() >= 0) {
                int waitTime = Math.max(0, currentTime - p.getTier2WaitStartTime());
                int boost = waitTime / agingInterval;
                int effective = Math.max(1, p.getInitialPriority() - boost);
                p.setEffectivePriority(effective);
            }
        }
    }

    public String describeAging(List<Process> tier2Queue, int currentTime, int agingInterval) {
        if (tier2Queue == null || tier2Queue.isEmpty() || agingInterval <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tier2Queue.size(); i++) {
            Process p = tier2Queue.get(i);
            int start = p.getTier2WaitStartTime();
            int waitTime = Math.max(0, currentTime - start);
            int boost = waitTime / agingInterval;
            int effective = Math.max(1, p.getInitialPriority() - boost);
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(String.format("%s: Waited T=%d..%d (%d), Aging=[%d/%d]=%d, EffPrio=%d",
                    p.getId(), start, currentTime, waitTime, waitTime, agingInterval, boost, effective));
        }
        return sb.toString();
    }

    public Comparator<Process> getTier2Comparator() {
        return Comparator.comparingInt(Process::getEffectivePriority)
                .thenComparingInt(Process::getArrivalTime)
                .thenComparing(Process::getId);
    }

    public boolean canPreempt(Process runningProcess) {
        if (runningProcess == null) {
            return true;
        }
        return runningProcess.getCurrentTier() != Process.Tier.TIER_2_PRIORITY;
    }
}
