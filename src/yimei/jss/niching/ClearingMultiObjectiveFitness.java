package yimei.jss.niching;

import ec.EvolutionState;
import ec.multiobjective.MultiObjectiveFitness;

/**
 * The multi-objective fitness with clearing method for niching.
 *
 * Created by yimei on 21/11/16.
 */
public class ClearingMultiObjectiveFitness
        extends MultiObjectiveFitness implements Clearable {

    private boolean cleared;

    @Override
    public void clear() {
        for (int i = 0; i < objectives.length; i++) {
            if (maximize[i]) {
                objectives[i] = Double.NEGATIVE_INFINITY;
            }
            else {
                objectives[i] = Double.POSITIVE_INFINITY;
            }
        }

        cleared = true;
    }

    @Override
    public boolean isCleared() {
        return cleared;
    }

    public void setObjectives(final EvolutionState state, double[] newObjectives) {
        super.setObjectives(state, newObjectives);

        cleared = false;
    }
}
