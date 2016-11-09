package yimei.jss.jobshop;

/**
 * Created by yimei on 22/09/16.
 */
public class Process implements Comparable<Process> {

    private WorkCenter workCenter;
    private int machineId;
    private Operation operation;
    private double startTime;
    private double finishTime;

    public Process(WorkCenter workCenter, int machineId, Operation operation, double startTime) {
        this.workCenter = workCenter;
        this.machineId = machineId;
        this.operation = operation;
        this.startTime = startTime;
        this.finishTime = startTime + operation.getProcTime();
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public int getMachineId() {
        return machineId;
    }

    public Operation getOperation() {
        return operation;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public double getDuration() {
        return finishTime - startTime;
    }

    @Override
    public String toString() {
        return String.format("([W%d,M%d], [J%d,O%d]: %.1f --> %.1f.\n",
                workCenter.getId(), machineId, operation.getJob().getId(), operation.getId(), startTime, finishTime);
    }

    @Override
    public int compareTo(Process other) {
        if (startTime < other.startTime)
            return -1;

        if (startTime > other.startTime)
            return 1;

        return 0;
    }


}
