package yimei.jss.gp;

import ec.EvolutionState;
import ec.gp.GPNode;

import java.util.List;

/**
 * An EvolutionState class with this interface
 * means that the terminal set is changable
 * during the evolutionary process.
 *
 * Created by YiMei on 6/10/16.
 */
public interface TerminalsChangable {

    List<GPNode> getTerminals();
    void setTerminals(List<GPNode> terminals);
    void adaptPopulation();
}
