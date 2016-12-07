package yimei.jss.rule.composite;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class LWKRplusPT extends AbstractRule {

    public LWKRplusPT() {
        name = "\"LWKR+SPT\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        return op.getWorkRemaining() + op.getProcTime();
    }
}
