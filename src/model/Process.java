package model;

public class Process {
    private String id;
    private int arrivalTime;
    private int burstTime;
    private int initialPriority;

    private int remainingTime;
    private int effectivePriority;
    private int startTime = -1;
    private int finishTime = -1;
    private int waitingTime = 0;
    private int turnaroundTime = 0;
    private int responseTime = -1;

    public enum Tier {
        TIER_1_RR,
        TIER_2_PRIORITY
    }

    private Tier currentTier = Tier.TIER_1_RR;
    private int tier2WaitStartTime = -1;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.initialPriority = priority;
        this.remainingTime = burstTime;
        this.effectivePriority = priority;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(int arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getBurstTime() { return burstTime; }
    public void setBurstTime(int burstTime) { this.burstTime = burstTime; }

    public int getInitialPriority() { return initialPriority; }
    public void setInitialPriority(int initialPriority) { this.initialPriority = initialPriority; }

    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int remainingTime) { this.remainingTime = remainingTime; }

    public int getEffectivePriority() { return effectivePriority; }
    public void setEffectivePriority(int effectivePriority) { this.effectivePriority = effectivePriority; }

    public int getStartTime() { return startTime; }
    public void setStartTime(int startTime) { this.startTime = startTime; }

    public int getFinishTime() { return finishTime; }
    public void setFinishTime(int finishTime) { this.finishTime = finishTime; }

    public int getWaitingTime() { return waitingTime; }
    public void setWaitingTime(int waitingTime) { this.waitingTime = waitingTime; }

    public int getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime = turnaroundTime; }

    public int getResponseTime() { return responseTime; }
    public void setResponseTime(int responseTime) { this.responseTime = responseTime; }

    public Tier getCurrentTier() { return currentTier; }
    public void setCurrentTier(Tier currentTier) { this.currentTier = currentTier; }

    public int getTier2WaitStartTime() { return tier2WaitStartTime; }
    public void setTier2WaitStartTime(int tier2WaitStartTime) { this.tier2WaitStartTime = tier2WaitStartTime; }
}
