package yimei.jss.rule.operation.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class FCFS extends AbstractRule {

    public FCFS(RuleType type) {
        name = "\"FCFS\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getReadyTime();
    }
}
