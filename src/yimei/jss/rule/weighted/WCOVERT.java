package yimei.jss.rule.weighted;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.state.SystemState;

/**
 * Created by YiMei on 28/09/16.
 */
public class WCOVERT extends AbstractRule {

    private double k;
    private double b;

    public WCOVERT(double k, double b) {
        name = "WCOVERT";
        setK(k);
        setB(b);
    }

    public WCOVERT() {
        this(2, 2);
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

    protected double totalExpWaitingTime;

    public void calcTotalExpWaitingTime(Operation op) {
        totalExpWaitingTime = b * op.getWorkRemaining();
    }

    @Override
    public double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
        calcTotalExpWaitingTime(op);

        double slack = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();

        if (slack < 0)
            slack = 0;

        double priority = 1 - (slack / (k * totalExpWaitingTime));

        if (priority < 0)
            priority = 0;

        return - priority * op.getJob().getWeight() / op.getProcTime();
    }
}
