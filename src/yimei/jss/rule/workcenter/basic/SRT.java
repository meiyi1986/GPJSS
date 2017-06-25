package yimei.jss.rule.workcenter.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Shortest ready time.
 * Priority of this rule should be the ready time of the workCenter, which is the least ready time of
 * all machines in the workCenter.
 */
public class SRT extends AbstractRule {

    public SRT() {
        name = "\"SRT\"";
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getReadyTime();
    }
}
