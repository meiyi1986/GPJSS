//package yimei.jss.rule.operation.composite;
//
//import yimei.jss.jobshop.OperationOption;
//import yimei.jss.jobshop.WorkCenter;
//import yimei.jss.rule.AbstractRule;
//import yimei.jss.rule.RuleType;
//import yimei.jss.simulation.state.SystemState;
//
///**
// * Created by YiMei on 27/09/16.
// */
//public class TwoPTplusWINQplusNPT extends AbstractRule {
//
//    public TwoPTplusWINQplusNPT(RuleType t) {
//        name = "\"2PT+WINQ+NPT\"";
//        this.type = t;
//    }
//
//    @Override
//    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
//        return 2 * op.getProcTime() + systemState.workInNextQueue(op)
//                + op.getNextProcTime();
//    }
//}
