package gui;

import analytics.DemotionMatrixFormatter;
import model.TimeStepLog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class QueueViewPanel extends JPanel {

    private final JTextArea logArea = new JTextArea();

    public QueueViewPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Heuristic Demotion Matrix & Tier Queues"));

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    public void displayQueues(List<TimeStepLog> logs) {
        logArea.setText(DemotionMatrixFormatter.formatDemotionMatrix(logs));
        logArea.setCaretPosition(0);
    }

    public void clear() {
        logArea.setText("");
    }
}
