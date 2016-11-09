package yimei.jss.rule.composite;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class TwoPTplusWINQplusNPT extends AbstractRule {

    public TwoPTplusWINQplusNPT() {
        name = "\"2PT+WINQ+NPT\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        return 2 * op.getProcTime() + systemState.workInNextQueue(op)
                + op.getNextProcTime();
    }
}
