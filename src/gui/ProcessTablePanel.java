package gui;

import model.Process;
import model.SimulationResult;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProcessTablePanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel avgLabel = new JLabel(" Averages: -");

    public ProcessTablePanel() {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createTitledBorder("Process Metrics & Analysis Table"));

        String[] cols = {"Process", "Arrival", "Burst", "Priority", "Finish", "Turnaround", "Waiting", "Response"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < cols.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(avgLabel, BorderLayout.SOUTH);
    }

    public void displayInputProcesses(List<Process> processes) {
        model.setRowCount(0);
        if (processes != null) {
            for (Process p : processes) {
                model.addRow(new Object[]{p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getInitialPriority(), "-", "-", "-", "-"});
            }
        }
        avgLabel.setText(" Averages: -");
    }

    public void displayResults(SimulationResult result) {
        model.setRowCount(0);
        if (result == null) {
            return;
        }
        for (Process p : result.getProcesses()) {
            model.addRow(new Object[]{
                    p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getInitialPriority(),
                    p.getFinishTime(), p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime()
            });
        }
        avgLabel.setText(String.format(" Avg TAT: %.2f  |  Avg WT: %.2f  |  Avg RT: %.2f  |  CPU Util: %.2f%%",
                result.getAverageTurnaroundTime(), result.getAverageWaitingTime(),
                result.getAverageResponseTime(), result.getCpuUtilization()));
    }

    public int getSelectedProcessIndex() {
        return table.getSelectedRow();
    }
}
