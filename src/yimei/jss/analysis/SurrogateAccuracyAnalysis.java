package yimei.jss.analysis;

import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.RuleType;
import yimei.jss.rule.operation.basic.*;
import yimei.jss.rule.operation.composite.*;

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
        rules.add(new SPT(RuleType.SEQUENCING));
        rules.add(new EDD(RuleType.SEQUENCING));
        rules.add(new FCFS(RuleType.SEQUENCING));
        rules.add(new LWKR(RuleType.SEQUENCING));
        rules.add(new CR(RuleType.SEQUENCING));
        rules.add(new SL(RuleType.SEQUENCING));
        rules.add(new PW(RuleType.SEQUENCING));
        rules.add(new ATC(RuleType.SEQUENCING));
        rules.add(new LPT(RuleType.SEQUENCING));
        rules.add(new FDD(RuleType.SEQUENCING));
        rules.add(new LCFS(RuleType.SEQUENCING));
        rules.add(new MWKR(RuleType.SEQUENCING));
        rules.add(new AVPRO(RuleType.SEQUENCING));
        rules.add(new MOPNR(RuleType.SEQUENCING));
        rules.add(new Slack(RuleType.SEQUENCING));
        rules.add(new COVERT(RuleType.SEQUENCING));

        rules.add(new OPFSLKperPT(RuleType.SEQUENCING));
        rules.add(new LWKRplusPT(RuleType.SEQUENCING));
        rules.add(new CRplusPT(RuleType.SEQUENCING));
        rules.add(new PTplusPW(RuleType.SEQUENCING));
        rules.add(new PTplusPWplusFDD(RuleType.SEQUENCING));
        rules.add(new SlackperOPN(RuleType.SEQUENCING));
        rules.add(new SlackperRPT(RuleType.SEQUENCING));
//        rules.add(new PTplusWINQ(RuleType.SEQUENCING));
//        rules.add(new TwoPTplusWINQplusNPT(RuleType.SEQUENCING));
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
            //rule.calcFitness(fitness, null, surrogateSet, objectives);
            double surrogateFit = fitness.fitness();
            //rule.calcFitness(fitness, null, set, objectives);
            double fit = fitness.fitness();

            System.out.println(rule.getName() + "\t " + surrogateFit + "\t " + fit);
        }
    }
}
