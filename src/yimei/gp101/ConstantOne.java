package yimei.gp101;

import ec.EvolutionState;
import ec.Problem;
import ec.app.regression.Regression;
import ec.app.regression.RegressionData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

/**
 * Created by YiMei on 19/09/16.
 */
public class ConstantOne extends GPNode {
    @Override
    public String toString() {
        return "1";
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
        RegressionData rd = ((RegressionData)(input));
        rd.x = 1;
    }
}
