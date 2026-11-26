package schedulers;

import analytics.MetricsCalculator;
import model.Process;
import model.Scheduler;
import model.SimulationResult;
import model.TimeStepLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SJFScheduler implements Scheduler {

    @Override
    public String getName() {
        return "Preemptive Shortest-Job First (SJF)";
    }

    @Override
    public SimulationResult schedule(List<Process> processes, int quantum, int contextSwitchTime) {
        if (processes == null || processes.isEmpty()) {
            return new SimulationResult(
                    getName(), quantum, contextSwitchTime,
                    new ArrayList<>(), new ArrayList<>(),
                    0.0, 0.0, 0.0, 0.0, 0
            );
        }

        // Copy input processes
        List<Process> simulationList = new ArrayList<>();
        for (Process p : processes) {
            simulationList.add(new Process(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getInitialPriority()));
        }

        List<TimeStepLog> timeStepLogs = new ArrayList<>();
        int currentTime = 0;
        int completedCount = 0;
        int totalProcesses = simulationList.size();
        int busyTime = 0;

        Process currentRunning = null;
        Process lastExecuted = null;

        // Comparator: shortest remaining time, then earlier arrival, then ID
        Comparator<Process> srtfComparator = (p1, p2) -> {
            int remComparison = Integer.compare(p1.getRemainingTime(), p2.getRemainingTime());
            if (remComparison != 0) return remComparison;
            int arrComparison = Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            if (arrComparison != 0) return arrComparison;
            return p1.getId().compareTo(p2.getId());
        };

        // Main simulation loop
        while (completedCount < totalProcesses) {
            // Find ready processes
            List<Process> readyPool = new ArrayList<>();
            for (Process p : simulationList) {
                if (p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0) {
                    readyPool.add(p);
                }
            }

            //Hassib: hena dlw2ti i will add setting lastExecuted = null, 3a4an dlw2ti boso
            //Hassib: dlw2ti lw P1 e4t8lt l7ad T = 3, w P2 htwslna 3ala T = 5, fa lama T = 5 tege, w nla2i en lastExecuted == P1, fa keda hn7ot context switch me4 me7tageno asasen ma7na b2alna T = 4 idle, no7ot context switch kaman? 
            // fa simply el fix is to add lastExecuted = null lama nkon fi idle state
            // CPU idle step
            if (readyPool.isEmpty()) {
                timeStepLogs.add(new TimeStepLog(currentTime, null, false, null, new ArrayList<>(), new ArrayList<>(), "CPU Idle"));
                currentTime++;
                lastExecuted = null;
                continue;
            }

            // Pick shortest remaining process
            readyPool.sort(srtfComparator);
            Process bestCandidate = readyPool.get(0);

            Process nextToRun = bestCandidate;
            if (currentRunning != null && currentRunning.getRemainingTime() > 0) {
                if (currentRunning.getRemainingTime() <= bestCandidate.getRemainingTime()) {
                    nextToRun = currentRunning;
                }
            }

            // Context switch handling
            if (lastExecuted != null && lastExecuted != nextToRun && contextSwitchTime > 0) {
                String csMessage;
                if (lastExecuted.getRemainingTime() > 0) {
                    csMessage = "Context switch saves " + lastExecuted.getId() + " and loads " + nextToRun.getId();
                } else {
                    csMessage = "Context switch loads " + nextToRun.getId();
                }

                for (int cs = 0; cs < contextSwitchTime; cs++) {
                    List<String> readySnapshot = getReadySnapshot(simulationList, currentTime, null, srtfComparator);
                    timeStepLogs.add(new TimeStepLog(currentTime, null, true, csMessage, readySnapshot, new ArrayList<>(), "Context Switch"));
                    currentTime++;
                }

                // Hassib: dol lazem yet4alo 3a4an 8alat w feeh case beybozo feeha
                // Hassib: la2en fo2 already e7na 3mlna context switch w el mafrood y7sal howa awel ma el dispatcher y3ml context switch w yebos 3ala el nexttorun, 
                // Hassib: w beyload el PCB w kol elkalam el7lw da, minf34 agi 3ala tol w a4of 7ad tani w atrod elnexttorun w a7ot makano 7ad gded.
                // Hassib: summary:elcontext switch men ela5er e4t8l 3ala instance men nexttorun mo3ayan w gowa el function kanet bt8ayar elnexttorun.
                // Hassib: law 7ad mo3tared, please speak up.
                /* what's getting removed in my opinion ta7t aho */
                // readyPool.clear();
                // for (Process p : simulationList) {
                //     if (p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0) {
                //         readyPool.add(p);
                //     }
                // }
                // readyPool.sort(srtfComparator);
                // if (!readyPool.isEmpty()) {
                //     nextToRun = readyPool.get(0);
                // }
            }

            // Execute process for 1 time unit
            currentRunning = nextToRun;

            if (currentRunning.getStartTime() == -1) {
                currentRunning.setStartTime(currentTime);
            }

            List<String> readySnapshot = getReadySnapshot(simulationList, currentTime, currentRunning, srtfComparator);
            timeStepLogs.add(new TimeStepLog(currentTime, currentRunning.getId(), false, null, readySnapshot, new ArrayList<>(), "Running " + currentRunning.getId()));

            currentRunning.setRemainingTime(currentRunning.getRemainingTime() - 1);
            busyTime++;
            currentTime++;

            // Check if process finished
            if (currentRunning.getRemainingTime() == 0) {
                currentRunning.setFinishTime(currentTime);
                completedCount++;
                lastExecuted = currentRunning;
                currentRunning = null;
            } else {
                lastExecuted = currentRunning;
            }
        }

        // Compute metrics
        for (Process p : simulationList) {
            MetricsCalculator.computeProcessMetrics(p);
        }

        for (Process original : processes) {
            for (Process simulated : simulationList) {
                if (original.getId().equals(simulated.getId())) {
                    original.setStartTime(simulated.getStartTime());
                    original.setFinishTime(simulated.getFinishTime());
                    original.setWaitingTime(simulated.getWaitingTime());
                    original.setTurnaroundTime(simulated.getTurnaroundTime());
                    original.setResponseTime(simulated.getResponseTime());
                    break;
                }
            }
        }

        double avgWaitingTime = MetricsCalculator.calculateAverageWaitingTime(simulationList);
        double avgTurnaroundTime = MetricsCalculator.calculateAverageTurnaroundTime(simulationList);
        double avgResponseTime = MetricsCalculator.calculateAverageResponseTime(simulationList);
        double cpuUtilization = MetricsCalculator.calculateCpuUtilization(busyTime, currentTime);

        return new SimulationResult(
                getName(), quantum, contextSwitchTime,
                simulationList, timeStepLogs,
                avgWaitingTime, avgTurnaroundTime, avgResponseTime,
                cpuUtilization, currentTime
        );
    }

    //Hassib: hena elmafrood nbuild tier1Snapshot list to display tl processes waiting fi el ready queue
    // bas eli bi7sl en getReadySnapshot betloop 3ala simulationList eli howa me4 sorted 3ala SRTF priority order
    // fa elsnapshot will simply display unsorted processes w eli elmafrood y7sl eno ydisplay SRTF priority order, fa lw 7atena el comparator m3ana ka paremeter
    // el trrace log w el GUI will display sorted data
    // elfix heya eno no7to el comparator ka parameter m3ana w simply sort them 
    // Ready queue snapshot helper
    private List<String> getReadySnapshot(List<Process> processes, int currentTime, Process running, Comparator<Process> comparator) {
        // List<String> snapshot = new ArrayList<>(); // we simply use it ba3d ma 3amalna sort lel processes sa7
        List<Process> ready = new ArrayList<>();
        for (Process p : processes) {
            if (p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0) {
                if (running == null || !p.getId().equals(running.getId())) {
                    // snapshot.add(p.getId() + " (remaining: " + p.getRemainingTime() + ")");
                    ready.add(p);
                }
            }
        }
        ready.sort(comparator);
        List<String> snapshot = new ArrayList<>();
        for (Process p: ready){
            snapshot.add(p.getId() + " (remaining: " + p.getRemainingTime() + ")");
        }

        return snapshot;
    }
}
