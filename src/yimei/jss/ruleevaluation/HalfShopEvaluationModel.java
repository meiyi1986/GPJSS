package yimei.jss.ruleevaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.AbstractRule;
import yimei.jss.surrogate.Surrogate;

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

    private SchedulingSet surrogateSet;
    private boolean useSurrogate;

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
    public void evaluate(Fitness fitness,
                         AbstractRule rule,
                         EvolutionState state) {
        if (useSurrogate) {
            rule.calcFitness(fitness, state, surrogateSet, objectives);
        }
        else {
            rule.calcFitness(fitness, state, schedulingSet, objectives);
        }
    }

    @Override
    public void rotate() {
        super.rotate();
        surrogateSet.rotateSeed(objectives);
    }
}
