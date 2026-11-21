package gui;

import model.SimulationResult;

import javax.swing.*;

public class MainFrame extends JFrame {

    private ProcessTablePanel processTablePanel;
    private GanttChartPanel ganttChartPanel;
    private QueueViewPanel queueViewPanel;

    public MainFrame() {
        super("Cloud-Cluster CPU Scheduling Simulator");
        initUI();
    }

    private void initUI() {
    }

    public void updateResults(SimulationResult result) {
    }
}
