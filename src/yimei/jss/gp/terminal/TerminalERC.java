package yimei.jss.gp.terminal;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;

import java.io.DataOutput;
import java.io.IOException;

/**
 * The ERC of the terminals.
 *
 * @author yimei
 */

public abstract class TerminalERC extends ERC {

    protected GPNode terminal;

    public void setTerminal(GPNode terminal) {
        this.terminal = terminal;
    }

    public GPNode getTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        return terminal.toString();
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
        terminal.parent = parent;
        terminal.argposition = argposition;
        terminal.eval(state, thread, input, stack, individual, problem);
    }

    @Override
    public int hashCode() {
        return terminal.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof TerminalERC) {
            TerminalERC o = (TerminalERC) other;
            return terminal.equals(o.terminal);
        }

        return false;
    }

    @Override
    public void writeNode(EvolutionState state, DataOutput output) throws IOException {
        terminal.writeNode(state, output);
    }

    @Override
    public boolean nodeEquals(GPNode node) {
        return equals(node);
    }

    @Override
    public String encode() {
        return terminal.toString();
    }
}
