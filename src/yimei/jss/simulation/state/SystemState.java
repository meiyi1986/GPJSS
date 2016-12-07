package yimei.jss.simulation.state;

import yimei.jss.jobshop.*;
import yimei.jss.jobshop.Process;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.event.AbstractEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The state of the discrete event simulation system.
 *
 * Created by yimei on 22/09/16.
 */
public class SystemState {

    private double clockTime;
    private List<WorkCenter> workCenters;
    private List<Job> jobsInSystem;
    private List<Job> jobsCompleted;

    public SystemState(double clockTime, List<WorkCenter> workCenters,
                       List<Job> jobsInSystem, List<Job> jobsCompleted) {
        this.clockTime = clockTime;
        this.workCenters = workCenters;
        this.jobsInSystem = jobsInSystem;
        this.jobsCompleted = jobsCompleted;
    }

    public SystemState(double clockTime) {
        this(clockTime, new ArrayList<>(), new LinkedList<>(), new ArrayList<>());
    }

    public SystemState() {
        this(0.0);
    }

    public double getClockTime() {
        return clockTime;
    }

    public List<WorkCenter> getWorkCenters() {
        return workCenters;
    }

    public WorkCenter getWorkCenter(int idx) {
        return workCenters.get(idx);
    }

    public List<Job> getJobsInSystem() {
        return jobsInSystem;
    }

    public List<Job> getJobsCompleted() {
        return jobsCompleted;
    }

    public void setClockTime(double clockTime) {
        this.clockTime = clockTime;
    }

    public void setWorkCenters(List<WorkCenter> workCenters) {
        this.workCenters = workCenters;
    }

    public void setJobsInSystem(List<Job> jobsInSystem) {
        this.jobsInSystem = jobsInSystem;
    }

    public void setJobsCompleted(List<Job> jobsCompleted) {
        this.jobsCompleted = jobsCompleted;
    }

    public void addWorkCenter(WorkCenter workCenter) {
        workCenters.add(workCenter);
    }

    public void addJobToSystem(Job job) {
        jobsInSystem.add(job);
    }

    public void removeJobFromSystem(Job job) {
        jobsInSystem.remove(job);
    }

    public void addCompletedJob(Job job) {
        jobsCompleted.add(job);
    }

    public void reset() {
        clockTime = 0.0;
        for (WorkCenter workCenter : workCenters) {
            workCenter.reset();
        }
        jobsInSystem.clear();
        jobsCompleted.clear();
    }

    public double slack(Operation operation) {
        return operation.getJob().getDueDate()
                - getClockTime() - operation.getWorkRemaining();
    }

    public double workInNextQueue(Operation operation) {
        Operation nextOp = operation.getNext();
        if (nextOp == null) {
            return 0;
        }

        return nextOp.getWorkCenter().getWorkInQueue();
    }

    public double numOpsInNextQueue(Operation operation) {
        Operation nextOp = operation.getNext();
        if (nextOp == null) {
            return 0;
        }

        return nextOp.getWorkCenter().getQueue().size();
    }

    public double nextReadyTime(Operation operation) {
        Operation nextOp = operation.getNext();
        if (nextOp == null) {
            return 0;
        }

        return nextOp.getWorkCenter().getReadyTime();
    }

    @Override
    public SystemState clone() {
        List<WorkCenter> clonedWCs = new ArrayList<>();
        for (WorkCenter wc : workCenters) {
            clonedWCs.add(wc.clone());
        }

        return new SystemState(clockTime, clonedWCs,
                new LinkedList<>(), new ArrayList<>());
    }
}
