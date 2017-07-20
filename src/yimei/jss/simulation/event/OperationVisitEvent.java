package yimei.jss.simulation.event;

import yimei.jss.jobshop.*;
import yimei.jss.jobshop.Process;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;

import java.util.List;

/**
 * Created by YiMei on 25/09/16.
 */
public class OperationVisitEvent extends AbstractEvent {

    private OperationOption operation;

    public OperationVisitEvent(double time, OperationOption operation) {
        super(time);
        this.operation = operation;
    }

    public OperationVisitEvent(OperationOption operation) {
        this(operation.getReadyTime(), operation);
    }

    @Override
    public void trigger(Simulation simulation) {
        operation.setReadyTime(time);

        WorkCenter workCenter = operation.getWorkCenter();
        Machine earliestMachine = workCenter.earliestReadyMachine();
        Process p = new Process(workCenter, earliestMachine.getId(), operation, time);

        if (earliestMachine.getReadyTime() > time || !simulation.canAddToQueue(p)) {
            workCenter.addToQueue(operation);
        }
        else {
            //we add a process start event here because at this point in time, the workcenter
            //ready time is less than or equal to the start time of the process
            //we expect to start this in a minute

            //job 7 - operation 1 - option 0
            //work center 2 - machine 0 - ready time = 8
            //time = 8

            //what is happening presumably is that the queue already contains a ProcessStartEvent at this
            //exact time for the same work center
            //the problem with this is that by the time this process is pulled off the queue, the
            //work center ready time will have gone up, and this process will reset it, which is
            //leading to impossibly low makespans

            //this is going to be a problem whenever there are multiple process start events for the
            //same work center in the queue
            //we can either be very careful about which events we add to the queue, or update all events
            //in the queue relating to a certain workcenter once a ProcessFinishEvent has occurred
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
                time, operation.getJob().getId(), operation.getOperation().getId());
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
