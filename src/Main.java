import analytics.DemotionMatrixFormatter;
import analytics.InputParser;
import gui.MainFrame;
import model.SimulationResult;
import schedulers.ACSAgingStrategy;
import schedulers.AdaptiveContainerScheduler;
import schedulers.SJFScheduler;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });
            return;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("--benchmark")) {
            runBenchmarks();
            return;
        }

        try {
            if (args.length > 0 && args[0].equalsIgnoreCase("--file")) {
                InputParser.SimulationConfig config = InputParser.parseCliArgs(args);
                runSingleConfig(args[1], config);
                return;
            }

            String[] defaultFiles = {
                    "test-cases/test_case_1.txt",
                    "test-cases/test_case_2.txt"
            };
            for (String path : defaultFiles) {
                File f = new File(path);
                if (f.exists()) {
                    InputParser.SimulationConfig config = InputParser.parseFile(f);
                    runSingleConfig(path, config);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runSingleConfig(String label, InputParser.SimulationConfig config) {
        System.out.println("\n########################################################################################");
        System.out.println(" TEST CASE: " + label);
        System.out.println("########################################################################################");

        AdaptiveContainerScheduler acs = new AdaptiveContainerScheduler(new ACSAgingStrategy(), config.agingInterval);
        SimulationResult acsResult = acs.schedule(config.processes, config.quantum, config.contextSwitch);
        System.out.println("\n--- ACS Demotion Matrix ---");
        System.out.println(DemotionMatrixFormatter.formatDemotionMatrix(acsResult.getTimeStepLogs()));
        System.out.println(DemotionMatrixFormatter.formatSummaryTable(acsResult));

        SJFScheduler sjf = new SJFScheduler();
        SimulationResult sjfResult = sjf.schedule(config.processes, config.quantum, config.contextSwitch);
        System.out.println(DemotionMatrixFormatter.formatSummaryTable(sjfResult));
    }

    private static void runBenchmarks() {
        String[] testFiles = {
                "test-cases/test_case_1.txt",
                "test-cases/test_case_2.txt",
                "test-cases/test_case_3.txt",
                "test-cases/test_case_4.txt"
        };
        int[] csValues = {0, 1, 2, 3};
        int[] qValues = {1, 2, 3, 5, 8, 10};

        try (PrintWriter out = new PrintWriter(new FileWriter("report/benchmark_results.csv"))) {
            out.println("test_case,scheduler,sweep,q,cs,awt,atat,art,cpu_util");

            for (String path : testFiles) {
                File f = new File(path);
                if (!f.exists()) {
                    continue;
                }
                String tcName = f.getName().replace(".txt", "");
                InputParser.SimulationConfig config = InputParser.parseFile(f);
                AdaptiveContainerScheduler acs = new AdaptiveContainerScheduler(new ACSAgingStrategy(), config.agingInterval);
                SJFScheduler sjf = new SJFScheduler();

                for (int cs : csValues) {
                    SimulationResult rAcs = acs.schedule(config.processes, config.quantum, cs);
                    out.printf("%s,ACS,CS,%d,%d,%.2f,%.2f,%.2f,%.2f%n",
                            tcName, config.quantum, cs, rAcs.getAverageWaitingTime(),
                            rAcs.getAverageTurnaroundTime(), rAcs.getAverageResponseTime(), rAcs.getCpuUtilization());

                    SimulationResult rSjf = sjf.schedule(config.processes, config.quantum, cs);
                    out.printf("%s,SJF,CS,%d,%d,%.2f,%.2f,%.2f,%.2f%n",
                            tcName, config.quantum, cs, rSjf.getAverageWaitingTime(),
                            rSjf.getAverageTurnaroundTime(), rSjf.getAverageResponseTime(), rSjf.getCpuUtilization());
                }

                for (int q : qValues) {
                    SimulationResult rAcs = acs.schedule(config.processes, q, config.contextSwitch);
                    out.printf("%s,ACS,Q,%d,%d,%.2f,%.2f,%.2f,%.2f%n",
                            tcName, q, config.contextSwitch, rAcs.getAverageWaitingTime(),
                            rAcs.getAverageTurnaroundTime(), rAcs.getAverageResponseTime(), rAcs.getCpuUtilization());
                }
            }
            System.out.println("Benchmarks written to report/benchmark_results.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
