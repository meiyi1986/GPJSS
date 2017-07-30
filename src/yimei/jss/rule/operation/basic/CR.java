package yimei.jss.rule.operation.basic;

import yimei.jss.rule.AbstractRule;
import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.RuleType;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by yimei on 5/12/16.
 */
public class CR extends AbstractRule {

    public CR(RuleType type) {
        name = "\"CR\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return (op.getJob().getDueDate() - systemState.getClockTime()) / op.getWorkRemaining();
    }
}
