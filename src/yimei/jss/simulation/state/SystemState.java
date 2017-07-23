package yimei.jss.simulation.state;

import yimei.jss.jobshop.*;
import yimei.jss.rule.AbstractRule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    private AbstractRule routingRule;

    public SystemState(double clockTime, List<WorkCenter> workCenters,
                       List<Job> jobsInSystem, List<Job> jobsCompleted, AbstractRule routingRule) {
        this.clockTime = clockTime;
        this.workCenters = workCenters;
        this.jobsInSystem = jobsInSystem;
        this.jobsCompleted = jobsCompleted;
        this.routingRule = routingRule;
    }

    public SystemState(double clockTime, AbstractRule routingRule) {
        this(clockTime, new ArrayList<>(), new LinkedList<>(), new ArrayList<>(), routingRule);
    }

    public SystemState(AbstractRule routingRule) {
        this(0.0, routingRule);
    }

    public double getClockTime() {
        return clockTime;
    }

    public AbstractRule getRoutingRule() { return routingRule; }

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

    public void setRoutingRule(AbstractRule routingRule) {this.routingRule = routingRule; }

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

    private boolean verifyRestrictionsMet(List<Job> jobsCompleted) {
//        //as a basic start, let's go through the work centers and create an array with clocktime empty slots for each
//        //then we can fill in each array with the operation that was being worked on, and check none used the
//        //same work center at the same time
//        int numWorkCenters = workCenters.size();
//        int clockTime = (int) getClockTime();
//
//        int[][] workCenterAllocations = new int[numWorkCenters][clockTime];
//        for (int i = 0; i < numWorkCenters; ++i) {
//            for (int j = 0; j < clockTime; ++j) {
//                //ensure all have the same default value
//                workCenterAllocations[i][j] = -1;
//            }
//        }
//
//        int numJobs = 0;
//        int numProcess = 0;
//        for (Job job: jobsCompleted) {
//            for (ProcessFinishEvent processFinishEvent: job.getProcessFinishEvents()) {
//                Process p = processFinishEvent.getProcess();
//                int[] workCenterSchedule = workCenterAllocations[p.getWorkCenter().getId()];
//                for (int i = (int) p.getStartTime(); i < (int) p.getFinishTime(); ++i) {
//                    if (workCenterSchedule[i] == -1) {
//                        workCenterSchedule[i] = job.getId();
//                    } else {
//                        //System.out.println("Doubled up on the schedule");
//                        return false;
//                    }
//                }
//                numProcess++;
//            }
//            numJobs++;
//        }
        return true;
    }

    public void reset() {
        clockTime = 0.0;
        for (WorkCenter workCenter : workCenters) {
            workCenter.reset();
        }
        jobsInSystem.clear();
        jobsCompleted.clear();
    }

    public double slack(OperationOption operation) {
        return operation.getOperation().getJob().getDueDate()
                - getClockTime() - operation.getWorkRemaining();
    }

    public double workInNextQueue(OperationOption operation) {
        OperationOption nextOp = operation.getNext(this);
        if (nextOp == null) {
            return 0;
        }

        return nextOp.getWorkCenter().getWorkInQueue();
    }

    public double numOpsInNextQueue(OperationOption operation) {
        OperationOption nextOp = operation.getNext(this);
        if (nextOp == null) {
            return 0;
        }

        return nextOp.getWorkCenter().getQueue().size();
    }

    public double nextReadyTime(OperationOption operation) {
        OperationOption nextOp = operation.getNext(this);
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

        //rules do not maintain state
        return new SystemState(clockTime, clonedWCs,
                new LinkedList<>(), new ArrayList<>(), routingRule);
    }
}
