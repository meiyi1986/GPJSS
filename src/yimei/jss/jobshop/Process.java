package yimei.jss.jobshop;

/**
 * Created by yimei on 22/09/16.
 */
public class Process implements Comparable<Process> {

    private WorkCenter workCenter;
    private int machineId;
    private OperationOption operation;
    private double startTime;
    private double finishTime;

    public Process(WorkCenter workCenter, int machineId, OperationOption operation, double startTime) {
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

    public OperationOption getOperation() {
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
        return String.format("([W%d,M%d], [J%d,O%d,O%d]: %.1f --> %.1f.\n",
                workCenter.getId(), machineId, operation.getJob().getId(),
                operation.getOperation().getId(), operation.getOptionId(), startTime, finishTime);
    }

    @Override
    public int compareTo(Process other) {
        if (startTime < other.startTime)
            return -1;

        if (startTime > other.startTime)
            return 1;

        return 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Process process = (Process) o;

        if (machineId != process.machineId) return false;
        if (Double.compare(process.startTime, startTime) != 0) return false;
        if (Double.compare(process.finishTime, finishTime) != 0) return false;
        if (workCenter != null ? !workCenter.equals(process.workCenter) : process.workCenter != null) return false;
        return operation != null ? operation.equals(process.operation) : process.operation == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = workCenter != null ? workCenter.hashCode() : 0;
        result = 31 * result + machineId;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        temp = Double.doubleToLongBits(startTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(finishTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
