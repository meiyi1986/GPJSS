//package yimei.jss.rule.operation.basic;
//
//import yimei.jss.jobshop.OperationOption;
//import yimei.jss.jobshop.WorkCenter;
//import yimei.jss.rule.AbstractRule;
//import yimei.jss.rule.RuleType;
//import yimei.jss.simulation.state.SystemState;
//
///**
// * Created by yimei on 5/12/16.
// */
//public class WINQ extends AbstractRule {
//
//    public WINQ(RuleType type) {
//        name = "\"WINQ\"";
//        this.type = type;
//    }
//
//    @Override
//    public double priority(OperationOption op, WorkCenter workCenter, SystemState systemState) {
//        return systemState.workInNextQueue(op);
//    }
//}
