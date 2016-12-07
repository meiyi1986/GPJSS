package yimei.jss.rule;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.state.SystemState;

import java.util.List;

/**
 * The abstract dispatching rule for job shop scheduling.
 *
 * Created by yimei on 22/09/16.
 */
public abstract class AbstractRule {

    protected String name;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public RealMatrix objectiveValueMatrix(SchedulingSet schedulingSet,
                                           List<Objective> objectives) {
        int rows = schedulingSet.getObjectiveLowerBoundMtx().getRowDimension();
        int cols = schedulingSet.getObjectiveLowerBoundMtx().getColumnDimension();

        RealMatrix matrix = new Array2DRowRealMatrix(rows, cols);
        List<Simulation> simulations = schedulingSet.getSimulations();
        int col = 0;

        for (int j = 0; j < simulations.size(); j++) {
            Simulation simulation = simulations.get(j);
            simulation.setRule(this);

            simulation.run();
//            System.out.println(simulation.workCenterUtilLevelsToString());

            for (int i = 0; i < objectives.size(); i++) {
                matrix.setEntry(i, col,
                        simulation.objectiveValue(objectives.get(i)));
            }

            col ++;

            for (int k = 1; k < schedulingSet.getReplications().get(j); k++) {
                simulation.rerun();
//                System.out.println(simulation.workCenterUtilLevelsToString());

                for (int i = 0; i < objectives.size(); i++) {
                    matrix.setEntry(i, col,
                            simulation.objectiveValue(objectives.get(i)));
                }

                col ++;
            }

            simulation.reset();
        }

        return matrix;
    }

    public void calcFitness(Fitness fitness, EvolutionState state,
                            SchedulingSet schedulingSet, List<Objective> objectives) {
        double[] fitnesses = new double[objectives.size()];

        List<Simulation> simulations = schedulingSet.getSimulations();
        int col = 0;

        for (int j = 0; j < simulations.size(); j++) {
            Simulation simulation = simulations.get(j);
            simulation.setRule(this);

            simulation.run();
//            System.out.println(simulation.workCenterUtilLevelsToString());

            for (int i = 0; i < objectives.size(); i++) {
//                double obj = simulation.objectiveValue(objectives.get(i));
//                double lb = schedulingSet.getObjectiveLowerBound(i, col);
                double normObjValue = simulation.objectiveValue(objectives.get(i))
                        / schedulingSet.getObjectiveLowerBound(i, col);
                fitnesses[i] += normObjValue;
            }

            col ++;

            for (int k = 1; k < schedulingSet.getReplications().get(j); k++) {
                simulation.rerun();
//                System.out.println(simulation.workCenterUtilLevelsToString());

                for (int i = 0; i < objectives.size(); i++) {
//                    System.out.println("obj = " + simulation.objectiveValue(objectives.get(i)));
//                    System.out.println("lb  = " + schedulingSet.getObjectiveLowerBound(i, col));
                    double normObjValue = simulation.objectiveValue(objectives.get(i))
                            / schedulingSet.getObjectiveLowerBound(i, col);
                    fitnesses[i] += normObjValue;
                }

                col ++;
            }

            simulation.reset();
        }

        for (int i = 0; i < fitnesses.length; i++) {
            fitnesses[i] /= col;
        }

//        if (objectives.size() == 1) {
//            KozaFitness f = (KozaFitness) fitness;
//            f.setStandardizedFitness(state, fitnesses[0]);
//        }
//        else {
            MultiObjectiveFitness f = (MultiObjectiveFitness)fitness;
            f.setObjectives(state, fitnesses);
//        }
    }

    public Operation priorOperation(DecisionSituation decisionSituation) {
        List<Operation> queue = decisionSituation.getQueue();
        WorkCenter workCenter = decisionSituation.getWorkCenter();
        SystemState systemState = decisionSituation.getSystemState();

        Operation priorOp = queue.get(0);
        priorOp.setPriority(
                priority(priorOp, workCenter, systemState));

        for (int i = 1; i < queue.size(); i++) {
            Operation op = queue.get(i);
            op.setPriority(priority(op, workCenter, systemState));

            if (op.priorTo(priorOp))
                priorOp = op;
        }

        return priorOp;
    }

    public abstract double priority(Operation op,
                                    WorkCenter workCenter,
                                    SystemState systemState);
}
