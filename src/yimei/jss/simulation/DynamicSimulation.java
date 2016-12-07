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
 * The dynamic simulation -- discrete event simulation
 *
 * Created by yimei on 22/09/16.
 */
public class DynamicSimulation extends Simulation {

    public final static int SEED_ROTATION = 10000;

    private long seed;
    private RandomDataGenerator randomDataGenerator;

    private final int minNumOps;
    private final int maxNumOps;
    private final double utilLevel;
    private final double dueDateFactor;
    private final boolean revisit;

    private AbstractIntegerSampler numOpsSampler;
    private AbstractRealSampler procTimeSampler;
    private AbstractRealSampler interArrivalTimeSampler;
    private AbstractRealSampler jobWeightSampler;

    private DynamicSimulation(long seed,
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
        super(rule, numWorkCenters, numJobsRecorded, warmupJobs);

        this.seed = seed;
        this.randomDataGenerator = new RandomDataGenerator();
        this.randomDataGenerator.reSeed(seed);

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

        // Create the work centers, with empty queue and ready to go initially.
        for (int i = 0; i < numWorkCenters; i++) {
            systemState.addWorkCenter(new WorkCenter(i));
        }

        setup();
    }

    public DynamicSimulation(long seed,
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

    @Override
    public void setup() {
        numJobsArrived = 0;
        throughput = 0;
        generateJob();
    }

    @Override
    public void resetState() {
        systemState.reset();
        eventQueue.clear();

        setup();
    }

    @Override
    public void reset() {
        reset(seed);
    }

    public void reset(long seed) {
        reseed(seed);
        resetState();
    }

    public void reseed(long seed) {
        this.seed = seed;
        randomDataGenerator.reSeed(seed);
    }

    @Override
    public void rotateSeed() {
        seed += SEED_ROTATION;
        reset();
    }

    @Override
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

    @Override
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

        Simulation surrogate = new DynamicSimulation(seed, rule, numWorkCenters,
                numJobsRecorded, warmupJobs, minNumOps, surrogateMaxNumOps,
                utilLevel, dueDateFactor, revisit, surrogateNumOpsSampler,
                procTimeSampler, surrogateInterArrivalTimeSampler, jobWeightSampler);

        return surrogate;
    }

    @Override
    public Simulation surrogateBusy(int numWorkCenters, int numJobsRecorded,
                                int warmupJobs) {
        double utilLevel = 1;
        int surrogateMaxNumOps = maxNumOps;
        AbstractIntegerSampler surrogateNumOpsSampler = numOpsSampler.clone();
        AbstractRealSampler surrogateInterArrivalTimeSampler = interArrivalTimeSampler.clone();
        if (surrogateMaxNumOps > numWorkCenters) {
            surrogateMaxNumOps = numWorkCenters;
            surrogateNumOpsSampler.setUpper(surrogateMaxNumOps);

            surrogateInterArrivalTimeSampler.setMean(interArrivalTimeMean(numWorkCenters,
                    minNumOps, surrogateMaxNumOps, utilLevel));
        }

        Simulation surrogate = new DynamicSimulation(seed, rule, numWorkCenters,
                numJobsRecorded, warmupJobs, minNumOps, surrogateMaxNumOps,
                utilLevel, dueDateFactor, revisit, surrogateNumOpsSampler,
                procTimeSampler, surrogateInterArrivalTimeSampler, jobWeightSampler);

        return surrogate;
    }

    public static DynamicSimulation standardFull(
            long seed,
            AbstractRule rule,
            int numWorkCenters,
            int numJobsRecorded,
            int warmupJobs,
            double utilLevel,
            double dueDateFactor) {
        return new DynamicSimulation(seed, rule, numWorkCenters, numJobsRecorded,
                warmupJobs, numWorkCenters, numWorkCenters, utilLevel,
                dueDateFactor, false);
    }

    public static DynamicSimulation standardMissing(
            long seed,
            AbstractRule rule,
            int numWorkCenters,
            int numJobsRecorded,
            int warmupJobs,
            double utilLevel,
            double dueDateFactor) {
        return new DynamicSimulation(seed, rule, numWorkCenters, numJobsRecorded,
                warmupJobs, 2, numWorkCenters, utilLevel, dueDateFactor, false);
    }
}
