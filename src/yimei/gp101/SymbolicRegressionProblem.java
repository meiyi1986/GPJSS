package yimei.gp101;

import ec.app.regression.Regression;

/**
 * Created by YiMei on 19/09/16.
 */
public class SymbolicRegressionProblem extends Regression {

    public double func(double x) {
        return x*x*x*x - 2 * x*x*x + x*x;
    }
}
