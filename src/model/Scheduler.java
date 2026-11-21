package model;

import java.util.List;

public interface Scheduler {
    SimulationResult schedule(List<Process> processes, int quantum, int contextSwitchTime);
    String getName();
}
