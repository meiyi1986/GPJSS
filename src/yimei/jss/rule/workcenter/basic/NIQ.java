package yimei.jss.rule.workcenter.basic;

import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by dyska on 6/06/17.
 * Number in queue.
 * This (routing) rule should return as the priority the number of operations in the queue of the workCenter
 */
public class NIQ extends AbstractRule {

    public NIQ() {
        name = "\"NIQ\"";
    }


    @Override
    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
        return workCenter.getNumMachines();
    }
}
