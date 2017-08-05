package yimei.jss.ruleevaluation;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleFitness;
import ec.util.Parameter;
import org.apache.commons.math3.analysis.function.Abs;
import yimei.jss.jobshop.FlexibleStaticInstance;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyska on 4/07/17.
 */
public class MultipleRuleEvaluationModel extends AbstractEvaluationModel{

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
    public final static String P_SIM_REPLICATIONS = "replications";

    protected SchedulingSet schedulingSet;
    protected long simSeed;
    protected boolean rotateSimSeed;

    public SchedulingSet getSchedulingSet() {
        return schedulingSet;
    }

    public long getSimSeed() {
        return simSeed;
    }

    public boolean isRotateSimSeed() {
        return rotateSimSeed;
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // Get the seed for the simulation.
        Parameter p = base.push(P_SIM_SEED);
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
            Parameter b = base.push(P_SIM_MODELS).push("" + x);
            // Number of machines
            p = b.push(P_SIM_NUM_MACHINES);
            int numMachines = state.parameters.getIntWithDefault(p, null, 10);
            // Number of jobs
            p = b.push(P_SIM_NUM_JOBS);
            int numJobs = state.parameters.getIntWithDefault(p, null, 5000);
            // Number of warmup jobs
            p = b.push(P_SIM_WARMUP_JOBS);
            // Number of replications
            p = b.push(P_SIM_REPLICATIONS);
            int rep = state.parameters.getIntWithDefault(p, null, 1);

            String filePath = state.parameters.getString(new Parameter("filePath"), null);
            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(filePath);

            Simulation simulation = new StaticSimulation(null, null, instance);

            trainSimulations.add(simulation);
            replications.add(new Integer(rep));
        }

        schedulingSet = new SchedulingSet(trainSimulations, replications, objectives);

        p = base.push(P_ROTATE_SIM_SEED);
        rotateSimSeed = state.parameters.getBoolean(p, null, false);
    }

    @Override
    public void evaluate(List<Fitness> currentFitnesses,
                         List<AbstractRule> rules,
                         EvolutionState state) {
        //expecting 2 rules here - one routing rule and one sequencing rule
        if (rules.size() != currentFitnesses.size() || rules.size() != 2) {
            return;
        }

        AbstractRule sequencingRule = rules.get(0);
        AbstractRule routingRule = rules.get(1);

        //code taken from Abstract Rule

        double[] fitnesses = new double[objectives.size()];

        List<Simulation> simulations = schedulingSet.getSimulations();
        int col = 0;

        for (int j = 0; j < simulations.size(); j++) {
            Simulation simulation = simulations.get(j);
            simulation.setSequencingRule(sequencingRule);
            simulation.setRoutingRule(routingRule);

            simulation.run();

            for (int i = 0; i < objectives.size(); i++) {
                double normObjValue = simulation.objectiveValue(objectives.get(i))
                        / schedulingSet.getObjectiveLowerBound(i, col);
                fitnesses[i] += normObjValue;
            }

            col++;

            for (int k = 1; k < schedulingSet.getReplications().get(j); k++) {
                simulation.rerun();

                for (int i = 0; i < objectives.size(); i++) {
                    double normObjValue = simulation.objectiveValue(objectives.get(i))
                            / schedulingSet.getObjectiveLowerBound(i, col);
                    fitnesses[i] += normObjValue;
                }

                col++;
            }

            simulation.reset();
        }

        for (int i = 0; i < fitnesses.length; i++) {
            fitnesses[i] /= col;
        }

        for (Fitness fitness: currentFitnesses) {
            MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
            f.setObjectives(state, fitnesses);
        }
    }

    @Override
    public boolean isRotatable() {
        return rotateSimSeed;
    }

    @Override
    public void rotate() {
        schedulingSet.rotateSeed(objectives);
    }
}
