import gui.MainFrame;
import model.Process;
import model.Scheduler;
import model.SimulationResult;
import schedulers.AdaptiveContainerScheduler;
import schedulers.SJFScheduler;

import javax.swing.*;
import java.util.List;

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

        System.out.println("Cloud-Cluster CPU Scheduling Simulator");
    }
}
