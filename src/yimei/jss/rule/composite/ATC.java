package yimei.jss.rule.composite;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

import java.util.List;

/**
 * Created by YiMei on 28/09/16.
 */
public class ATC extends AbstractRule {

    private double k;
    private double b;

    public ATC(double k, double b) {
        name = "ATC";
        setK(k);
        setB(b);
    }

    public ATC() {
        this(3, 2);
    }

    @Override
    public String getName() {
        return "\"" + name + "(k=" + getK() + ",b=" + getB() + ")\"";
    }

    public void setK(double k) {
        this.k = k;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getK() {
        return k;
    }

    public double getB() {
        return b;
    }

    protected double slackNorm;
    protected double expWaitingTime;

    public void calcSlackNorm(Operation op, WorkCenter workCenter, SystemState systemState) {
        slackNorm = 0.0d;

        // Get the queue that the processing is in
        List<Operation> queue = workCenter.getQueue();
        for (Operation o : queue) {
            slackNorm += o.getProcTime();
        }

        slackNorm = (slackNorm / queue.size()) * k;
    }

    public void calcExpWaitingTime(Operation op) {
        expWaitingTime = b * (op.getWorkRemaining() - op.getProcTime());
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        calcSlackNorm(op, workCenter, systemState);
        calcExpWaitingTime(op);

        double slack = systemState.slack(op);
        slack -= expWaitingTime;
        double prod1 = -Math.max(slack, 0.0d) / slackNorm;

        return -(1.0 / op.getProcTime()) * Math.exp(prod1);
    }
}
