package yimei.jss.ruleoptimisation;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import yimei.jss.jobshop.Objective;
import yimei.jss.rule.evolved.GPRule;

import java.util.List;

/**
 * The rule optimisation problem.
 *
 * In the problem, a rule is evaluated by being
 * applied to the training set.
 *
 * Created by YiMei on 29/09/16.
 */
public class RuleOptimizationProblem extends GPProblem implements SimpleProblemForm {

    public static final String P_EVAL_MODEL = "eval-model";

    private AbstractEvaluationModel evaluationModel;

    public List<Objective> getObjectives() {
        return evaluationModel.getObjectives();
    }

    public AbstractEvaluationModel getEvaluationModel() {
        return evaluationModel;
    }

    public void rotateEvaluationModel() {
        evaluationModel.rotate();
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        Parameter p = base.push(P_EVAL_MODEL);
        evaluationModel = (AbstractEvaluationModel)(
                state.parameters.getInstanceForParameter(
                        p, null, AbstractEvaluationModel.class));

        evaluationModel.setup(state, p);
    }

    @Override
    public void evaluate(EvolutionState state,
                         Individual indi,
                         int subpopulation,
                         int threadnum) {
        GPRule rule = new GPRule(((GPIndividual)indi).trees[0]);

        if (getObjectives().size() > 1) {
            System.err.println("ERROR:");
            System.err.println("Do NOT support more than one objective yet.");
            System.exit(1);
        }

        evaluationModel.evaluate(indi.fitness, rule, state);

//        rule.calcFitness(indi.fitness, state, trainSet, objectives);

        indi.evaluated = true;
    }
}
