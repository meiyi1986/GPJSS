/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package yimei.jss.gp.function;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

/**
 *
 * Functional GPNode: If. It takes three children x, y and z. If x > 0, then it returns y. Otherwise, it returns z.
 *
 * @author yimei
 *
 */

public class If extends GPNode {

	public String toString() {
		return "if";
	}

/*
  public void checkConstraints(final EvolutionState state,
  final int tree,
  final GPIndividual typicalIndividual,
  final Parameter individualBase)
  {
  super.checkConstraints(state,tree,typicalIndividual,individualBase);
  if (children.length!=2)
  state.output.error("Incorrect number of children for node " +
  toStringForError() + " at " +
  individualBase);
  }
*/
    public int expectedChildren() {
    	return 3;
    }

    public void eval(final EvolutionState state,
    		final int thread,
    		final GPData input,
    		final ADFStack stack,
    		final GPIndividual individual,
    		final Problem problem) {

        double result;
        DoubleData rd = ((DoubleData)(input));

		children[0].eval(state,thread,input,stack,individual,problem);
		result = rd.value;
		if (result > 0) {
			children[1].eval(state,thread,input,stack,individual,problem);
		}
		else {
			children[2].eval(state,thread,input,stack,individual,problem);
		}
    }
}

