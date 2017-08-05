package yimei.jss.ruleevaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import org.apache.commons.math3.analysis.function.Abs;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;
import yimei.jss.surrogate.Surrogate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The HalfShop surrogate evaluation model proposed in
 * Nguyen, S., Zhang, M., Tan, K.C., 2016.
 * Surrogate-Assisted Genetic Programming With Simplified Models
 * for Automated Design of Dispatching Rules.
 * IEEE Transactions on Cybernetics 1–15.
 *
 * If use surrogate, then use the HalfShop surrogate model to evaluate.
 * Otherwise, use the original scheduling set.
 *
 * Created by yimei on 10/11/16.
 */
public class HalfShopEvaluationModel extends SimpleEvaluationModel implements Surrogate {

    protected SchedulingSet surrogateSet;
    protected boolean useSurrogate;

    public SchedulingSet getSurrogateSet() {
        return surrogateSet;
    }

    @Override
    public void useSurrogate() {
        useSurrogate = true;
    }

    @Override
    public void useOriginal() {
        useSurrogate = false;
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        surrogateSet = schedulingSet.surrogate(5, 500, 100, objectives);
        useSurrogate();
    }

    @Override
    public void evaluate(List<Fitness> fitnesses,
                         List<AbstractRule> rules,
                         EvolutionState state) {
        //only expecting one rule here
        if (rules.size() > 1 || rules.size() == 0) {
            try {
                throw new Exception(rules.size()+" - unexpected number of rules.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AbstractRule rule = rules.get(0);
        Fitness fitness = fitnesses.get(0);
        AbstractRule routingRule = schedulingSet.getSimulations().get(0).getRoutingRule();

        if (useSurrogate) {
            rule.calcFitness(fitness, state, surrogateSet, routingRule, objectives);
        }
        else {
            rule.calcFitness(fitness, state, schedulingSet, routingRule, objectives);
        }
    }

    @Override
    public void rotate() {
        super.rotate();
        surrogateSet.rotateSeed(objectives);
    }
}
