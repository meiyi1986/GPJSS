package yimei.jss.simulation;

import yimei.jss.jobshop.Job;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.Process;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.event.*;
import yimei.jss.simulation.state.SystemState;
import java.util.Iterator;
import java.util.PriorityQueue;

import static java.util.Arrays.fill;

/**
 * The abstract simulation class for evaluating rules.
 *
 * Created by yimei on 21/11/16.
 */
public abstract class Simulation {
    @Override
    public String toString() {
        return "Simulation{" +
                "sequencingRule=" + sequencingRule +
                ", routingRule=" + routingRule +
                ", systemState=" + systemState +
                ", eventQueue=" + eventQueue +
                ", numWorkCenters=" + numWorkCenters +
                ", numJobsRecorded=" + numJobsRecorded +
                ", warmupJobs=" + warmupJobs +
                ", numJobsArrived=" + numJobsArrived +
                ", throughput=" + throughput +
                '}';
    }

    protected AbstractRule sequencingRule;
    protected AbstractRule routingRule;
    protected SystemState systemState;
    protected PriorityQueue<AbstractEvent> eventQueue;

    protected int numWorkCenters;
    protected int numJobsRecorded;
    protected int warmupJobs;
    protected int numJobsArrived;
    protected int throughput;
    //protected int[] jobStates;

    public Simulation(AbstractRule sequencingRule,
                      AbstractRule routingRule,
                      int numWorkCenters,
                      int numJobsRecorded,
                      int warmupJobs) {
        this.sequencingRule = sequencingRule;
        this.routingRule = routingRule;
        this.numWorkCenters = numWorkCenters;
        this.numJobsRecorded = numJobsRecorded;
        this.warmupJobs = warmupJobs;

        systemState = new SystemState();
        eventQueue = new PriorityQueue<>();
//        int[] jobStates = new int[numJobsRecorded];
//        fill(jobStates, -1);
//        this.jobStates = jobStates;
    }

    public AbstractRule getSequencingRule() {
        return sequencingRule;
    }

//    public int[] getJobStates() { return jobStates; }

    public AbstractRule getRoutingRule() {
        return routingRule;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public PriorityQueue<AbstractEvent> getEventQueue() {
        return eventQueue;
    }

    public void setSequencingRule(AbstractRule sequencingRule) {
        this.sequencingRule = sequencingRule;
    }

//    public void setJobStates(int[] jobStates) { this.jobStates = jobStates; }

    public void setRoutingRule(AbstractRule routingRule) {
        this.routingRule = routingRule;
        //need to reset state as well, as the operationoptions associated
        //with workcenters are chosen using this routing rule, so current
        //values are outdated
        resetState();
    }

    public double getClockTime() {
        return systemState.getClockTime();
    }

    public void addEvent(AbstractEvent event) {
        eventQueue.add(event);
    }

    public boolean canAddToQueue(Process process) {
        Iterator<AbstractEvent> e = eventQueue.iterator();
        if (e.hasNext()) {
            AbstractEvent a = e.next();
            if (a instanceof ProcessStartEvent) {
                if (((ProcessStartEvent) a).getProcess().getWorkCenter().getId() ==
                        process.getWorkCenter().getId()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void run() {
        while (!eventQueue.isEmpty() && throughput < numJobsRecorded) {
            AbstractEvent nextEvent = eventQueue.poll();
//            OperationOption o = null;
//            if (nextEvent instanceof ProcessFinishEvent) {
//                o = ((ProcessFinishEvent) nextEvent).getProcess().getOperationOption();
//            } else if (nextEvent instanceof  OperationVisitEvent) {
//                o = ((OperationVisitEvent) nextEvent).getOperationOption();
//            } else if (nextEvent instanceof ProcessStartEvent) {
//                o = ((ProcessStartEvent) nextEvent).getProcess().getOperationOption();
//            }
            //if (!eventIsDuplicate(nextEvent)) {
            systemState.setClockTime(nextEvent.getTime());
            nextEvent.trigger(this);
            //}
        }
        if (!systemState.getJobsInSystem().isEmpty()) {
            System.out.println("Event queue is empty but simulation is not complete.");
            System.out.println("Makespan is garbage - cannot continue.");
            System.exit(0);
        }

    }

//    private boolean eventIsDuplicate(AbstractEvent event) {
//        if (event instanceof ProcessFinishEvent) {
//            Process p = ((ProcessFinishEvent) event).getProcess();
//            //want to check whether this operation has already been performed
//            int jobId = p.getOperationOption().getJob().getId();
//            if (jobId >= 0) {
//                int jobState = jobStates[jobId];
//                int opNum = p.getOperationOption().getOperation().getId();
//                if ((jobState+1) != opNum) {
//                    //upcoming event should only be the next job in the sequence,
//                    //not a job we've already done, or one ahead of the next one
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public void rerun() {
        resetState();

        run();
    }

    public void completeJob(Job job) {
        if (numJobsArrived > warmupJobs && job.getId() >= 0
                && job.getId() < numJobsRecorded + warmupJobs) {
            throughput++;

            systemState.addCompletedJob(job);
        }
        systemState.removeJobFromSystem(job);
    }

    public double makespan() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.getCompletionTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.flowTime();
        }

        return value / numJobsRecorded;
    }

    public double maxFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.flowTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanWeightedFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.weightedFlowTime();
        }

        return value / numJobsRecorded;
    }

    public double maxWeightedFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.weightedFlowTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.tardiness();
        }

        return value / numJobsRecorded;
    }

    public double maxTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.tardiness();

            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanWeightedTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.weightedTardiness();
        }

        return value / numJobsRecorded;
    }

    public double maxWeightedTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.weightedTardiness();

            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double propTardyJobs() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            if (job.getCompletionTime() > job.getDueDate())
                value ++;
        }

        return value / numJobsRecorded;
    }

    public double objectiveValue(Objective objective) {
        switch (objective) {
            case MAKESPAN:
                return makespan();
            case MEAN_FLOWTIME:
                return meanFlowtime();
            case MAX_FLOWTIME:
                return maxFlowtime();
            case MEAN_WEIGHTED_FLOWTIME:
                return meanWeightedFlowtime();
            case MAX_WEIGHTED_FLOWTIME:
                return maxWeightedFlowtime();
            case MEAN_TARDINESS:
                return meanTardiness();
            case MAX_TARDINESS:
                return maxTardiness();
            case MEAN_WEIGHTED_TARDINESS:
                return meanWeightedTardiness();
            case MAX_WEIGHTED_TARDINESS:
                return maxWeightedTardiness();
            case PROP_TARDY_JOBS:
                return propTardyJobs();
        }

        return -1.0;
    }

    public double workCenterUtilLevel(int idx) {
        return systemState.getWorkCenter(idx).getBusyTime() / getClockTime();
    }

    public String workCenterUtilLevelsToString() {
        String string = "[";
        for (int i = 0; i < systemState.getWorkCenters().size(); i++) {
            string += String.format("%.3f ", workCenterUtilLevel(i));
        }
        string += "]";

        return string;
    }

    public abstract void setup();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Simulation that = (Simulation) o;

        if (numWorkCenters != that.numWorkCenters) return false;
        if (numJobsRecorded != that.numJobsRecorded) return false;
        if (warmupJobs != that.warmupJobs) return false;
        if (numJobsArrived != that.numJobsArrived) return false;
        if (throughput != that.throughput) return false;
        if (sequencingRule != null ? !sequencingRule.equals(that.sequencingRule) : that.sequencingRule != null)
            return false;
        if (routingRule != null ? !routingRule.equals(that.routingRule) : that.routingRule != null) return false;
        if (systemState != null ? !systemState.equals(that.systemState) : that.systemState != null) return false;
        return eventQueue != null ? eventQueue.equals(that.eventQueue) : that.eventQueue == null;
    }

    @Override
    public int hashCode() {
        int result = sequencingRule != null ? sequencingRule.hashCode() : 0;
        result = 31 * result + (routingRule != null ? routingRule.hashCode() : 0);
        result = 31 * result + (systemState != null ? systemState.hashCode() : 0);
        result = 31 * result + (eventQueue != null ? eventQueue.hashCode() : 0);
        result = 31 * result + numWorkCenters;
        result = 31 * result + numJobsRecorded;
        result = 31 * result + warmupJobs;
        result = 31 * result + numJobsArrived;
        result = 31 * result + throughput;
        return result;
    }

    public abstract void resetState();
    public abstract void reset();
    public abstract void rotateSeed();
    public abstract void generateJob();
    public abstract Simulation surrogate(int numWorkCenters, int numJobsRecorded,
                                         int warmupJobs);
    public abstract Simulation surrogateBusy(int numWorkCenters, int numJobsRecorded,
                                             int warmupJobs);
}