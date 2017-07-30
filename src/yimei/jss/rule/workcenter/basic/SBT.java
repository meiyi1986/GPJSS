package yimei.jss.rule.workcenter.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Shortest busy time.
 * This rule should have a priority of the busy time of the workCenter.
 */
public class SBT extends AbstractRule {
    private RuleType type;

    public SBT(RuleType t) {
        name = "\"SBT\"";
        this.type = t;
    }

    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getBusyTime();
    }
}
