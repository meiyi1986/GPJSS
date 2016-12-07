package yimei.jss.simulation.event;

import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;

import java.util.List;

/**
 * Created by yimei on 22/09/16.
 */
public abstract class AbstractEvent implements Comparable<AbstractEvent> {

    protected double time;

    public AbstractEvent(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    public abstract void trigger(Simulation simulation);

    public abstract void addDecisionSituation(DynamicSimulation simulation,
                                              List<DecisionSituation> situations,
                                              int minQueueLength);

    @Override
    public int compareTo(AbstractEvent other) {
        if (time < other.time)
            return -1;

        if (time > other.time)
            return 1;

        return 0;
    }
}
