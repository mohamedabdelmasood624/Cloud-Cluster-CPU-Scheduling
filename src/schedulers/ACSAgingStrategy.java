package schedulers;

import model.Process;

import java.util.Comparator;
import java.util.List;

public class ACSAgingStrategy {

    public void applyAging(List<Process> tier2Queue, int currentTime, int agingInterval) {
    }

    public Comparator<Process> getTier2Comparator() {
        return null;
    }

    public boolean canPreempt(Process runningProcess) {
        return false;
    }
}
