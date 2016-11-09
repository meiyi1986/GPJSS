package yimei.jss.ruleopt;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Scenario;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.evolved.GPRule;
import yimei.jss.simulation.Simulation;

import java.util.ArrayList;
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

    public final static String P_OBJECTIVES = "objectives";
    public final static String P_SCENARIO = "scenario";

    /**
     * The group of static instances.
     */
    public final static String P_GROUP = "group";

    /**
     * The starting seed of the simulation models.
     */
    public final static String P_SIM_SEED = "sim-seed";

    /**
     * Whether to rotate the simulation seed or not.
     */
    public final static String P_ROTATE_SIM_SEED = "rotate-sim-seed";

    public final static String P_SIM_MODELS = "sim-models";
    public final static String P_SIM_NUM_MACHINES = "num-machines";
    public final static String P_SIM_NUM_JOBS = "num-jobs";
    public final static String P_SIM_WARMUP_JOBS = "warmup-jobs";
    public final static String P_SIM_MIN_NUM_OPS = "min-num-ops";
    public final static String P_SIM_MAX_NUM_OPS = "max-num-ops";
    public final static String P_SIM_UTIL_LEVEL = "util-level";
    public final static String P_SIM_DUE_DATE_FACTOR = "due-date-factor";
    public final static String P_SIM_REPLICATIONS = "replications";

    protected List<Objective> objectives;
    protected SchedulingSet trainSet;
    protected String scenarioName;
    protected long simSeed;
    protected boolean rotateSimSeed;
    protected String group; // the group for static instances

    public List<Objective> getObjectives() {
        return objectives;
    }

    public SchedulingSet getTrainSet() {
        return trainSet;
    }

    public boolean rotateSimSeed() {
        return rotateSimSeed;
    }

    public void rotateSeed() {
        trainSet.rotateSeed(objectives);
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

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
            p = base.push(P_OBJECTIVES).push(""+i);
            String objectiveName = state.parameters.getStringWithDefault(p, null, "");
            Objective objective = Objective.get(objectiveName);

            objectives.add(objective);
        }

        // Get the scenario
        p = base.push(P_SCENARIO);
        scenarioName = state.parameters.getStringWithDefault(p, null, "");

        if (scenarioName.equals(Scenario.DYNAMIC_JOB_SHOP.getName())) {
            // Get the seed for the simulation.
            p = base.push(P_SIM_SEED);
            simSeed = state.parameters.getLongWithDefault(p, null, 0);

            // Get the simulation models.
            p = base.push(P_SIM_MODELS);
            int numSimModels = state.parameters.getIntWithDefault(p, null, 0);

            if (numSimModels == 0) {
                System.err.println("ERROR:");
                System.err.println("No simulation model is specified.");
                System.exit(1);
            }

            List<Simulation> trainSimulations = new ArrayList<>();
            List<Integer> replications = new ArrayList<>();
            for (int x = 0; x < numSimModels; x++) {
                // Read this simulation model
                Parameter b = base.push(P_SIM_MODELS).push(""+x);
                // Number of machines
                p = b.push(P_SIM_NUM_MACHINES);
                int numMachines = state.parameters.getIntWithDefault(p, null, 10);
                // Number of jobs
                p = b.push(P_SIM_NUM_JOBS);
                int numJobs = state.parameters.getIntWithDefault(p, null, 5000);
                // Number of warmup jobs
                p = b.push(P_SIM_WARMUP_JOBS);
                int warmupJobs = state.parameters.getIntWithDefault(p, null, 1000);
                // Min number of operations
                p = b.push(P_SIM_MIN_NUM_OPS);
                int minNumOps = state.parameters.getIntWithDefault(p, null, 2);
                // Max number of operations
                p = b.push(P_SIM_MAX_NUM_OPS);
                int maxNumOps = state.parameters.getIntWithDefault(p, null, numMachines);
                // Utilization level
                p = b.push(P_SIM_UTIL_LEVEL);
                double utilLevel = state.parameters.getDoubleWithDefault(p, null, 0.85);
                // Due date factor
                p = b.push(P_SIM_DUE_DATE_FACTOR);
                double dueDateFactor = state.parameters.getDoubleWithDefault(p, null, 4.0);
                // Number of replications
                p = b.push(P_SIM_REPLICATIONS);
                int rep = state.parameters.getIntWithDefault(p, null, 1);

                Simulation simulation = new Simulation(simSeed,
                        null, numMachines, numJobs, warmupJobs,
                        minNumOps, maxNumOps, utilLevel, dueDateFactor, false);

                trainSimulations.add(simulation);
                replications.add(new Integer(rep));
            }

            trainSet = new SchedulingSet(trainSimulations, replications, objectives);

            p = base.push(P_ROTATE_SIM_SEED);
            rotateSimSeed = state.parameters.getBoolean(p, null, false);
        }
        else if (scenarioName.equals(Scenario.STATIC_JOB_SHOP.getName())) {
//			p = new Parameter(P_GROUP);
//
//			group = parameters.getString(p, null);
//
//			if (group == null) {
//				System.err.println("ERROR:");
//				System.err.println("The group of the JSS training instances is not specified.");
//				System.exit(1);
//			}
//
//			// Create the job shop instances.
//			generator = new StaticJobShopSchedulingInstanceGenerator(group, objectives);
//			trainingSet = generator.generate();
        }
    }

    @Override
    public void evaluate(EvolutionState state, Individual indi, int subpopulation, int threadnum) {
        GPRule rule = new GPRule(((GPIndividual)indi).trees[0]);

        if (objectives.size() > 1) {
            System.err.println("ERROR:");
            System.err.println("Do NOT support more than one objective yet.");
            System.exit(1);
        }

        rule.calcFitness(indi.fitness, state, trainSet, objectives);

        indi.evaluated = true;
    }
}
