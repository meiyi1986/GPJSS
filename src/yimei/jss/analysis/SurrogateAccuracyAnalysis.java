package yimei.jss.analysis;

import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.basic.*;
import yimei.jss.rule.composite.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yimei on 5/12/16.
 */
public class SurrogateAccuracyAnalysis {

    public static final long simSeed = 31812425;

    private String testScenario;
    private String testSetName;
    private List<Objective> objectives; // The objectives to test.

    private static final List<AbstractRule> rules = new ArrayList<>();

    static {
        rules.add(new SPT());
        rules.add(new EDD());
        rules.add(new FCFS());
        rules.add(new LWKR());
        rules.add(new CR());
        rules.add(new SL());
        rules.add(new PW());
        rules.add(new ATC());
        rules.add(new LPT());
        rules.add(new FDD());
        rules.add(new LCFS());
        rules.add(new MWKR());
        rules.add(new AVPRO());
        rules.add(new MOPNR());
        rules.add(new Slack());
        rules.add(new COVERT());

        rules.add(new OPFSLKperPT());
        rules.add(new LWKRplusPT());
        rules.add(new CRplusPT());
        rules.add(new PTplusPW());
        rules.add(new PTplusPWplusFDD());
        rules.add(new SlackperOPN());
        rules.add(new SlackperRPT());
        rules.add(new PTplusWINQ());
        rules.add(new TwoPTplusWINQplusNPT());
    }

    public static void main(String[] args) {
        int idx = 0;
        String scenario = args[idx];
        idx ++;
        String setName = args[idx];
        idx ++;
        int numObjectives = Integer.valueOf(args[idx]);
        idx ++;
        List<Objective> objectives = new ArrayList<>();
        for (int i = 0; i < numObjectives; i++) {
            objectives.add(Objective.get(args[idx]));
            idx ++;
        }

        SchedulingSet set = SchedulingSet.generateSet(simSeed,
                scenario, setName, objectives, 50);

        SchedulingSet surrogateSet = set.surrogate(5, 500, 100, objectives);
        List<Integer> reps = new ArrayList<>();
        reps.add(1);
        surrogateSet.setReplications(reps);

        MultiObjectiveFitness fitness = new MultiObjectiveFitness();
        fitness.objectives = new double[1];
        fitness.maxObjective = new double[1];
        fitness.minObjective = new double[1];
        fitness.maximize = new boolean[1];

        for (AbstractRule rule : rules) {
            rule.calcFitness(fitness, null, surrogateSet, objectives);
            double surrogateFit = fitness.fitness();
            rule.calcFitness(fitness, null, set, objectives);
            double fit = fitness.fitness();

            System.out.println(rule.getName() + "\t " + surrogateFit + "\t " + fit);
        }
    }
}
