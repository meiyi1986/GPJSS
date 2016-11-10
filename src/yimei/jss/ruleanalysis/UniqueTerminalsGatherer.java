package yimei.jss.ruleanalysis;

import ec.gp.GPNode;
import ec.gp.GPNodeGatherer;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The GP node gatherer that collects the unique terminals.
 *
 * Created by yimei on 10/10/16.
 */
public class UniqueTerminalsGatherer extends GPNodeGatherer {

    List<GPNode> terminals;

    public UniqueTerminalsGatherer(List<GPNode> terminals) {
        super();
        this.terminals = terminals;
    }

    public UniqueTerminalsGatherer() {
        this(new ArrayList<>());
    }

    @Override
    public boolean test(GPNode thisNode) {
        if (thisNode.depth() > 1)
            return false;

        if (NumberUtils.isNumber(thisNode.toString()))
            return false;

        for (GPNode terminal : terminals) {
            if (terminal.toString().equals(thisNode.toString()))
                return false;
        }

        terminals.add(thisNode);

        return true;
    }
}
