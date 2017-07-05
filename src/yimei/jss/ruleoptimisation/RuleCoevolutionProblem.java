package yimei.jss.ruleoptimisation;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Population;
import ec.coevolve.GroupedProblemForm;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;
import yimei.jss.jobshop.Objective;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.operation.evolved.GPRule;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import yimei.jss.ruleevaluation.MultipleRuleEvaluationModel;
import java.util.ArrayList;
import java.util.List;

import static ec.app.ecsuite.ECSuite.*;

/**
 * Created by dyska on 28/06/17.
 */
public class RuleCoevolutionProblem extends RuleOptimizationProblem implements GroupedProblemForm {

    public static final String P_EVAL_MODEL = "eval-model";
    public static final String P_SHOULD_SET_CONTEXT = "set-context";
    boolean shouldSetContext;

    private AbstractEvaluationModel evaluationModel;

    public AbstractEvaluationModel getEvaluationModel() {
        return evaluationModel;
    }

    public List<Objective> getObjectives() {
        return evaluationModel.getObjectives();
    }

    public void rotateEvaluationModel() {
        evaluationModel.rotate();
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        Parameter p = base.push(P_EVAL_MODEL);
        evaluationModel = (AbstractEvaluationModel)
                state.parameters.getInstanceForParameter(p, null,
                        AbstractEvaluationModel.class);

        evaluationModel.setup(state, p);
        shouldSetContext = state.parameters.getBoolean(base.push(P_SHOULD_SET_CONTEXT),
                null, true);

    }

    @Override
    public void preprocessPopulation(EvolutionState state,
                                     Population pop,
                                     boolean[] prepareForFitnessAssessment,
                                     boolean countVictoriesOnly) {
        for (int i = 0 ; i < pop.subpops.length; i++) {
            if (prepareForFitnessAssessment[i]) {
                for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    ((MultiObjectiveFitness)(pop.subpops[i].individuals[j].fitness)).trials = new ArrayList();
                }
            }
        }
    }

    @Override
    public void postprocessPopulation(EvolutionState state,
                                      Population pop,
                                      boolean[] assessFitness,
                                      boolean countVictoriesOnly) {
        for (int i = 0; i < pop.subpops.length; i++) {
            if (assessFitness[i]) {
                for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                    MultiObjectiveFitness fit = ((MultiObjectiveFitness) (pop.subpops[i].individuals[j].fitness));

                    // we take the max over the trials
                    double max = Double.NEGATIVE_INFINITY;
                    int len = fit.trials.size();
                    for (int l = 0; l < len; l++) {
                        max = Math.max(((Double) (fit.trials.get(l))).doubleValue(), max);
                    }
                    if (max == Double.NEGATIVE_INFINITY && len > 0) {
                        //getting error messages when len == 0
                        //why would there be no trials?
                        System.out.println("What's going on here?");
                    }
                    double[] objectiveFitness = new double[1];
                    objectiveFitness[0] = max;
                    //TODO: Check this
                    fit.setObjectives(state, objectiveFitness);

                    pop.subpops[i].individuals[j].evaluated = true;
                }
            }
        }
    }

    @Override
    public void evaluate(EvolutionState state,
                         Individual[] ind,
                         boolean[] updateFitness,
                         boolean countVictoriesOnly,
                         int[] subpops,
                         int threadnum) {
        //System.out.println("Evaluating "+ind[0].toString() + " & " + ind[1].toString()+".");

        if (ind.length == 0) {
            state.output.fatal("Number of individuals provided to RuleCoevolutionProblem is 0!");
        }
        if (ind.length == 1) {
            state.output.warnOnce("Coevolution used," +
                    " but number of individuals provided to RuleCoevolutionProblem is 1.");
        }

        //we are going to run a simulation with the two rules
        //need to create/call an evaluation model

        //this should create a set of simulations to run

        List<AbstractRule> rules = new ArrayList<AbstractRule>();
        List<Fitness> fitnesses = new ArrayList<Fitness>();

        for (int i = 0; i < ind.length; ++i) {
            fitnesses.add(ind[i].fitness);
            rules.add(new GPRule(((GPIndividual)ind[i]).trees[0]));
        }

        evaluationModel.evaluate(fitnesses, rules, state);

        //okay, we have run a trial for the above rules - now we add a trial for each

        // update individuals to reflect the trial
        for (int i = 0; i < ind.length; i++) {
            GPIndividual coind = (GPIndividual) (ind[i]);
            Double trialValue = fitnesses.get(i).fitness(); //will actually be the same for all individuals
            if (updateFitness[i])
            {
                // Update the context if this is the best trial.  We're going to assume that the best
                // trial is trial #0 so we don't have to search through them.
                int len = coind.fitness.trials.size();

                if (len == 0)  // easy
                {
                    if (shouldSetContext) {
                        coind.fitness.setContext(ind, i);
                    }
                    coind.fitness.trials.add(trialValue);
                }
                else if (((Double)(coind.fitness.trials.get(0))).doubleValue() < trialValue)  // best trial is presently #0
                {
                    if (shouldSetContext) {
                        coind.fitness.setContext(ind, i);
                    }
                    // put me at position 0
                    Double t = (Double)(coind.fitness.trials.get(0));
                    coind.fitness.trials.set(0, new Double(trialValue));  // put me at 0
                    coind.fitness.trials.add(t);  // move him to the end
                }

                // finally set the fitness for good measure
                double[] objectiveFitness = new double[1];
                objectiveFitness[0] = trialValue;
                ((MultiObjectiveFitness) coind.fitness).setObjectives(state, objectiveFitness);

            }
        }
    }
}
