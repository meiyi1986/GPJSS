package yimei.jss;

import ec.gp.koza.KozaFitness;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Schedule;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.basic.EDD;
import yimei.jss.rule.basic.FCFS;
import yimei.jss.rule.basic.SPT;
import yimei.jss.rule.evolved.GPRule;
import yimei.jss.rule.weighted.WATC;
import yimei.jss.rule.weighted.WSPT;
import yimei.jss.simulation.Simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * The main program of job shop scheduling, for basic testing.
 *
 * Created by YiMei on 27/09/16.
 */
public class JSSMain {

    public static void main(String[] args) {
        long start, finish, duration;

        KozaFitness fitness = new KozaFitness();

        long seed = 968356;

        List<Objective> objectives = new ArrayList<>();
//        objectives.add(Objective.MAKESPAN);
//        objectives.add(Objective.MEAN_FLOWTIME);
        objectives.add(Objective.MEAN_WEIGHTED_TARDINESS);

        GPRule rule1 = GPRule.readFromLispExpression("(* (max (- (* (* (/ SL WKR) (+ W WIQ)) NIQ) (+ TIS (- PT W))) (+ (- WKR NPT) PT)) (* PT (+ (+ (/ (min (+ OWT WINQ) (+ W WIQ)) W) (- PT W)) (- PT W))))");
        AbstractRule rule2 = new WATC();
        AbstractRule rule3 = new WSPT();

        Simulation simulation =
                Simulation.standardMissing(seed,
                        rule1, 10, 5000, 1000, 0.85, 4.0);

        List<Simulation> simulations = new ArrayList<>();
        simulations.add(simulation);

        List<Integer> replications = new ArrayList<>();
        replications.add(50);

        SchedulingSet set = new SchedulingSet(simulations,
                replications, objectives);

        SchedulingSet originalSet = set;
        SchedulingSet surrogateSet = originalSet.surrogate(5, 500, 100, objectives);

        set = originalSet;

        start = System.currentTimeMillis();

//        RealMatrix matrix = rule1.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        rule1.calcFitness(fitness, null, set, objectives);
        System.out.println("Koza Fitness = " + ((KozaFitness)fitness).standardizedFitness());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

        start = System.currentTimeMillis();

//        matrix = rule2.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        rule2.calcFitness(fitness, null, set, objectives);
        System.out.println("Koza Fitness = " + ((KozaFitness)fitness).standardizedFitness());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

        start = System.currentTimeMillis();

//        matrix = rule3.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        rule3.calcFitness(fitness, null, set, objectives);
        System.out.println("Koza Fitness = " + ((KozaFitness)fitness).standardizedFitness());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

    }

}
