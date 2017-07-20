package yimei.jss.simulation;

import yimei.jss.jobshop.*;
import yimei.jss.jobshop.Process;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.event.AbstractEvent;
import yimei.jss.simulation.event.JobArrivalEvent;
import yimei.jss.simulation.event.ProcessFinishEvent;
import yimei.jss.simulation.state.SystemState;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The simulation based on static job shop instance.
 *
 * Created by yimei on 21/11/16.
 */
public class StaticSimulation extends Simulation {

    private JSSInstance instance;
    private Shop shop;
    private List<Process> dummyProcesses;

    public StaticSimulation(AbstractRule sequencingRule, AbstractRule routingRule, JSSInstance instance) {
        super(sequencingRule, routingRule, instance.getNumWorkCenters(), instance.getNumJobs(), 0);

        this.instance = instance;

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

            //this first operation option will always be the same
            OperationOption firstOption = job.getOperation(0).getOperationOption(systemState);

            //this is a static simulation, no jobs are arriving after t = 0
            firstOption.getWorkCenter().addToQueue(firstOption);
        }

        throughput = 0;
    }

    @Override
    public void resetState() {
        systemState.reset();
        eventQueue.clear();
        instance.resetShop(shop);

        // Create the shop
        shop = instance.createShop();

        // Create the dummy processes to fill the initial ready time
        dummyProcesses = new ArrayList<>();
        for (int i = 0; i < shop.getWorkCenters().size(); i++) {
            dummyProcesses.add(createDummyProcess(shop.getWorkCenter(i),
                    instance.getWorkCenterReadyTimes().get(i)));
        }

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

    public String toString() {
        String str = "Shop: \n";
        str += shop.toString();

        return str;
    }
}
