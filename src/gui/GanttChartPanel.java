package gui;

import model.SimulationResult;
import model.TimeStepLog;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GanttChartPanel extends JPanel {

    private SimulationResult result;
    private final Map<String, Color> colors = new LinkedHashMap<>();
    private final Color[] palette = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(239, 68, 68),
            new Color(139, 92, 246),
            new Color(6, 182, 212)
    };

    public GanttChartPanel() {
        setBorder(BorderFactory.createTitledBorder("Execution Timeline (Gantt Chart)"));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(900, 175));
    }

    public void displayTimeline(SimulationResult result) {
        this.result = result;
        colors.clear();
        if (result != null) {
            int i = 0;
            for (model.Process p : result.getProcesses()) {
                colors.put(p.getId(), palette[i++ % palette.length]);
            }
            setPreferredSize(new Dimension(Math.max(900, (result.getTotalSimulationTime() + 2) * 44 + 40), 175));
            revalidate();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (result == null || result.getTimeStepLogs().isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("Run a simulation to display the Gantt chart.", 24, 85);
            return;
        }

        int cellW = 44, cellH = 52, startX = 24, startY = 58;
        int maxT = result.getTotalSimulationTime();
        List<TimeStepLog> logs = result.getTimeStepLogs();

        int lx = startX;
        for (Map.Entry<String, Color> e : colors.entrySet()) {
            g2.setColor(e.getValue());
            g2.fillRoundRect(lx, 26, 12, 12, 4, 4);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(e.getKey(), lx + 16, 36);
            lx += 55;
        }
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRoundRect(lx, 26, 12, 12, 4, 4);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("CS (Context Switch)", lx + 16, 36);

        for (int t = 0; t < maxT; t++) {
            TimeStepLog log = findTick(logs, t);
            int x = startX + t * cellW;
            boolean isCs = log != null && log.isContextSwitch();
            String label = isCs ? "CS" : (log != null && log.getRunningProcessId() != null ? log.getRunningProcessId() : "IDLE");
            Color fill = isCs ? Color.LIGHT_GRAY : colors.getOrDefault(label, new Color(235, 235, 235));

            g2.setColor(fill);
            g2.fillRoundRect(x + 2, startY, cellW - 4, cellH, 8, 8);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(x + 2, startY, cellW - 4, cellH, 8, 8);

            g2.setColor(isCs || label.equals("IDLE") ? Color.DARK_GRAY : Color.WHITE);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, x + (cellW - fm.stringWidth(label)) / 2, startY + 31);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g2.drawString(String.valueOf(t), x, startY + cellH + 16);
        }
        g2.drawString(String.valueOf(maxT), startX + maxT * cellW, startY + cellH + 16);
    }

    private TimeStepLog findTick(List<TimeStepLog> logs, int t) {
        for (TimeStepLog log : logs) {
            if (log.getTime() == t && (log.isContextSwitch() || log.getRunningProcessId() != null)) {
                return log;
            }
        }
        return null;
    }
}
