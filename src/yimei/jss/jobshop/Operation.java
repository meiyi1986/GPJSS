package yimei.jss.jobshop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yimei on 22/09/16.
 */
public class Operation implements Comparable<Operation> {

    private final Job job;
    private final int id;
    private double procTime;
    private WorkCenter workCenter;
    private Operation next;

    // Attributes for simulation.
    private double readyTime;
    private double workRemaining;
    private int numOpsRemaining;
    private double flowDueDate;
    private double nextProcTime;
    private double priority;

    public Operation(Job job, int id, double procTime, WorkCenter workCenter) {
        this.job = job;
        this.id = id;
        this.procTime = procTime;
        this.workCenter = workCenter;
    }

    public Job getJob() {
        return job;
    }

    public int getId() {
        return id;
    }

    public double getProcTime() {
        return procTime;
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public Operation getNext() {
        return next;
    }

    public double getReadyTime() {
        return readyTime;
    }

    public double getWorkRemaining() {
        return workRemaining;
    }

    public int getNumOpsRemaining() {
        return numOpsRemaining;
    }

    public double getFlowDueDate() {
        return flowDueDate;
    }

    public double getNextProcTime() {
        return nextProcTime;
    }

    public double getPriority() {
        return priority;
    }

    public void setNext(Operation next) {
        this.next = next;

    }

    public void setWorkRemaining(double workRemaining) {
        this.workRemaining = workRemaining;
    }

    public void setNextProcTime(double nextProcTime) {
        this.nextProcTime = nextProcTime;
    }

    public void setNumOpsRemaining(int numOpsRemaining) {
        this.numOpsRemaining = numOpsRemaining;
    }

    public void setReadyTime(double readyTime) {
        this.readyTime = readyTime;
    }

    public void setFlowDueDate(double flowDueDate) {
        this.flowDueDate = flowDueDate;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    /**
     * Compare with another process based on priority.
     * @param other the other process.
     * @return true if prior to other, and false otherwise.
     */
    public boolean priorTo(Operation other) {
        if (Double.compare(priority, other.priority) < 0)
            return true;

        if (Double.compare(priority, other.priority) > 0)
            return false;

        return job.getId() < other.job.getId();
    }

    @Override
    public String toString() {
        return String.format("[J%d, O%d, W%d, T%.1f]",
                job.getId(), id, workCenter.getId(), procTime);
    }

    public boolean equals(Operation other) {
        return id == other.id;
    }

    @Override
    public int compareTo(Operation other) {
        if (readyTime < other.readyTime)
            return -1;

        if (readyTime > other.readyTime)
            return 1;

        return 0;
    }
}
