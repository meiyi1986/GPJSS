package yimei.jss.gp;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleProblemForm;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class CalcPriorityProblem extends Problem implements SimpleProblemForm {

    private Operation operation;
    private WorkCenter workCenter;
    private SystemState systemState;

    public CalcPriorityProblem(Operation operation,
                               WorkCenter workCenter,
                               SystemState systemState) {
        this.operation = operation;
        this.workCenter = workCenter;
        this.systemState = systemState;
    }

    public Operation getOperation() {
        return operation;
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    @Override
    public void evaluate(EvolutionState state, Individual ind,
                         int subpopulation, int threadnum) {
    }
}
