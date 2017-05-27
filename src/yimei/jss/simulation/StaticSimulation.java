package yimei.jss.simulation;

import yimei.jss.jobshop.*;
import yimei.jss.jobshop.Process;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.event.JobArrivalEvent;
import yimei.jss.simulation.event.ProcessFinishEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The simulation based on static job shop instance.
 *
 * Created by yimei on 21/11/16.
 */
public class StaticSimulation extends Simulation {

    private JSSInstance instance;
    private Shop shop;
    private List<Process> dummyProcesses;
    private AbstractRule routingRule;

    public StaticSimulation(AbstractRule sequencingRule, AbstractRule routingRule, JSSInstance instance) {
        super(sequencingRule, routingRule, instance.getNumWorkCenters(), instance.getNumJobs(), 0);

        this.instance = instance;
        this.routingRule = routingRule;

        // Create the shop
        shop = instance.createShop();

        // Create the dummy processes to fill the initial ready time
        dummyProcesses = new ArrayList<>();
        for (int i = 0; i < shop.getWorkCenters().size(); i++) {
            dummyProcesses.add(createDummyProcess(shop.getWorkCenter(i),
                    instance.getWorkCenterReadyTimes().get(i)));
        }

        // Create the work centers of the system state
        systemState.setWorkCenters(shop.getWorkCenters());

        numJobsArrived = instance.getNumJobs();

        setup();
    }

    @Override
    public void setup() {
        for (int i = 0; i < systemState.getWorkCenters().size(); i++) {
            eventQueue.add(new ProcessFinishEvent(dummyProcesses.get(i)));
        }

        for (Job job : shop.getJobs()) {
            systemState.addJobToSystem(job);

            if (job.getArrivalTime() >
                    job.getOperation(0).getOperationOption(systemState).getWorkCenter().getReadyTime()) {
                eventQueue.add(new JobArrivalEvent(job));
            }
            else {
                job.getOperation(0).getOperationOption(systemState).getWorkCenter().addToQueue(
                        job.getOperation(0).getOperationOption(systemState));
            }
        }

        throughput = 0;
    }

    @Override
    public void resetState() {
        systemState.reset();
        eventQueue.clear();
        instance.resetShop(shop);

        setup();
    }

    @Override
    public void reset() {
        resetState();
    }

    @Override
    public void rotateSeed() {
        // Do nothing, since it is static.
    }

    @Override
    public void generateJob() {
        // Since it is static, nothing is done here.
    }

    @Override
    public Simulation surrogate(int numWorkCenters,
                                int numJobsRecorded,
                                int warmupJobs) {
        return this;
    }

    @Override
    public Simulation surrogateBusy(int numWorkCenters,
                                int numJobsRecorded,
                                int warmupJobs) {
        return this;
    }

    public Process createDummyProcess(WorkCenter workCenter, double readyTime) {
        Job job = new Job(-1-workCenter.getId(), new ArrayList<>());
        Operation op = new Operation(job, 0, readyTime, workCenter, routingRule);
        Process process = new Process(workCenter, 0, op.getOperationOption(systemState), 0);

        return process;
    }
}
