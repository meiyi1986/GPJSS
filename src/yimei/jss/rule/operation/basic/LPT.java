package yimei.jss.rule.operation.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * The LPT (longest processing time) rule.
 * <p>
 * Created by YiMei on 4/10/16.
 */
public class LPT extends AbstractRule {

    public LPT() {
        name = "\"LPT\"";
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return -op.getProcTime();
    }

}