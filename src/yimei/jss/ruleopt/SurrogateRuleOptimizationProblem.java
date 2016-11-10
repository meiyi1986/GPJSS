//package yimei.jss.ruleopt;
//
//import ec.EvolutionState;
//import ec.util.Parameter;
//import yimei.jss.jobshop.SchedulingSet;
//import yimei.jss.ruleopt.RuleOptimizationProblem;
//
///**
// * The surrogate rule optimization problem.
// *
// * There are two sets of instances:
// *      (1) The original set;
// *      (2) The surrogate set.
// * The evaluation may be on either set depending on situation.
// *
// * Created by YiMei on 4/10/16.
// */
//public class SurrogateRuleOptimizationProblem extends RuleOptimizationProblem {
//    private SchedulingSet originalSet;
//    private SchedulingSet surrogateSet;
//
//    @Override
//    public void setup(final EvolutionState state, final Parameter base) {
//        super.setup(state, base);
//
//        originalSet = trainSet;
//        surrogateSet = originalSet.surrogate(5, 500, 100, objectives);
//
//        trainSet = surrogateSet;
//    }
//
//    public void setOriginalSet() {
//        trainSet = originalSet;
//    }
//
//    public void setSurrogateSet() {
//        trainSet = surrogateSet;
//    }
//}
