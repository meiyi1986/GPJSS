package yimei.jss.rule.basic;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by yimei on 5/12/16.
 */
public class Slack extends AbstractRule {

    public Slack() {
        name = "\"Slack\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        return op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();
    }
}
