package model;

import java.util.List;

public class TimeStepLog {
    private int time;
    private String runningProcessId;
    private boolean isContextSwitch;
    private String contextSwitchMessage;
    private List<String> tier1Snapshot;
    private List<String> tier2Snapshot;
    private String notes;

    public TimeStepLog(int time,
                       String runningProcessId,
                       boolean isContextSwitch,
                       String contextSwitchMessage,
                       List<String> tier1Snapshot,
                       List<String> tier2Snapshot,
                       String notes) {
        this.time = time;
        this.runningProcessId = runningProcessId;
        this.isContextSwitch = isContextSwitch;
        this.contextSwitchMessage = contextSwitchMessage;
        this.tier1Snapshot = tier1Snapshot;
        this.tier2Snapshot = tier2Snapshot;
        this.notes = notes;
    }

    public int getTime() { return time; }
    public String getRunningProcessId() { return runningProcessId; }
    public boolean isContextSwitch() { return isContextSwitch; }
    public String getContextSwitchMessage() { return contextSwitchMessage; }
    public List<String> getTier1Snapshot() { return tier1Snapshot; }
    public List<String> getTier2Snapshot() { return tier2Snapshot; }
    public String getNotes() { return notes; }
}
