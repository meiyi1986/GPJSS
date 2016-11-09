package yimei.jss.gp.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

/**
 * Created by YiMei on 3/10/16.
 */
public class BuildingBlock extends GPNode {

    private GPNode root;

    public BuildingBlock(GPNode root) {
        super();
        this.root = (GPNode)root.clone();
    }

    public GPNode getRoot() {
        return root;
    }

    public String toString() {
        return subTreeString(root);
    }

    private String subTreeString(GPNode node) {
        if (node.children.length == 0) {
            return node.toString();
        }
        else {
            String string = "(" + node.toString();
            for (GPNode child : node.children) {
                string += " " + subTreeString(child);
            }
            string += ")";

            return string;
        }
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
        root.eval(state, thread, input, stack, individual, problem);
    }
}
