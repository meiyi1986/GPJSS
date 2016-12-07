package yimei.jss.rule.composite;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by yimei on 5/12/16.
 */
public class SlackperOPN extends AbstractRule {

    public SlackperOPN() {
        name = "\"Slack/OPN\"";
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        double slack = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();

        if (slack > 0) {
            return slack / op.getNumOpsRemaining();
        }
        else {
            return slack * op.getNumOpsRemaining();
        }
    }
}
