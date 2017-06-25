package yimei.jss.rule.workcenter.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Longest ready time.
 * This rule should have a priority of the negative of the workCenter's ready time.
 */
public class LRT extends AbstractRule {

    public LRT() {
        name = "\"LRT\"";
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return - workCenter.getReadyTime();
    }
}
