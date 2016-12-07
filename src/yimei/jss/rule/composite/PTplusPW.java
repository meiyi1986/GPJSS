package yimei.jss.rule.composite;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class PTplusPW extends AbstractRule {

    public PTplusPW() {
        name = "\"SPT+PW\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        return op.getProcTime() - op.getReadyTime();
    }
}
