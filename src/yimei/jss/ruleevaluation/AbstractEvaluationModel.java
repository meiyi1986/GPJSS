package yimei.jss.ruleevaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import yimei.jss.jobshop.Objective;
import yimei.jss.rule.AbstractRule;

import java.util.ArrayList;
import java.util.List;

/**
 * The evaluation model for job shop dispatching rules.
 *
 * Created by yimei on 10/11/16.
 */
public abstract class AbstractEvaluationModel {

    public final static String P_OBJECTIVES = "objectives";

    protected List<Objective> objectives;

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void setup(final EvolutionState state, final Parameter base) {
        // Get the objectives.
        objectives = new ArrayList<>();
        Parameter p = base.push(P_OBJECTIVES);
        int numObjectives = state.parameters.getIntWithDefault(p, null, 0);

        if (numObjectives == 0) {
            System.err.println("ERROR:");
            System.err.println("No objective is specified.");
            System.exit(1);
        }

        for (int i = 0; i < numObjectives; i++) {
            p = base.push(P_OBJECTIVES).push("" + i);
            String objectiveName = state.parameters.getStringWithDefault(p, null, "");
            Objective objective = Objective.get(objectiveName);

            objectives.add(objective);
        }
    }

    public abstract void evaluate(Fitness fitness,
                                  AbstractRule rule,
                                  EvolutionState state);

    public abstract boolean isRotatable();
    public abstract void rotate();
}
