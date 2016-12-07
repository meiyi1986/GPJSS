package yimei.jss.simulation.event;

import yimei.jss.jobshop.Machine;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.jobshop.Process;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;

import java.util.List;

/**
 * Created by YiMei on 25/09/16.
 */
public class OperationVisitEvent extends AbstractEvent {

    private Operation operation;

    public OperationVisitEvent(double time, Operation operation) {
        super(time);
        this.operation = operation;
    }

    public OperationVisitEvent(Operation operation) {
        this(operation.getReadyTime(), operation);
    }

    @Override
    public void trigger(Simulation simulation) {
        operation.setReadyTime(time);

        WorkCenter workCenter = operation.getWorkCenter();
        Machine earliestMachine = workCenter.earliestReadyMachine();

        if (earliestMachine.getReadyTime() > time) {
            workCenter.addToQueue(operation);
        }
        else {
            Process p = new Process(workCenter, earliestMachine.getId(), operation, time);
            simulation.addEvent(new ProcessStartEvent(p));
        }
    }

    @Override
    public void addDecisionSituation(DynamicSimulation simulation,
                                     List<DecisionSituation> situations,
                                     int minQueueLength) {
        trigger(simulation);
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d op %d visits.\n",
                time, operation.getJob().getId(), operation.getId());
    }

    @Override
    public int compareTo(AbstractEvent other) {
        if (time < other.time)
            return -1;

        if (time > other.time)
            return 1;

        if (other instanceof JobArrivalEvent)
            return 1;

        if (other instanceof OperationVisitEvent)
            return 0;

        return -1;
    }
}
