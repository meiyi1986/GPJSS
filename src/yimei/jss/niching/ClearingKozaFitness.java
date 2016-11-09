package yimei.jss.niching;

import ec.gp.koza.KozaFitness;

/**
 * The Koza Fitness based on clearing.
 * The fitness is set to infinity if the individual is cleared.
 *
 * Created by YiMei on 3/10/16.
 */
public class ClearingKozaFitness extends KozaFitness implements Clearable {

    @Override
    public void clear() {
        standardizedFitness = Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean isCleared() {
        return (standardizedFitness == Double.POSITIVE_INFINITY);
    }
}
