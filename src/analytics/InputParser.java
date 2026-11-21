package analytics;

import model.Process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputParser {

    public static class SimulationConfig {
        public int quantum = 3;
        public int contextSwitch = 0;
        public int agingInterval = 4;
        public List<Process> processes = new ArrayList<>();
    }

    public static SimulationConfig parseFile(File file) throws IOException {
        SimulationConfig config = new SimulationConfig();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.toUpperCase().startsWith("Q") && line.contains("=")) {
                    config.quantum = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.toUpperCase().startsWith("CS") && line.contains("=")) {
                    config.contextSwitch = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.toUpperCase().startsWith("AGING") && line.contains("=")) {
                    config.agingInterval = Integer.parseInt(line.split("=")[1].trim());
                } else {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 4) {
                        String id = tokens[0];
                        int arrival = Integer.parseInt(tokens[1]);
                        int burst = Integer.parseInt(tokens[2]);
                        int priority = Integer.parseInt(tokens[3]);
                        config.processes.add(new Process(id, arrival, burst, priority));
                    }
                }
            }
        }
        return config;
    }

    public static SimulationConfig parseCliArgs(String[] args) {
        SimulationConfig config = new SimulationConfig();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--quantum") && i + 1 < args.length) {
                config.quantum = Integer.parseInt(args[++i]);
            } else if (args[i].equalsIgnoreCase("--cs") && i + 1 < args.length) {
                config.contextSwitch = Integer.parseInt(args[++i]);
            } else if (args[i].equalsIgnoreCase("--aging") && i + 1 < args.length) {
                config.agingInterval = Integer.parseInt(args[++i]);
            } else if (args[i].equalsIgnoreCase("--file") && i + 1 < args.length) {
                try {
                    return parseFile(new File(args[++i]));
                } catch (IOException e) {
                    return config;
                }
            }
        }
        return config;
    }
}
