package yimei.jss.rule.operation.basic;

import ec.rule.Rule;
import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.simulation.state.SystemState;

/**
 * The LPT (longest processing time) rule.
 * <p>
 * Created by YiMei on 4/10/16.
 */
public class LPT extends AbstractRule {

    public LPT(RuleType type) {
        name = "\"LPT\"";
        this.type = type;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return -op.getProcTime();
    }

}
