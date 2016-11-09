package yimei.jss.rule.weighted;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.composite.ATC;
import yimei.jss.simulation.state.SystemState;

import java.util.List;

/**
 * Created by YiMei on 27/09/16.
 */
public class WATC extends ATC {

    private double k;
    private double b;

    public WATC(double k, double b) {
        name = "WATC";
        setK(k);
        setB(b);
    }

    public WATC() {
        this(3, 2);
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        calcSlackNorm(op, workCenter, systemState);
        calcExpWaitingTime(op);

        double slack = systemState.slack(op);
        slack -= expWaitingTime;
        double prod1 = -Math.max(slack, 0.0d) / slackNorm;

        return -(op.getJob().getWeight() / op.getProcTime()) * Math.exp(prod1);
    }
}
