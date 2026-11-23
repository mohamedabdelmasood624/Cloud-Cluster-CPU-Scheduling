package schedulers;

import model.Process;

import java.util.ArrayList;
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

                //hassib: me4 mo4kal en yb2a 3ndna elpriority bel negative, 34an keda momken elpriorities mtb2a4 sorted, yaani fi el aging w keda lw b2a 3ndna priority -5, 
                //hassib: w priority 1, fa aked priority -5 aham, fa once enna n3mlha 1 bardo da momken ybwz elordering where two processes kan feh mabenhom different priorities, ba2o nafs el priority w da 8alat.
                // if (newPriority < 1) {
                //     newPriority = 1;
                // }
                process.setEffectivePriority(newPriority);
            }
        }

        // Sort by priority after aging
        tier2Queue.sort(getTier2Comparator());
    }

    //Hassib: created the helper method to prin the aging explanation logs 34an tb2a zai el pdfs bzbt
    //hassib: fa applyagin() works silently without printing what happened, fa h3ml el assisting function
    // me4 fahem elsyntax kolo w el logic hena, i put it here as a placeholder for now until we make sure we know how it prints
    public List<String> buildAgingNotes(List<Process> tier2Queue, int currentTime, int agingInterval){
        List<String> notes = new ArrayList<>();
        if (tier2Queue == null || tier2Queue.isEmpty() || agingInterval <= 0) return notes;

        for(Process process: tier2Queue){
            int waitStartTime = process.getTier2WaitStartTime();
            if(waitStartTime != -1 && currentTime >= waitStartTime){
                int waitTime = currentTime - waitStartTime;
                int agingFactor = waitTime / agingInterval;
                int effectivePriority = process.getInitialPriority() - agingFactor;

                if (agingFactor > 0) {
                    notes.add(String.format(
                            "%s: Waited from T=%d to T=%d (%d units). Aging: [%d/%d]=%d. Effective Priority =%d-%d=%d",
                            process.getId(), waitStartTime, currentTime, waitTime,
                            waitTime, agingInterval, agingFactor,
                            process.getInitialPriority(), agingFactor, effectivePriority
                    ));
                } else {
                    notes.add(String.format(
                            "%s: Waited from T=%d to T=%d (%d units). Effective Priority =%d",
                            process.getId(), waitStartTime, currentTime, waitTime,
                            process.getInitialPriority()
                    ));
                }
            }
        }

        return notes;
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
