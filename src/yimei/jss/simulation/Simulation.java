package yimei.jss.simulation;

import org.apache.commons.math3.random.RandomDataGenerator;
import yimei.jss.jobshop.*;
import yimei.util.random.*;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.event.AbstractEvent;
import yimei.jss.simulation.event.JobArrivalEvent;
import yimei.jss.simulation.state.SystemState;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by yimei on 22/09/16.
 */
public class Simulation {

    public final static int SEED_ROTATION = 10000;

    private long seed;
    private RandomDataGenerator randomDataGenerator;

    private final int numWorkCenters;
    private final int numJobsRecorded;
    private final int warmupJobs;
    private final int minNumOps;
    private final int maxNumOps;
    private final double utilLevel;
    private final double dueDateFactor;
    private final boolean revisit;

    private AbstractIntegerSampler numOpsSampler;
    private AbstractRealSampler procTimeSampler;
    private AbstractRealSampler interArrivalTimeSampler;
    private AbstractRealSampler jobWeightSampler;

    private AbstractRule rule;

    private SystemState systemState;
    private PriorityQueue<AbstractEvent> eventQueue;
    private int numJobsArrived;
    private int throughput;

    private Simulation(long seed,
                       AbstractRule rule,
                       int numWorkCenters,
                       int numJobsRecorded,
                       int warmupJobs,
                       int minNumOps,
                       int maxNumOps,
                       double utilLevel,
                       double dueDateFactor,
                       boolean revisit,
                       AbstractIntegerSampler numOpsSampler,
                       AbstractRealSampler procTimeSampler,
                       AbstractRealSampler interArrivalTimeSampler,
                       AbstractRealSampler jobWeightSampler) {
        this.seed = seed;
        this.randomDataGenerator = new RandomDataGenerator();
        this.randomDataGenerator.reSeed(seed);
        this.rule = rule;

        this.numWorkCenters = numWorkCenters;
        this.numJobsRecorded = numJobsRecorded;
        this.warmupJobs = warmupJobs;
        this.minNumOps = minNumOps;
        this.maxNumOps = maxNumOps;
        this.utilLevel = utilLevel;
        this.dueDateFactor = dueDateFactor;
        this.revisit = revisit;

        this.numOpsSampler = numOpsSampler;
        this.procTimeSampler = procTimeSampler;
        this.interArrivalTimeSampler = interArrivalTimeSampler;
        this.jobWeightSampler = jobWeightSampler;

        setInterArrivalTimeSamplerMean();

        setup();
    }

    public Simulation(long seed,
                      AbstractRule rule,
                      int numWorkCenters,
                      int numJobsRecorded,
                      int warmupJobs,
                      int minNumOps,
                      int maxNumOps,
                      double utilLevel,
                      double dueDateFactor,
                      boolean revisit) {
        this(seed, rule, numWorkCenters, numJobsRecorded, warmupJobs,
                minNumOps, maxNumOps, utilLevel, dueDateFactor, revisit,
                new UniformIntegerSampler(minNumOps, maxNumOps),
                new UniformSampler(1, 99),
                new ExponentialSampler(),
                new TwoSixTwoSampler());
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public AbstractRule getRule() {
        return rule;
    }

    public PriorityQueue<AbstractEvent> getEventQueue() {
        return eventQueue;
    }

    public int getNumWorkCenters() {
        return numWorkCenters;
    }

    public int getNumJobsRecorded() {
        return numJobsRecorded;
    }

    public int getWarmupJobs() {
        return warmupJobs;
    }

    public int getMinNumOps() {
        return minNumOps;
    }

    public int getMaxNumOps() {
        return maxNumOps;
    }

    public double getUtilLevel() {
        return utilLevel;
    }

    public double getDueDateFactor() {
        return dueDateFactor;
    }

    public boolean isRevisit() {
        return revisit;
    }

    public RandomDataGenerator getRandomDataGenerator() {
        return randomDataGenerator;
    }

    public AbstractIntegerSampler getNumOpsSampler() {
        return numOpsSampler;
    }

    public AbstractRealSampler getProcTimeSampler() {
        return procTimeSampler;
    }

    public AbstractRealSampler getInterArrivalTimeSampler() {
        return interArrivalTimeSampler;
    }

    public AbstractRealSampler getJobWeightSampler() {
        return jobWeightSampler;
    }

    public double getClockTime() {
        return systemState.getClockTime();
    }

    public void setRule(AbstractRule rule) {
        this.rule = rule;
    }

    public void addEvent(AbstractEvent event) {
        eventQueue.add(event);
    }

    public void setup() {
        systemState = new SystemState();
        for (int i = 0; i < numWorkCenters; i++) {
            systemState.addWorkCenter(new WorkCenter(i));
        }

        eventQueue = new PriorityQueue<>();

        numJobsArrived = 0;
        throughput = 0;
        generateJob();
    }

    public void resetState() {
        systemState.reset();
        eventQueue.clear();

        numJobsArrived = 0;
        throughput = 0;
        generateJob();
    }

    public void reset(long seed) {
        reseed(seed);
        resetState();
    }

    public void reset() {
        reset(seed);
    }

    public void reseed(long seed) {
        this.seed = seed;
        randomDataGenerator.reSeed(seed);
    }

    public void rotateSeed() {
        seed += SEED_ROTATION;
        reset();
    }

    public void generateJob() {
        double arrivalTime = getClockTime()
                + interArrivalTimeSampler.next(randomDataGenerator);
        double weight = jobWeightSampler.next(randomDataGenerator);
        Job job = new Job(numJobsArrived, new ArrayList<>(),
                arrivalTime, arrivalTime, 0, weight);
        int numOps = numOpsSampler.next(randomDataGenerator);

        int[] route = randomDataGenerator.nextPermutation(numWorkCenters, numOps);

        double totalProcTime = 0.0;
        for (int i = 0; i < numOps; i++) {
            double procTime = procTimeSampler.next(randomDataGenerator);
            totalProcTime += procTime;

            Operation o = new Operation(job, i, procTime, systemState.getWorkCenter(route[i]));

            job.addOperation(o);
        }

        job.linkOperations();

        double dueDate = job.getReleaseTime() + dueDateFactor * totalProcTime;
        job.setDueDate(dueDate);

        systemState.addJobToSystem(job);
        numJobsArrived ++;

        eventQueue.add(new JobArrivalEvent(job));
    }

    public void completeJob(Job job) {
        if (numJobsArrived > warmupJobs && job.getId() < numJobsRecorded + warmupJobs) {
            throughput++;

            systemState.addCompletedJob(job);
        }
        systemState.removeJobFromSystem(job);
    }

    public double interArrivalTimeMean(int numWorkCenters,
                                             int minNumOps,
                                             int maxNumOps,
                                             double utilLevel) {
        double meanNumOps = 0.5 * (minNumOps + maxNumOps);
        double meanProcTime = procTimeSampler.getMean();

        return (meanNumOps * meanProcTime) / (utilLevel * numWorkCenters);
    }

    public void setInterArrivalTimeSamplerMean() {
        double mean = interArrivalTimeMean(numWorkCenters, minNumOps, maxNumOps, utilLevel);
        interArrivalTimeSampler.setMean(mean);
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

        while (!eventQueue.isEmpty() && throughput < numJobsRecorded) {
            AbstractEvent nextEvent = eventQueue.poll();

            systemState.setClockTime(nextEvent.getTime());
            nextEvent.trigger(this);
        }
    }

    public List<DecisionSituation> decisionSituations(int minQueueLength) {
        List<DecisionSituation> decisionSituations = new ArrayList<>();

        while (!eventQueue.isEmpty() && throughput < numJobsRecorded) {
            AbstractEvent nextEvent = eventQueue.poll();

            systemState.setClockTime(nextEvent.getTime());
            nextEvent.addDecisionSituation(this, decisionSituations, minQueueLength);
        }

        resetState();

        return decisionSituations;
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
        for (int i = 0; i < numWorkCenters; i++) {
            string += String.format("%.3f ", workCenterUtilLevel(i));
        }
        string += "]";

        return string;
    }

    public Simulation surrogate(int numWorkCenters, int numJobsRecorded,
                                int warmupJobs) {
        int surrogateMaxNumOps = maxNumOps;
        AbstractIntegerSampler surrogateNumOpsSampler = numOpsSampler.clone();
        AbstractRealSampler surrogateInterArrivalTimeSampler = interArrivalTimeSampler.clone();
        if (surrogateMaxNumOps > numWorkCenters) {
            surrogateMaxNumOps = numWorkCenters;
            surrogateNumOpsSampler.setUpper(surrogateMaxNumOps);

            surrogateInterArrivalTimeSampler.setMean(interArrivalTimeMean(numWorkCenters,
                    minNumOps, surrogateMaxNumOps, utilLevel));
        }

        Simulation surrogate = new Simulation(seed, rule, numWorkCenters,
                numJobsRecorded, warmupJobs, minNumOps, surrogateMaxNumOps,
                utilLevel, dueDateFactor, revisit, surrogateNumOpsSampler,
                procTimeSampler, surrogateInterArrivalTimeSampler, jobWeightSampler);

        return surrogate;
    }

    public static Simulation standardFull(
            long seed,
            AbstractRule rule,
            int numWorkCenters,
            int numJobsRecorded,
            int warmupJobs,
            double utilLevel,
            double dueDateFactor) {
        return new Simulation(seed, rule, numWorkCenters, numJobsRecorded,
                warmupJobs, numWorkCenters, numWorkCenters, utilLevel,
                dueDateFactor, false);
    }

    public static Simulation standardMissing(
            long seed,
            AbstractRule rule,
            int numWorkCenters,
            int numJobsRecorded,
            int warmupJobs,
            double utilLevel,
            double dueDateFactor) {
        return new Simulation(seed, rule, numWorkCenters, numJobsRecorded,
                warmupJobs, 2, numWorkCenters, utilLevel, dueDateFactor, false);
    }
}
