package yimei.jss.jobshop;

import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.rule.workcenter.basic.SBT;
import yimei.jss.simulation.state.SystemState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yimei on 22/09/16.
 */
public class Operation {
    private final Job job;
    private final int id;
    private List<OperationOption> operationOptions;
    private Operation next;

    public Operation(Job job, int id) {
        this.job = job;
        this.id = id;
        this.operationOptions = new ArrayList<>();
    }

    public Operation(Job job, int id, double procTime, WorkCenter workCenter) {
        this.job = job;
        this.id = id;
        this.operationOptions = new ArrayList<>();
        this.next = null;
        operationOptions.add(new OperationOption(this,
                operationOptions.size()+1,procTime,workCenter));
    }

    public String toString() {
        String msg = "";
        for (OperationOption option: operationOptions) {
            msg += String.format("[J%d O%d-%d, W%d, T%.1f], ",
                    job.getId(), id, option.getOptionId(), option.getWorkCenter().getId(), option.getProcTime());
        }
        msg = msg.substring(0, msg.length()-2);
        msg += "\n";
        return msg;
    }

    public Job getJob() {
        return job;
    }

    public int getId() {
        return id;
    }

    public void setNext(Operation next) {this.next = next; }

    public void setOperationOptions (List<OperationOption> operationOptions) {this.operationOptions = operationOptions; }

    public Operation getNext() { return next; }

    public List<OperationOption> getOperationOptions() { return operationOptions; }

    public void addOperationOption(OperationOption option) {
        operationOptions.add(option);
    }

    /*
    This method is to be called before a simulation has begun and additional information
    has been made availble. It will simply return the OperationOption with the highest
    procedure time, aka the most pessimistic procedure time guess.
     */
    public OperationOption getOperationOption() {
        double highestProcTime = Double.NEGATIVE_INFINITY;
        OperationOption best = null;
        for (OperationOption option: operationOptions) {
            if (option.getProcTime() > highestProcTime || highestProcTime == Double.NEGATIVE_INFINITY) {
                highestProcTime = option.getProcTime();
                best = option;
            }
        }
        return best;
    }

    /*
    This method is to be called with the system state parameter. It allows for an
    informed decision in choosing which operation option to use.
     */
    public OperationOption getOperationOption(SystemState systemState, AbstractRule routingRule) {
        if (operationOptions.size() == 1) {
            return operationOptions.iterator().next();
        }

        if (routingRule == null) {
            routingRule = new SBT(RuleType.ROUTING);
        }

        double lowestPriority = Double.POSITIVE_INFINITY;
        OperationOption best = null;
        for (OperationOption option: operationOptions) {
            double priority = routingRule.priority(option, option.getWorkCenter(), systemState);
            if (priority < lowestPriority || lowestPriority == Double.POSITIVE_INFINITY) {
                lowestPriority = priority;
                best = option;
            }
        }
        return best;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation operation = (Operation) o;

        if (id != operation.id) return false;
        if (job != null ? !job.equals(operation.job) : operation.job != null) return false;
        if (operationOptions != null ? !operationOptions.equals(operation.operationOptions) : operation.operationOptions != null)
            return false;
        return next != null ? next.equals(operation.next) : operation.next == null;
    }

    @Override
    public int hashCode() {
        int result = job != null ? job.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + (operationOptions != null ? operationOptions.hashCode() : 0);
        result = 31 * result + (next != null ? next.hashCode() : 0);
        return result;
    }
}