package yimei.jss.gp.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;

/**
 * The job shop attribute as terminal.
 *
 * @author yimei
 */

public class AttributeGPNode extends GPNode {

    private final JobShopAttribute attribute;

    public AttributeGPNode(JobShopAttribute attribute) {
        super();
        children = new GPNode[0];
        this.attribute = attribute;
    }

    public JobShopAttribute getJobShopAttribute() {
        return attribute;
    }

    @Override
    public String toString() {
        return attribute.getName();
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
        // The problem is essentially a priority calculation.
        CalcPriorityProblem calcPrioProb = ((CalcPriorityProblem)problem);

        DoubleData data = ((DoubleData)input);
        data.value = attribute.value(
                calcPrioProb.getOperation(),
                calcPrioProb.getWorkCenter(),
                calcPrioProb.getSystemState());
    }

    @Override
    public int hashCode() {
        return attribute.getName().hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof AttributeGPNode) {
            AttributeGPNode o = (AttributeGPNode)other;
            return (attribute == o.attribute);
        }

        return false;
    }
}
