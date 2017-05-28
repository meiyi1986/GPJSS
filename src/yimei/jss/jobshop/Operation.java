package yimei.jss.jobshop;

import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.basic.FCFS;
import yimei.jss.simulation.state.SystemState;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yimei on 22/09/16.
 */
public class Operation {
    private final Job job;
    private final int id;
    private Set<OperationOption> operationOptionSet;
    private AbstractRule rule;
    private Operation next;

    public Operation(Job job, int id, AbstractRule rule) {
        this.job = job;
        this.id = id;
        this.rule = rule;
        this.operationOptionSet = new HashSet<>();
    }

    public Operation(Job job, int id, double procTime, WorkCenter workCenter, AbstractRule rule) {
        this.job = job;
        this.id = id;
        this.operationOptionSet = new HashSet<>();
        this.rule = rule;
        this.next = null;
        operationOptionSet.add(new OperationOption(this,
                operationOptionSet.size()+1,procTime,workCenter));
    }

    public String toString() {
        String msg = "";
        for (OperationOption option: operationOptionSet) {
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

    public Set<OperationOption> getOperationOptionSet() { return operationOptionSet; }

    public void addOperationOption(OperationOption option) {
        operationOptionSet.add(option);
    }

    /*
    This method is to be called before a simulation has begun and additional information
    has been made availble. It will simply return the OperationOption with the highest
    procedure time, aka the most pessimistic procedure time guess.
     */
    public OperationOption getOperationOption() {
        double highestProcTime = Double.NEGATIVE_INFINITY;
        OperationOption best = null;
        for (OperationOption option: operationOptionSet) {
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
        if (operationOptionSet.size() == 1) {
            return operationOptionSet.iterator().next();
        }

        //TODO: Check assumption - lowest priority value is best
        AbstractRule rule = this.rule;
        if (rule == null) {
            rule = new FCFS();
        }

        double lowestPriority = Double.POSITIVE_INFINITY;
        OperationOption best = null;
        for (OperationOption option: operationOptionSet) {
            double priority = rule.priority(option, option.getWorkCenter(), systemState);
            if (priority < lowestPriority || lowestPriority == Double.POSITIVE_INFINITY) {
                lowestPriority = priority;
                best = option;
            }
        }
        return best;
    }
}