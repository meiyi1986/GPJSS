package yimei.jss.jobshop;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yimei on 22/09/16.
 */
public class Operation {
    private final Job job;
    private final int id;
    private Set<OperationOption> operationOptionSet;
    private OperationOption chosenOperationOption;

    public Operation(Job job, int id) {
        this.job = job;
        this.id = id;
        this.operationOptionSet = new HashSet<>();
    }

    public Operation(Job job, int id, double procTime, WorkCenter workCenter) {
        this.job = job;
        this.id = id;
        this.operationOptionSet = new HashSet<>();
        operationOptionSet.add(new OperationOption(this,
                operationOptionSet.size()+1,procTime,workCenter));
    }

    public Job getJob() {
        return job;
    }

    public int getId() {
        return id;
    }

    public void addOperationOption(OperationOption option) {
        operationOptionSet.add(option);
    }

    public Set<OperationOption> getOperationSet() {
        return operationOptionSet;
    }

    public OperationOption getChosenOperationOption() {
        return chosenOperationOption;
    }

    /*
    This method should employ a heuristic to select which work center
    this operation should use. Should only be called once all options
    have been added to Operation.
     */
    public void chooseOperationOption() {
        //TODO: For now basic heuristic - will update later
        if (chosenOperationOption == null) {
            if (operationOptionSet.size() > 1) {
                OperationOption bestOption = null;
                double leastProcTime = Double.MAX_VALUE;
                for (OperationOption op: operationOptionSet) {
                    if (op.getProcTime() < leastProcTime) {
                        leastProcTime = op.getProcTime();
                        bestOption = op;
                    }
                }
                this.chosenOperationOption = bestOption;
            }
            else if (operationOptionSet.size() == 1) {
                this.chosenOperationOption = operationOptionSet.iterator().next();
            }
        }
    }
}