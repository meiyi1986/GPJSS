package yimei.jss.rule.basic;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * The SPT (shortest processing time) rule.
 *
 * Created by YiMei on 27/09/16.
 */
public class SPT extends AbstractRule {

    public SPT() {
        name = "\"SPT\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        return op.getProcTime();
    }

}
