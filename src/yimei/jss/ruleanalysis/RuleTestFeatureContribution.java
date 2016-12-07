package yimei.jss.ruleanalysis;

import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.feature.ignore.Ignorer;
import yimei.jss.feature.ignore.SimpleIgnorer;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.evolved.GPRule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yimei on 12/10/16.
 */
public class RuleTestFeatureContribution extends RuleTest {

    private String featureSetName;
    private Ignorer ignorer = new SimpleIgnorer();

    public RuleTestFeatureContribution(String trainPath,
                                       RuleType ruleType,
                                       int numRuns,
                                       String testScenario,
                                       String testSetName,
                                       List<Objective> objectives,
                                       String featureSetName) {
        super(trainPath, ruleType, numRuns, testScenario, testSetName, objectives);
        this.featureSetName = featureSetName;
    }

    public RuleTestFeatureContribution(String trainPath,
                                       RuleType ruleType,
                                       int numRuns,
                                       String testScenario,
                                       String testSetName,
                                       String featureSetName) {
        this(trainPath, ruleType, numRuns, testScenario, testSetName, new ArrayList<>(), featureSetName);
    }

    public List<GPNode> featuresFromSetName() {
        List<GPNode> features = new ArrayList<>();

        switch (featureSetName) {
            case "basic-terminals":
                for (JobShopAttribute a : JobShopAttribute.basicAttributes()) {
                    features.add(new AttributeGPNode(a));
                }
                break;
            case "relative-terminals":
                for (JobShopAttribute a : JobShopAttribute.relativeAttributes()) {
                    features.add(new AttributeGPNode(a));
                }
                break;
            default:
                break;
        }

        return features;
    }

    @Override
    public void writeToCSV() {
        SchedulingSet testSet = generateTestSet();
        List<GPNode> features = featuresFromSetName();

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + testSetName + "-feature-contribution.csv");

        double[][] featureContributionMtx = new double[numRuns][features.size()];

        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult result = TestResult.readFromFile(sourceFile, ruleType);

            long start = System.currentTimeMillis();

            GPRule bestRule = (GPRule)result.getBestRule();

            MultiObjectiveFitness allFeaturesFit = new MultiObjectiveFitness();
            allFeaturesFit.objectives = new double[1];
            allFeaturesFit.maxObjective = new double[1];
            allFeaturesFit.minObjective = new double[1];
            allFeaturesFit.maximize = new boolean[1];
            bestRule.calcFitness(allFeaturesFit, null, testSet, objectives);

            for (int j = 0; j < features.size(); j++) {
                GPNode feature = features.get(j);
                MultiObjectiveFitness fit = new MultiObjectiveFitness();
                fit.objectives = new double[1];
                fit.maxObjective = new double[1];
                fit.minObjective = new double[1];
                fit.maximize = new boolean[1];

                GPRule tmpRule = new GPRule((GPTree)(bestRule.getGPTree().clone()));

                tmpRule.ignore(feature, ignorer);
                tmpRule.calcFitness(fit, null, testSet, objectives);

                System.out.format("Run %d, %s: %.2f\n", i, feature.toString(),
                        fit.fitness() - allFeaturesFit.fitness());

                featureContributionMtx[i][j] = fit.fitness() - allFeaturesFit.fitness();
            }

            long finish = System.currentTimeMillis();
            long duration = finish - start;
            System.out.println("Run " + i + ": Duration = " + duration + " ms.");
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            writer.write("Run,Feature,Contribution");
            writer.newLine();
            for (int i = 0; i < numRuns; i++) {
                for (int j = 0; j < features.size(); j++) {
                    writer.write(i + "," + features.get(j).toString() + "," +
                            featureContributionMtx[i][j]);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int idx = 0;
        String trainPath = args[idx];
        idx ++;
        RuleType ruleType = RuleType.get(args[idx]);
        idx ++;
        int numRuns = Integer.valueOf(args[idx]);
        idx ++;
        String testScenario = args[idx];
        idx ++;
        String testSetName = args[idx];
        idx ++;
        int numObjectives = Integer.valueOf(args[idx]);
        idx ++;
        List<Objective> objectives = new ArrayList<>();
        for (int i = 0; i < numObjectives; i++) {
            objectives.add(Objective.get(args[idx]));
            idx ++;
        }
        String featureSetName = String.valueOf(args[idx]);
        idx ++;

        RuleTestFeatureContribution ruleTest = new RuleTestFeatureContribution(trainPath,
                ruleType, numRuns, testScenario, testSetName, objectives, featureSetName);

        ruleTest.writeToCSV();
    }
}
