package yimei.jss.algorithm.surrogategp;

import yimei.jss.rule.RuleType;
import yimei.jss.rule.operation.basic.FCFS;
import yimei.jss.rule.workcenter.basic.SBT;
import yimei.jss.simulation.DynamicSimulation;

/**
 * The generator to generate challenging static instances for training.
 * A challenging static instance includes more hard decision situations
 * where the queue size is long
 * Created by yimei on 5/12/16.
 */
public class StaticInstanceGenerator {

    DynamicSimulation simulation = DynamicSimulation.standardMissing(72334,
            new SBT(RuleType.SEQUENCING), new SBT(RuleType.ROUTING), 10, 500000, 0, 0.95, 4.0);


}
