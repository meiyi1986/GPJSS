package yimei.jss;

import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.jobshop.StaticInstance;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.rule.operation.basic.EDD;
import yimei.jss.rule.operation.basic.FDD;
import yimei.jss.rule.operation.basic.SPT;
import yimei.jss.rule.operation.evolved.GPRule;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The main program of job shop scheduling, for basic testing.
 *
 * Created by YiMei on 27/09/16.
 */
public class JSSMain {

    public static void main(String[] args) {
        long start, finish, duration;

        MultiObjectiveFitness fitness = new MultiObjectiveFitness();
        fitness.objectives = new double[1];
        fitness.maxObjective = new double[1];
        fitness.minObjective = new double[1];
        fitness.maximize = new boolean[1];

        long seed = 968356;

        List<Objective> objectives = new ArrayList<>();
//        objectives.add(Objective.MAKESPAN);
//        objectives.add(Objective.MEAN_FLOWTIME);
        objectives.add(Objective.MEAN_FLOWTIME);

        GPRule rule1 = GPRule.readFromLispExpression(RuleType.SEQUENCING, "(* (max (- (* (* (/ SL WKR) (+ W WIQ)) NIQ) (+ TIS (- PT W))) (+ (- WKR NPT) PT)) (* PT (+ (+ (/ (min (+ OWT WINQ) (+ W WIQ)) W) (- PT W)) (- PT W))))");
        AbstractRule rule2 = new FDD(RuleType.SEQUENCING);
        AbstractRule rule3 = new EDD(RuleType.SEQUENCING);
        AbstractRule routingRule = new SPT(RuleType.ROUTING);

//        DynamicSimulation simulation =
//                DynamicSimulation.standardMissing(seed,
//                        rule1, 10, 5000, 1000, 0.85, 4.0);

        StaticInstance instance = StaticInstance.readFromFile("JSS_Data/complete-20_5_0.txt");
        List<Integer> permutation = new ArrayList<>();
        for (int i = 0; i < instance.numWorkCenters; i++) {
            permutation.add(i);
        }
        Collections.shuffle(permutation, new Random(13));

        instance.permutateWorkCenter(permutation);

        System.out.println(instance);

        Simulation simulation = new StaticSimulation(rule1, routingRule, instance);
        List<Simulation> simulations = new ArrayList<>();
        simulations.add(simulation);

        List<Integer> replications = new ArrayList<>();
        replications.add(1);

        SchedulingSet set = new SchedulingSet(simulations,
                replications, objectives);

        SchedulingSet originalSet = set;
        SchedulingSet surrogateSet = originalSet.surrogate(5, 500, 100, objectives);

        set = originalSet;

        start = System.currentTimeMillis();

//        RealMatrix matrix = rule1.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        rule1.calcFitness(fitness, null, set, routingRule, objectives);
        System.out.println("Fitness = " + fitness.fitnessToStringForHumans());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

        start = System.currentTimeMillis();

//        matrix = rule2.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        rule2.calcFitness(fitness, null, set, routingRule, objectives);
        System.out.println("Fitness = " + fitness.fitnessToStringForHumans());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

        start = System.currentTimeMillis();

//        matrix = rule3.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        rule3.calcFitness(fitness, null, set, routingRule, objectives);
        System.out.println("Fitness = " + fitness.fitnessToStringForHumans());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

    }

}
