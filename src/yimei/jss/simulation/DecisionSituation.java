package yimei.jss.simulation;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;

import java.util.ArrayList;
import java.util.List;

/**
 * A decision situation.
 *
 * Created by YiMei on 3/10/16.
 */
public class DecisionSituation {

    private List<OperationOption> queue;
    private WorkCenter workCenter;
    private SystemState systemState;

    public DecisionSituation(List<OperationOption> queue,
                             WorkCenter workCenter,
                             SystemState systemState) {
        this.queue = queue;
        this.workCenter = workCenter;
        this.systemState = systemState;
    }

    public List<OperationOption> getQueue() {
        return queue;
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public DecisionSituation clone() {
        List<OperationOption> clonedQ = new ArrayList<>(queue);
        WorkCenter clonedWC = workCenter.clone();
        SystemState clonedState = systemState.clone();

        return new DecisionSituation(clonedQ, clonedWC, clonedState);
    }
}
