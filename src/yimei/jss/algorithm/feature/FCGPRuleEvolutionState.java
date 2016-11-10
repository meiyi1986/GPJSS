//package yimei.jss.algorithm.feature;
//
//import ec.EvolutionState;
//import ec.Individual;
//import ec.gp.GPIndividual;
//import ec.gp.GPNode;
//import ec.util.Checkpoint;
//import ec.util.Parameter;
//import yimei.jss.feature.FeatureUtil;
//import yimei.jss.gp.GPRuleEvolutionState;
//import yimei.jss.gp.TerminalsChangable;
//import yimei.jss.niching.ClearingEvaluator;
//import yimei.jss.ruleoptimisation.RuleOptimizationProblem;
//import yimei.jss.ruleoptimisation.SurrogateRuleOptimizationProblem;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by YiMei on 5/10/16.
// */
//public class FCGPRuleEvolutionState extends GPRuleEvolutionState implements TerminalsChangable {
//
//    public static final String P_PRE_GENERATIONS = "pre-generations";
//    public static final String P_FRAC_ELITES = "frac-elites";
//    public static final String P_FRAC_ADAPTED = "frac-adapted";
//
//    private int preGenerations;
//    private double fracElites;
//    private double fracAdapted;
//
//    private List<Individual> archive = new ArrayList<>();
//
//    private double fitUB = Double.NEGATIVE_INFINITY;
//    private double fitLB = Double.POSITIVE_INFINITY;
//
//    @Override
//    public void setup(EvolutionState state, Parameter base) {
//        super.setup(state, base);
//
//        preGenerations = state.parameters.getIntWithDefault(
//                base.push(P_PRE_GENERATIONS), null, -1);
//        fracElites = state.parameters.getDoubleWithDefault(
//                base.push(P_FRAC_ELITES), null, 0.0);
//        fracAdapted = state.parameters.getDoubleWithDefault(
//                base.push(P_FRAC_ADAPTED), null, 1.0);
//    }
//
//    @Override
//    public int evolve() {
//        if (generation > 0)
//            output.message("Generation " + generation);
//
//        if (generation == preGenerations) {
//            for (Individual indi : population.subpops[0].individuals)
//                archive.add(indi);
//
//            SurrogateRuleOptimizationProblem problem = (SurrogateRuleOptimizationProblem)evaluator.p_problem;
//            problem.setOriginalSet();
//
//            for (Individual indi : archive)
//                problem.evaluate(this, indi, 0, 0);
//
//            List<GPIndividual> selIndis =
//                    FeatureUtil.selectDiverseIndis(this, archive, 30);
//
//            fitUB = selIndis.get(0).fitness.fitness();
//            fitLB = selIndis.get(selIndis.size()-1).fitness.fitness();
//
//            List<GPNode> selFeatures =
//                    FeatureUtil.featureSelection(this, selIndis, fitUB, fitLB);
//            List<GPNode> BBs =
//                    FeatureUtil.featureConstruction(this, selIndis, fitUB, fitLB);
//
//            selFeatures.addAll(BBs);
//
//            setTerminals(selFeatures);
//
//            adaptPopulation();
//
//            ((ClearingEvaluator)evaluator).setClear(false);
//        }
//
//        // EVALUATION
//        statistics.preEvaluationStatistics(this);
//        evaluator.evaluatePopulation(this);
//        statistics.postEvaluationStatistics(this);
//
//        if (generation < preGenerations)
//            archive.add(bestIndi(0));
//
//        // SHOULD WE QUIT?
//        if (evaluator.runComplete(this) && quitOnRunComplete)
//        {
//            output.message("Found Ideal Individual");
//            return R_SUCCESS;
//        }
//
//        // SHOULD WE QUIT?
//        if (generation == numGenerations-1)
//        {
//            return R_FAILURE;
//        }
//
//        // PRE-BREEDING EXCHANGING
//        statistics.prePreBreedingExchangeStatistics(this);
//        population = exchanger.preBreedingExchangePopulation(this);
//        statistics.postPreBreedingExchangeStatistics(this);
//
//        String exchangerWantsToShutdown = exchanger.runComplete(this);
//        if (exchangerWantsToShutdown!=null)
//        {
//            output.message(exchangerWantsToShutdown);
//	        /*
//	         * Don't really know what to return here.  The only place I could
//	         * find where runComplete ever returns non-null is
//	         * IslandExchange.  However, that can return non-null whether or
//	         * not the ideal individual was found (for example, if there was
//	         * a communication error with the server).
//	         *
//	         * Since the original version of this code didn't care, and the
//	         * result was initialized to R_SUCCESS before the while loop, I'm
//	         * just going to return R_SUCCESS here.
//	         */
//
//            return R_SUCCESS;
//        }
//
//        // BREEDING
//        statistics.preBreedingStatistics(this);
//
//        population = breeder.breedPopulation(this);
//
//        // POST-BREEDING EXCHANGING
//        statistics.postBreedingStatistics(this);
//
//        // POST-BREEDING EXCHANGING
//        statistics.prePostBreedingExchangeStatistics(this);
//        population = exchanger.postBreedingExchangePopulation(this);
//        statistics.postPostBreedingExchangeStatistics(this);
//
//        // Generate new instances if needed
//        RuleOptimizationProblem problem = (RuleOptimizationProblem)evaluator.p_problem;
//        if (problem.rotateSimSeed()) {
//            problem.rotateSeed();
//        }
//
//        // INCREMENT GENERATION AND CHECKPOINT
//        generation++;
//        if (checkpoint && generation%checkpointModulo == 0)
//        {
//            output.message("Checkpointing");
//            statistics.preCheckpointStatistics(this);
//            Checkpoint.setCheckpoint(this);
//            statistics.postCheckpointStatistics(this);
//        }
//
//        return R_NOTDONE;
//    }
//
//    @Override
//    public void adaptPopulation() {
//        FeatureUtil.adaptPopulationThreeParts(this, fracElites, fracAdapted);
//    }
//}
