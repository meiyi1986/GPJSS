package yimei.jss.gp.terminal;

import ec.EvolutionState;
import ec.gp.ERC;
import ec.util.Parameter;
import yimei.jss.gp.GPRuleEvolutionState;

/**
 * The terminal ERC, with uniform selection.
 *
 * @author yimei
 */

public class TerminalERCUniform extends TerminalERC {

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        terminal = ((GPRuleEvolutionState)state).pickTerminalRandom();
    }

    @Override
    public void resetNode(EvolutionState state, int thread) {
        terminal = ((GPRuleEvolutionState)state).pickTerminalRandom();

        if (terminal instanceof ERC) {
            ERC ercTerminal = new DoubleERC();
            ercTerminal.resetNode(state, thread);
            terminal = ercTerminal;
        }
    }

    @Override
    public void mutateERC(EvolutionState state, int thread) {
        resetNode(state, thread);
    }
}
