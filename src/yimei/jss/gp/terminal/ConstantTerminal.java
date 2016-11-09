package yimei.jss.gp.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

/**
 * A constant terminal.
 *
 * Created by YiMei on 29/09/16.
 */
public class ConstantTerminal extends GPNode {

    private final double value;

    public ConstantTerminal(double value) {
        super();
        children = new GPNode[0];
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
        DoubleData data = ((DoubleData) input);
        data.value = value;
    }
}
