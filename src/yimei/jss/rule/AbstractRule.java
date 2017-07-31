package yimei.jss.rule;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import yimei.jss.jobshop.*;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;
import yimei.jss.simulation.state.SystemState;

import java.util.List;

/**
 * The abstract dispatching rule for job shop scheduling.
 * <p>
 * Created by yimei on 22/09/16.
 */
public abstract class AbstractRule {

    protected String name;
    protected RuleType type;
    public String getName() {
        return name;
    }
    public RuleType getType() { return type; }

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
            simulation.setSequencingRule(this);

            simulation.run();
//            System.out.println(simulation.workCenterUtilLevelsToString());

            for (int i = 0; i < objectives.size(); i++) {
                matrix.setEntry(i, col,
                        simulation.objectiveValue(objectives.get(i)));
            }

            col++;

            for (int k = 1; k < schedulingSet.getReplications().get(j); k++) {
                simulation.rerun();
//                System.out.println(simulation.workCenterUtilLevelsToString());

                for (int i = 0; i < objectives.size(); i++) {
                    matrix.setEntry(i, col,
                            simulation.objectiveValue(objectives.get(i)));
                }

                col++;
            }

            simulation.reset();
        }

        return matrix;
    }

    public void calcFitness(Fitness fitness, EvolutionState state,
                            SchedulingSet schedulingSet, AbstractRule otherRule,
                            List<Objective> objectives) {
        //whenever fitness is calculated, need a routing rule and a sequencing rule
        if (this.type == otherRule.type) {
            System.out.println("We need one routing rule and one sequencing rule, not 2 "+otherRule.getType()+" rules.");
            return;
        }
        AbstractRule routingRule;
        AbstractRule sequencingRule;
        if (this.type == RuleType.ROUTING) {
            routingRule = this;
            sequencingRule = otherRule;
        } else {
            routingRule = otherRule;
            sequencingRule = this;
        }

        double[] fitnesses = new double[objectives.size()];

        List<Simulation> simulations = schedulingSet.getSimulations();
        int col = 0;

        for (int j = 0; j < simulations.size(); j++) {
            Simulation simulation = simulations.get(j);
            simulation.setSequencingRule(sequencingRule);
            simulation.setRoutingRule(routingRule);
            simulation.run();

            for (int i = 0; i < objectives.size(); i++) {
/*                System.out.println("Makespan: "+simulation.objectiveValue(objectives.get(i)));
                System.out.println("Benchmark makespan: "+schedulingSet.getObjectiveLowerBound(i, col));*/
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
        MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
        f.setObjectives(state, fitnesses);
    }

    public OperationOption priorOperation(DecisionSituation decisionSituation) {
        List<OperationOption> queue = decisionSituation.getQueue();
        WorkCenter workCenter = decisionSituation.getWorkCenter();
        SystemState systemState = decisionSituation.getSystemState();

        OperationOption priorOp = queue.get(0);
        priorOp.setPriority(
                priority(priorOp, workCenter, systemState));

        for (int i = 1; i < queue.size(); i++) {
            OperationOption op = queue.get(i);
            op.setPriority(priority(op, workCenter, systemState));

            if (op.priorTo(priorOp))
                priorOp = op;
        }

        return priorOp;
    }

    public abstract double priority(OperationOption op,
                                    WorkCenter workCenter,
                                    SystemState systemState);
}
