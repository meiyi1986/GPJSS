package yimei.jss.rule.operation.composite;

import ec.rule.Rule;
import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class PTplusPWplusFDD extends AbstractRule {

    public PTplusPWplusFDD(RuleType t) {
        name = "\"SPT+PW+FDD\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getProcTime() - op.getReadyTime() + op.getFlowDueDate();
    }
}
