package yimei.jss.rule.basic;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * The LPT (longest processing time) rule.
 *
 * Created by YiMei on 4/10/16.
 */
public class LPT extends AbstractRule {

    public LPT() {
        name = "\"LPT\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        return -op.getProcTime();
    }

}
