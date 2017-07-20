package yimei.jss.simulation.event;

import yimei.jss.jobshop.Process;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;

import java.util.List;

/**
 * Created by YiMei on 25/09/16.
 */
public class ProcessStartEvent extends AbstractEvent {

    private Process process;

    public ProcessStartEvent(double time, Process process) {
        super(time);
        this.process = process;
    }

    public ProcessStartEvent(Process process) {
        this(process.getStartTime(), process);
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void trigger(Simulation simulation) {
        WorkCenter workCenter = process.getWorkCenter();
        if (workCenter.getReadyTime() > process.getStartTime()) {
            int a = 0;


            //well it is good to know the work center ready time is somewhat reliable,
            //or at the least, being updated
            //but it is not being checked!
            //where would it be checked?
            //well we want to set the start time of a process to occur at least when the machine is
            //ready right? Is that it?
        }
        workCenter.setMachineReadyTime(
                process.getMachineId(), process.getFinishTime());
        workCenter.incrementBusyTime(process.getDuration());

        simulation.addEvent(
                new ProcessFinishEvent(process.getFinishTime(), process));
    }

    @Override
    public void addDecisionSituation(DynamicSimulation simulation,
                                     List<DecisionSituation> situations,
                                     int minQueueLength) {
        trigger(simulation);
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d op %d started on work center %d.\n",
                time,
                process.getOperation().getJob().getId(),
                process.getOperation().getOperation().getId(),
                process.getWorkCenter().getId());
    }

    @Override
    public int compareTo(AbstractEvent other) {
        if (time < other.time)
            return -1;

        if (time > other.time)
            return 1;

        if (other instanceof ProcessStartEvent)
            return 0;

        if (other instanceof ProcessFinishEvent)
            return -1;

        return 1;
    }
}
