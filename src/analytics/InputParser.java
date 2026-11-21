package analytics;

import model.Process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputParser {

    public static class SimulationConfig {
        public int quantum;
        public int contextSwitch;
        public int agingInterval = 4;
        public List<Process> processes = new ArrayList<>();
    }

    public static SimulationConfig parseFile(File file) throws IOException {
        return null;
    }

    public static SimulationConfig parseCliArgs(String[] args) {
        return null;
    }
}
