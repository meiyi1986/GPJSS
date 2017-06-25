package yimei.jss.rule.operation.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class LCFS extends AbstractRule {

    public LCFS() {
        name = "\"LCFS\"";
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return -op.getReadyTime();
    }
}
