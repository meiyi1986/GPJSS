package yimei.jss.jobshop;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * The set of scheduling problems. The set includes:
 *   1. A list of simulations.
 *   2. Number of replications for each simulation.
 *   3. The objective lower bound matrix: (i,j) - the lower bound of objective i in replication j.
 *
 * Created by YiMei on 28/09/16.
 */
public class SchedulingSet {

    private List<Simulation> simulations;
    private List<Integer> replications;
    private RealMatrix objectiveLowerBoundMtx;

    public SchedulingSet(List<Simulation> simulations,
                         List<Integer> replications,
                         List<Objective> objectives) {
        this.simulations = simulations;
        this.replications = replications;
        createObjectiveLowerBoundMatrix(objectives);
        lowerBoundsFromBenchmarkRule(objectives);
    }

    public List<Simulation> getSimulations() {
        return simulations;
    }

    public List<Integer> getReplications() {
        return replications;
    }

    public RealMatrix getObjectiveLowerBoundMtx() {
        return objectiveLowerBoundMtx;
    }

    public double getObjectiveLowerBound(int row, int col) {
        return objectiveLowerBoundMtx.getEntry(row, col);
    }

    public void setReplications(List<Integer> replications) {
        this.replications = replications;
    }

    public void setRule(AbstractRule rule) {
        for (Simulation simulation : simulations) {
            simulation.setRule(rule);
        }
    }

    public void rotateSeed(List<Objective> objectives) {
        for (Simulation simulation : simulations) {
            simulation.rotateSeed();
        }

        lowerBoundsFromBenchmarkRule(objectives);
    }

//    public void reset() {
//        for (DynamicSimulation simulation : simulations) {
//            simulation.reset();
//        }
//    }

    private void createObjectiveLowerBoundMatrix(List<Objective> objectives) {
        int rows = objectives.size();
        int cols = 0;
        for (int rep : replications)
            cols += rep;

        objectiveLowerBoundMtx = new Array2DRowRealMatrix(rows, cols);
    }

    private void lowerBoundsFromBenchmarkRule(List<Objective> objectives) {
        for (int i = 0; i < objectives.size(); i++) {
            Objective objective = objectives.get(i);
            AbstractRule benchmarkRule = objective.benchmarkRule();

            int col = 0;
            for (int j = 0; j < simulations.size(); j++) {
                Simulation simulation = simulations.get(j);
                simulation.setRule(benchmarkRule);
                simulation.run();
//                System.out.println(simulation.workCenterUtilLevelsToString());
                double value = simulation.objectiveValue(objective);
                objectiveLowerBoundMtx.setEntry(i, col, value);
                col ++;

                for (int k = 1; k < replications.get(j); k++) {
                    simulation.rerun();
//                    System.out.println(simulation.workCenterUtilLevelsToString());
                    value = simulation.objectiveValue(objective);
                    objectiveLowerBoundMtx.setEntry(i, col, value);
                    col ++;
                }

                simulation.reset();
            }
        }
    }

    public SchedulingSet surrogate(int numWorkCenters, int numJobsRecorded,
                                   int warmupJobs, List<Objective> objectives) {
        List<Simulation> surrogateSimulations = new ArrayList<>();
        List<Integer> surrogateReplications = new ArrayList<>();

        for (int i = 0; i < simulations.size(); i++) {
            surrogateSimulations.add(
                    simulations.get(i).surrogate(
                    numWorkCenters, numJobsRecorded, warmupJobs));
            surrogateReplications.add(1);
        }

        return new SchedulingSet(surrogateSimulations,
                surrogateReplications, objectives);
    }

    public SchedulingSet surrogateBusy(int numWorkCenters, int numJobsRecorded,
                                   int warmupJobs, List<Objective> objectives) {
        List<Simulation> surrogateSimulations = new ArrayList<>();
        List<Integer> surrogateReplications = new ArrayList<>();

        for (int i = 0; i < simulations.size(); i++) {
            surrogateSimulations.add(
                    simulations.get(i).surrogateBusy(
                            numWorkCenters, numJobsRecorded, warmupJobs));
            surrogateReplications.add(1);
        }

        return new SchedulingSet(surrogateSimulations,
                surrogateReplications, objectives);
    }

    public static SchedulingSet dynamicFullSet(long simSeed,
                                               double utilLevel,
                                               double dueDateFactor,
                                               List<Objective> objectives,
                                               int reps) {
        List<Simulation> simulations = new ArrayList<>();
        simulations.add(
                DynamicSimulation.standardFull(simSeed, null, 10, 4000, 1000,
                        utilLevel, dueDateFactor));
        List<Integer> replications = new ArrayList<>();
        replications.add(reps);

        return new SchedulingSet(simulations, replications, objectives);
    }

    public static SchedulingSet dynamicMissingSet(long simSeed,
                                                  double utilLevel,
                                                  double dueDateFactor,
                                                  List<Objective> objectives,
                                                  int reps) {
        List<Simulation> simulations = new ArrayList<>();
        simulations.add(
                DynamicSimulation.standardMissing(simSeed, null, 10, 4000, 1000,
                        utilLevel, dueDateFactor));
        List<Integer> replications = new ArrayList<>();
        replications.add(reps);

        return new SchedulingSet(simulations, replications, objectives);
    }

    public static SchedulingSet generateSet(long simSeed,
                                            String scenario,
                                            String setName,
                                            List<Objective> objectives,
                                            int replications) {
        if (scenario.equals(Scenario.DYNAMIC_JOB_SHOP.getName())) {
            String[] parameters = setName.split("-");
            double utilLevel = Double.valueOf(parameters[1]);
            double dueDateFactor = Double.valueOf(parameters[2]);

            if (parameters[0].equals("missing")) {
                return SchedulingSet.dynamicMissingSet(simSeed, utilLevel, dueDateFactor, objectives, replications);
            }
            else if (parameters[0].equals("full")) {
                return SchedulingSet.dynamicFullSet(simSeed, utilLevel, dueDateFactor, objectives, replications);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }
}
