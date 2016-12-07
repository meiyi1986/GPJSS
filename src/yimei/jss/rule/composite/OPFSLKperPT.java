package yimei.jss.rule.composite;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 27/09/16.
 */
public class OPFSLKperPT extends AbstractRule {

    public OPFSLKperPT() {
        name = "\"OPFSLK/PT\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        double value = (systemState.getClockTime() + op.getProcTime()
                - op.getFlowDueDate()) / op.getProcTime();

        if (value < 0)
            value = 0;

        return value;
    }
}
