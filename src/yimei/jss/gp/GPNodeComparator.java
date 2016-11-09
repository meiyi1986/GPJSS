package yimei.jss.gp;

import ec.gp.GPNode;
import yimei.jss.gp.function.*;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;

/**
 * Compare two GP nodes to see if they are equivalent.
 *
 * Created by YiMei on 5/10/16.
 */
public class GPNodeComparator {

    public static boolean equals(GPNode o1, GPNode o2) {
        if (o1.toString().equals(o2.toString())) {
            if (o1.children.length == o2.children.length) {
                if (o1.children.length == 0)
                    return true;

                switch (o1.toString()) {
                    case "+":
                        return sameChildrenUnordered(o1.children, o2.children);
                    case "-":
                        return sameChildrenUnordered(o1.children, o2.children);
                    case "*":
                        return sameChildrenUnordered(o1.children, o2.children);
                    case "/":
                        return sameChildrenUnordered(o1.children, o2.children);
                    case "max":
                        return sameChildrenUnordered(o1.children, o2.children);
                    case "min":
                        return sameChildrenUnordered(o1.children, o2.children);
                    case "if":
                        return sameChildrenOrdered(o1.children, o2.children);
                }
            }
        }

        return false;
    }

    public static boolean sameChildrenOrdered(GPNode[] children1,
                                              GPNode[] children2) {
        for (int i = 0; i < children1.length; i++) {
            boolean same = equals(children1[i], children2[i]);

            if (!same)
                return false;
        }

        return true;
    }

    public static boolean sameChildrenUnordered(GPNode[] children1,
                                                GPNode[] children2) {
        boolean[] matched = new boolean[children2.length];

        for (int i = 0; i < children1.length; i++) {
            boolean foundSame = false;

            for (int j = 0; j < children2.length; j++) {
                if (matched[j])
                    continue;

                boolean same = equals(children1[i], children2[j]);

                if (same) {
                    foundSame = true;
                    matched[j] = true;
                    break;
                }
            }

            if (!foundSame)
                return false;
        }

        return true;
    }

    public static void main(String[] args) {
        GPNode node1 = new Mul();
        node1.children = new GPNode[2];
        node1.children[0] = new AttributeGPNode(JobShopAttribute.DUE_DATE);
        node1.children[1] = new AttributeGPNode(JobShopAttribute.PROC_TIME);

        GPNode node2 = new Div();
        node2.children = new GPNode[2];
        node2.children[0] = new AttributeGPNode(JobShopAttribute.PROC_TIME);
        node2.children[1] = new AttributeGPNode(JobShopAttribute.DUE_DATE);

        System.out.println(equals(node1, node2));

    }
}
