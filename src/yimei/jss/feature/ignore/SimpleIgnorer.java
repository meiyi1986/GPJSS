package yimei.jss.feature.ignore;

import ec.gp.GPNode;
import ec.gp.GPTree;
import yimei.jss.gp.terminal.ConstantTerminal;

/**
 * A simple ignorer: replace the ignored GP node with a constant 1.0.
 *
 * Created by yimei on 12/10/16.
 */
public class SimpleIgnorer extends Ignorer {
    @Override
    public void ignore(GPNode node) {
        GPNode newNode = new ConstantTerminal(1.0);
        newNode.parent = node.parent;
        newNode.argposition = node.argposition;
        if (newNode.parent instanceof GPNode)
            ((GPNode)(newNode.parent)).children[newNode.argposition] = newNode;
        else
            ((GPTree)(newNode.parent)).child = newNode;
    }
}
