package gui;

import analytics.InputParser;
import model.Process;
import model.Scheduler;
import model.SimulationResult;
import schedulers.ACSAgingStrategy;
import schedulers.AdaptiveContainerScheduler;
import schedulers.SJFScheduler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private final ProcessTablePanel processTablePanel = new ProcessTablePanel();
    private final QueueViewPanel queueViewPanel = new QueueViewPanel();
    private final GanttChartPanel ganttChartPanel = new GanttChartPanel();

    private final JComboBox<String> schedulerBox = new JComboBox<>(new String[]{
            "Adaptive Container Scheduler (ACS)",
            "Preemptive Shortest-Job First (SJF)"
    });
    private final JTextField quantumField = new JTextField("3", 3);
    private final JTextField csField = new JTextField("1", 3);
    private final JTextField agingField = new JTextField("4", 3);
    private final JLabel statusLabel = new JLabel(" Ready");

    private List<Process> processes = new ArrayList<>();

    public MainFrame() {
        super("Cloud-Cluster CPU Scheduling Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        initUI();
        loadDefaultFile();
        runSimulation();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controls.setBackground(new Color(245, 247, 250));
        controls.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 232)));

        JButton loadBtn = new JButton("Load File");
        JButton addBtn = new JButton("+ Process");
        JButton removeBtn = new JButton("Remove");
        JButton clearBtn = new JButton("Clear");
        JButton runBtn = new JButton("Run Simulation");
        runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));

        schedulerBox.addActionListener(e -> {
            boolean isAcs = schedulerBox.getSelectedIndex() == 0;
            quantumField.setEnabled(isAcs);
            agingField.setEnabled(isAcs);
            if (!processes.isEmpty()) {
                runSimulation();
            }
        });

        quantumField.addActionListener(e -> runSimulation());
        csField.addActionListener(e -> runSimulation());
        agingField.addActionListener(e -> runSimulation());

        loadBtn.addActionListener(e -> loadFromFile());
        addBtn.addActionListener(e -> addProcessDialog());
        removeBtn.addActionListener(e -> removeSelected());
        clearBtn.addActionListener(e -> clearAll());
        runBtn.addActionListener(e -> runSimulation());

        controls.add(new JLabel("Algorithm:"));
        controls.add(schedulerBox);
        controls.add(new JLabel("Q:"));
        controls.add(quantumField);
        controls.add(new JLabel("CS:"));
        controls.add(csField);
        controls.add(new JLabel("Aging:"));
        controls.add(agingField);
        controls.add(loadBtn);
        controls.add(addBtn);
        controls.add(removeBtn);
        controls.add(clearBtn);
        controls.add(runBtn);

        add(controls, BorderLayout.NORTH);

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, processTablePanel, queueViewPanel);
        topSplit.setResizeWeight(0.45);

        JScrollPane ganttScroll = new JScrollPane(ganttChartPanel);
        ganttScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, ganttScroll);
        mainSplit.setResizeWeight(0.66);
        mainSplit.setBorder(new EmptyBorder(4, 8, 4, 8));
        add(mainSplit, BorderLayout.CENTER);

        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void loadDefaultFile() {
        File file = new File("test-cases/test_case_2.txt");
        if (file.exists()) {
            try {
                applyConfig(InputParser.parseFile(file));
                return;
            } catch (Exception ignored) {
            }
        }
        processes.add(new Process("P1", 0, 5, 4));
        processes.add(new Process("P2", 1, 6, 2));
        processes.add(new Process("P3", 4, 4, 3));
        processTablePanel.displayInputProcesses(processes);
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser(new File("test-cases"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                InputParser.SimulationConfig config = InputParser.parseFile(chooser.getSelectedFile());
                if (config.processes.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No valid processes found in file.",
                            "Empty File", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                applyConfig(config);
                runSimulation();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyConfig(InputParser.SimulationConfig config) {
        processes = new ArrayList<>(config.processes);
        quantumField.setText(String.valueOf(config.quantum));
        csField.setText(String.valueOf(config.contextSwitch));
        agingField.setText(String.valueOf(config.agingInterval));
        processTablePanel.displayInputProcesses(processes);
    }

    private void addProcessDialog() {
        JTextField idF = new JTextField("P" + (processes.size() + 1));
        JTextField arrF = new JTextField("0");
        JTextField burstF = new JTextField("4");
        JTextField prioF = new JTextField("2");

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Process ID:"));
        form.add(idF);
        form.add(new JLabel("Arrival Time:"));
        form.add(arrF);
        form.add(new JLabel("Burst Time:"));
        form.add(burstF);
        form.add(new JLabel("Priority:"));
        form.add(prioF);

        if (JOptionPane.showConfirmDialog(this, form, "Add Process", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String id = idF.getText().trim();
                int arr = Integer.parseInt(arrF.getText().trim());
                int burst = Integer.parseInt(burstF.getText().trim());
                int prio = Integer.parseInt(prioF.getText().trim());
                if (id.isEmpty() || arr < 0 || burst <= 0 || prio <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "ID cannot be empty, Arrival >= 0, Burst > 0, and Priority > 0.",
                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                processes.add(new Process(id, arr, burst, prio));
                runSimulation();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid integers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelected() {
        int idx = processTablePanel.getSelectedProcessIndex();
        if (idx < 0 || idx >= processes.size()) {
            statusLabel.setText(" Select a process row in the table first to remove it.");
            return;
        }
        String removedId = processes.remove(idx).getId();
        if (processes.isEmpty()) {
            clearViews();
            statusLabel.setText(" Removed " + removedId + ". Workload is now empty.");
        } else {
            runSimulation();
        }
    }

    private void clearAll() {
        if (processes.isEmpty()) {
            return;
        }
        processes.clear();
        clearViews();
        statusLabel.setText(" Cleared all processes.");
    }

    private void clearViews() {
        processTablePanel.displayInputProcesses(processes);
        queueViewPanel.clear();
        ganttChartPanel.displayTimeline(null);
    }

    private void runSimulation() {
        if (processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add or load at least one process first.",
                    "No Processes", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int q = Integer.parseInt(quantumField.getText().trim());
            int cs = Integer.parseInt(csField.getText().trim());
            int aging = Integer.parseInt(agingField.getText().trim());
            if (q <= 0 || cs < 0 || aging <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Quantum > 0, Context Switch >= 0, and Aging > 0 are required.",
                        "Invalid Parameters", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Scheduler scheduler = (schedulerBox.getSelectedIndex() == 0)
                    ? new AdaptiveContainerScheduler(new ACSAgingStrategy(), aging)
                    : new SJFScheduler();

            SimulationResult result = scheduler.schedule(processes, q, cs);
            updateResults(result);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Q, CS, and Aging must be integers.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateResults(SimulationResult result) {
        processTablePanel.displayResults(result);
        queueViewPanel.displayQueues(result.getTimeStepLogs());
        ganttChartPanel.displayTimeline(result);
        statusLabel.setText(String.format(
                " %s  |  Avg TAT: %.2f  |  Avg WT: %.2f  |  Avg RT: %.2f  |  CPU Util: %.2f%%  |  Total Time: %d",
                result.getSchedulerName(),
                result.getAverageTurnaroundTime(),
                result.getAverageWaitingTime(),
                result.getAverageResponseTime(),
                result.getCpuUtilization(),
                result.getTotalSimulationTime()
        ));
    }
}
