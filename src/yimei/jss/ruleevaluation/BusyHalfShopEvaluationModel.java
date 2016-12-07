package yimei.jss.ruleevaluation;

import ec.EvolutionState;
import ec.util.Parameter;

/**
 * The busy HalfShop surrogate evaluation model. It is extended from
 * the HalfShop surrogate evaluation model by simply setting a full
 * utilization level - util = 1.
 *
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
public class BusyHalfShopEvaluationModel extends HalfShopEvaluationModel {

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        surrogateSet = schedulingSet.surrogateBusy(5, 400, 0, objectives);
    }
}
