import analytics.InputParser;
import analytics.MetricsCalculator;
import gui.MainFrame;
import model.Process;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);
                frame.setVisible(true);
            });
            return;
        }

        System.out.println("===  InputParser & MetricsCalculator ");

        try {
            File testFile = new File("test-cases/test_case_1.txt");
            InputParser.SimulationConfig config = InputParser.parseFile(testFile);

            System.out.println("Parsed Config:");
            System.out.println("  Quantum (Q): " + config.quantum);
            System.out.println("  Context Switch (CS): " + config.contextSwitch);
            System.out.println("  Processes count: " + config.processes.size());

            for (Process p : config.processes) {
                System.out.println("  -> " + p.getId() + " | Arrival: " + p.getArrivalTime()
                        + " | Burst: " + p.getBurstTime() + " | Priority: " + p.getInitialPriority());
            }

            Process p1 = config.processes.get(0);
            p1.setStartTime(0);
            p1.setFinishTime(9);

            Process p2 = config.processes.get(1);
            p2.setStartTime(3);
            p2.setFinishTime(13);

            Process p3 = config.processes.get(2);
            p3.setStartTime(6);
            p3.setFinishTime(8);

            for (Process p : config.processes) {
                MetricsCalculator.computeProcessMetrics(p);
                System.out.println(p.getId() + " -> Turnaround: " + p.getTurnaroundTime()
                        + ", Waiting: " + p.getWaitingTime()
                        + ", Response: " + p.getResponseTime());
            }

            double awt = MetricsCalculator.calculateAverageWaitingTime(config.processes);
            double atat = MetricsCalculator.calculateAverageTurnaroundTime(config.processes);
            double art = MetricsCalculator.calculateAverageResponseTime(config.processes);

            System.out.printf("Average Turnaround Time: %.2f%n", atat);
            System.out.printf("Average Waiting Time: %.2f%n", awt);
            System.out.printf("Average Response Time: %.2f%n", art);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
