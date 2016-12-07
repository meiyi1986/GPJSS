package yimei.jss.algorithm.surrogategp;

import yimei.jss.rule.basic.FCFS;
import yimei.jss.simulation.DynamicSimulation;

/**
 * The generator to generate challenging static instances for training.
 * A challenging static instance includes more hard decision situations
 * where the queue size is long
 * Created by yimei on 5/12/16.
 */
public class StaticInstanceGenerator {

    DynamicSimulation simulation = DynamicSimulation.standardMissing(72334,
            new FCFS(), 10, 500000, 0, 0.95, 4.0);


}
