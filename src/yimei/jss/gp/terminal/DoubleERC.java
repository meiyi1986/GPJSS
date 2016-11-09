package yimei.jss.gp.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.app.regression.func.RegERC;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;

/**
 * Created by YiMei on 2/10/16.
 */
public class DoubleERC extends RegERC {

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        DoubleData a = ((DoubleData)(input));
        a.value = value;
    }
}
