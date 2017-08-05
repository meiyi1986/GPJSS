package yimei.jss.simulation.event;

import yimei.jss.jobshop.*;
import yimei.jss.jobshop.Process;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.Simulation;

import java.util.List;

/**
 * Created by yimei on 22/09/16.
 */
public class ProcessFinishEvent extends AbstractEvent {

    private Process process;

    public ProcessFinishEvent(double time, Process process) {
        super(time);
        this.process = process;
    }

    public ProcessFinishEvent(Process process) {
        this(process.getFinishTime(), process);
    }

    @Override
    public void trigger(Simulation simulation) {
        WorkCenter workCenter = process.getWorkCenter();
//        process.getOperationOption().getJob().addProcessFinishEvent(this);
//        if (process.getOperationOption().getJob().getId() >= 0) {
//            int[] jobStates = simulation.getJobStates();
//            int jobState = jobStates[process.getOperationOption().getJob().getId()];
//            if (process.getOperationOption().getOperation().getId() != (jobState+1)) {
//                //shouldn't happen
//                System.out.println("Hmmm");
//            }
//            jobStates[process.getOperationOption().getJob().getId()] = process.getOperationOption().getOperation().getId();
//            simulation.setJobStates(jobStates);
//        }

        if (!workCenter.getQueue().isEmpty()) {
            DecisionSituation decisionSituation =
                    new DecisionSituation(workCenter.getQueue(), workCenter,
                            simulation.getSystemState());

            OperationOption dispatchedOp =
                    simulation.getSequencingRule().priorOperation(decisionSituation);

            workCenter.removeFromQueue(dispatchedOp);

            //must wait for machine to be ready
            double processStartTime = Math.max(workCenter.getReadyTime(), time);

            Process nextP = new Process(workCenter, process.getMachineId(),
                    dispatchedOp, processStartTime);
            simulation.addEvent(new ProcessStartEvent(nextP));
        }

        OperationOption nextOp = process.getOperationOption().getNext(simulation.getSystemState(),simulation.getRoutingRule());

        if (nextOp == null) {
            Job job = process.getOperationOption().getJob();
            job.setCompletionTime(process.getFinishTime());
            simulation.completeJob(job);
        }
        else {
            simulation.addEvent(new OperationVisitEvent(time, nextOp));
        }
    }

    @Override
    public void addDecisionSituation(DynamicSimulation simulation,
                                     List<DecisionSituation> situations,
                                     int minQueueLength) {
        WorkCenter workCenter = process.getWorkCenter();

        if (!workCenter.getQueue().isEmpty()) {
            DecisionSituation decisionSituation =
                    new DecisionSituation(workCenter.getQueue(), workCenter,
                            simulation.getSystemState());

            if (workCenter.getQueue().size() >= minQueueLength) {
                situations.add(decisionSituation.clone());
            }

            OperationOption dispatchedOp =
                    simulation.getSequencingRule().priorOperation(decisionSituation);

            workCenter.removeFromQueue(dispatchedOp);
            Process nextP = new Process(workCenter, process.getMachineId(),
                    dispatchedOp, time);
            simulation.addEvent(new ProcessStartEvent(nextP));
        }

        OperationOption nextOp = process.getOperationOption().getNext(simulation.getSystemState(),simulation.getRoutingRule());
        if (nextOp == null) {
            Job job = process.getOperationOption().getJob();
            job.setCompletionTime(process.getFinishTime());
            simulation.completeJob(job);
        }
        else {
            simulation.addEvent(new OperationVisitEvent(time, nextOp));
        }
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d op %d finished on work center %d.\n",
                time,
                process.getOperationOption().getJob().getId(),
                process.getOperationOption().getOperation().getId(),
                process.getWorkCenter().getId());
    }

    @Override
    public int compareTo(AbstractEvent other) {
        if (time < other.time)
            return -1;

        if (time > other.time)
            return 1;

        if (other instanceof ProcessFinishEvent) {
            ProcessFinishEvent otherPFE = (ProcessFinishEvent)other;

            if (process.getWorkCenter().getId() < otherPFE.process.getWorkCenter().getId())
                return -1;

            if (process.getWorkCenter().getId() > otherPFE.process.getWorkCenter().getId())
            return 1;
        }

        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessFinishEvent that = (ProcessFinishEvent) o;

        return process != null ? process.equals(that.process) : that.process == null;
    }

    @Override
    public int hashCode() {
        return process != null ? process.hashCode() : 0;
    }


    public Process getProcess() {
        return process;
    }
}
