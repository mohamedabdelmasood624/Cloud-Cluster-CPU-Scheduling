package schedulers;

import model.Process;
import model.Scheduler;
import model.SimulationResult;

import java.util.List;

public class SJFScheduler implements Scheduler {

    @Override
    public String getName() {
        return "Preemptive Shortest-Job First (SJF)";
    }

    @Override
    public SimulationResult schedule(List<Process> processes, int quantum, int contextSwitchTime) {
        return null;
    }
}
