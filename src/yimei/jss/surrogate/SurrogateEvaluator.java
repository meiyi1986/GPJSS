package yimei.jss.surrogate;

import ec.*;
import ec.simple.SimpleEvaluator;
import ec.util.Parameter;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yimei on 11/11/16.
 */
public class SurrogateEvaluator extends SimpleEvaluator {

    public static final String P_ARCHIVE_SIZE = "archive-size";

    public int archiveSizes[];

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        Parameter p = new Parameter(Initializer.P_POP);
        int subpopsLength = state.parameters.getInt(p.push(Population.P_SIZE), null, 1);
        Parameter p_subpop;
        archiveSizes = new int[subpopsLength];
        for (int i = 0; i < subpopsLength; i++)
        {
            p_subpop = new Parameter(EvolutionState.P_EVALUATOR).push(P_ARCHIVE_SIZE).push("" + i);
            archiveSizes[i] = state.parameters.getInt(p_subpop, null, 1);
        }
    }

    public void evaluatePopulation(final EvolutionState state) {
        // Evaluate using the surrogate model
        AbstractEvaluationModel evaluationModel =
                ((RuleOptimizationProblem)state.evaluator.p_problem).getEvaluationModel();

        ((Surrogate)evaluationModel).useSurrogate();
        super.evaluatePopulation(state);

        // Keep archives of best individuals in terms of surrogate fitness
        for (int x = 0; x < state.population.subpops.length; x++) {
            Arrays.sort(state.population.subpops[x].individuals);

            Individual[] archive = new Individual[archiveSizes[x]];
            System.arraycopy(state.population.subpops[x].individuals, 0,
                    archive, 0, archiveSizes[x]);

            state.population.subpops[x].individuals = archive;
        }

//        List<Double> surrogatefitness = new ArrayList<>();
//        for (int x = 0; x < state.population.subpops.length; x++) {
//            for (Individual indi : state.population.subpops[x].individuals) {
//                surrogatefitness.add(indi.fitness.fitness());
//            }
//        }

        // Evaluate using the orignial model
        ((Surrogate)evaluationModel).useOriginal();
        super.evaluatePopulation(state);

//        List<Double> testfitness = new ArrayList<>();
//        for (int x = 0; x < state.population.subpops.length; x++) {
//            for (Individual indi : state.population.subpops[x].individuals) {
//                testfitness.add(indi.fitness.fitness());
//            }
//        }
//
//        for (int i = 0; i < surrogatefitness.size(); i++) {
//            System.out.println(surrogatefitness.get(i) + "\t " + testfitness.get(i));
//        }
    }
}
