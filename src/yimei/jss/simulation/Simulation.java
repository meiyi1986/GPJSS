package yimei.jss.simulation;

import yimei.jss.jobshop.Job;
import yimei.jss.jobshop.Objective;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.event.AbstractEvent;
import yimei.jss.simulation.state.SystemState;

import java.util.PriorityQueue;

/**
 * The abstract simulation class for evaluating rules.
 *
 * Created by yimei on 21/11/16.
 */
public abstract class Simulation {

    protected AbstractRule rule;
    protected SystemState systemState;
    protected PriorityQueue<AbstractEvent> eventQueue;

    protected int numWorkCenters;
    protected int numJobsRecorded;
    protected int warmupJobs;
    protected int numJobsArrived;
    protected int throughput;

    public Simulation(AbstractRule rule,
                      int numWorkCenters,
                      int numJobsRecorded,
                      int warmupJobs) {
        this.rule = rule;
        this.numWorkCenters = numWorkCenters;
        this.numJobsRecorded = numJobsRecorded;
        this.warmupJobs = warmupJobs;

        systemState = new SystemState();
        eventQueue = new PriorityQueue<>();
    }

    public AbstractRule getRule() {
        return rule;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public PriorityQueue<AbstractEvent> getEventQueue() {
        return eventQueue;
    }

    public void setRule(AbstractRule rule) {
        this.rule = rule;
    }

    public double getClockTime() {
        return systemState.getClockTime();
    }

    public void addEvent(AbstractEvent event) {
        eventQueue.add(event);
    }

    public void run() {
        while (!eventQueue.isEmpty() && throughput < numJobsRecorded) {
            AbstractEvent nextEvent = eventQueue.poll();

            systemState.setClockTime(nextEvent.getTime());
            nextEvent.trigger(this);
        }
    }

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
    public abstract void resetState();
    public abstract void reset();
    public abstract void rotateSeed();
    public abstract void generateJob();
    public abstract Simulation surrogate(int numWorkCenters, int numJobsRecorded,
                                int warmupJobs);
    public abstract Simulation surrogateBusy(int numWorkCenters, int numJobsRecorded,
                                         int warmupJobs);
}
