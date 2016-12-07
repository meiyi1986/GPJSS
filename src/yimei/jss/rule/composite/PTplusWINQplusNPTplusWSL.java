package yimei.jss.rule.composite;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class PTplusWINQplusNPTplusWSL extends AbstractRule {

    public PTplusWINQplusNPTplusWSL() {
        name = "\"PT+WINQ+NPT+WSL\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {

        return op.getProcTime() + systemState.workInNextQueue(op)
                + op.getNextProcTime();
    }
}
