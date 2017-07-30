package yimei.jss.rule.operation.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.simulation.state.SystemState;

/**
 * The SPT (shortest processing time) rule.
 * <p>
 * Created by YiMei on 27/09/16.
 */
public class SPT extends AbstractRule {

    public SPT(RuleType type) {
        name = "\"SPT\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return op.getProcTime();
    }

}
