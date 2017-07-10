package yimei.jss.jobshop;

import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.operation.basic.FCFS;
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
    private AbstractRule routingRule;
    private Operation next;

    public Operation(Job job, int id, AbstractRule routingRule) {
        this.job = job;
        this.id = id;
        this.routingRule = routingRule;
        this.operationOptions = new ArrayList<>();
    }

    public Operation(Job job, int id, double procTime, WorkCenter workCenter, AbstractRule routingRule) {
        this.job = job;
        this.id = id;
        this.operationOptions = new ArrayList<>();
        this.routingRule = routingRule;
        this.next = null;
        operationOptions.add(new OperationOption(this,
                operationOptions.size()+1,procTime,workCenter));
    }

    public String toString() {
        String msg = "";
        for (OperationOption option: operationOptions) {
            msg += String.format("[O%d-%d, W%d, T%.1f], ",
                    id, option.getOptionId(), option.getWorkCenter().getId(), option.getProcTime());
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
    public OperationOption getOperationOption(SystemState systemState) {
        if (operationOptions.size() == 1) {
            return operationOptions.iterator().next();
        }

        AbstractRule routingRule = this.routingRule;
        if (routingRule == null) {
            routingRule = new FCFS();
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
}