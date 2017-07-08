package yimei.jss.rule.operation.evolved;

import ec.gp.GPNode;
import ec.gp.GPTree;
import yimei.jss.feature.ignore.Ignorer;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.gp.GPNodeComparator;
import yimei.jss.jobshop.OperationOption;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;
import yimei.util.lisp.LispParser;

/**
 * The GP-evolved rule.
 * <p>
 * Created by YiMei on 27/09/16.
 */
public class GPRule extends AbstractRule {

    private GPTree gpTree;
    private String lispString;

    public GPRule(GPTree gpTree) {
        name = "\"GPRule\"";
        this.gpTree = gpTree;
    }

    public GPRule(GPTree gpTree, String expression) {
        name = "\"GPRule\"";
        this.lispString = expression;
        this.gpTree = gpTree;
    }

    public GPTree getGPTree() {
        return gpTree;
    }

    public void setGPTree(GPTree gpTree) {
        this.gpTree = gpTree;
    }

    public String getLispString() {
        return lispString;
    }

    public static GPRule readFromLispExpression(String expression) {
        GPTree tree = LispParser.parseJobShopRule(expression);

        return new GPRule(tree, expression);
    }

    public void ignore(GPNode tree, GPNode feature, Ignorer ignorer) {
        if (tree.depth() < feature.depth())
            return;

        if (GPNodeComparator.equals(tree, feature)) {
            ignorer.ignore(tree);

            return;
        }

        if (tree.depth() == feature.depth())
            return;

        for (GPNode child : tree.children) {
            ignore(child, feature, ignorer);
        }
    }

    public void ignore(GPNode feature, Ignorer ignorer) {
        ignore(gpTree.child, feature, ignorer);
    }

    public double priority(OperationOption op, WorkCenter workCenter,
                           SystemState systemState) {
        CalcPriorityProblem calcPrioProb =
                new CalcPriorityProblem(op, workCenter, systemState);

        DoubleData tmp = new DoubleData();
        gpTree.child.eval(null, 0, tmp, null, null, calcPrioProb);

        return tmp.value;
    }
}