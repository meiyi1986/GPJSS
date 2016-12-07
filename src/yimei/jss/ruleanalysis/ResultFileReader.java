package yimei.jss.ruleanalysis;

import ec.Fitness;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import yimei.jss.rule.evolved.GPRule;
import yimei.util.lisp.LispSimplifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The reader of the result file.
 *
 * Created by YiMei on 12/10/16.
 */
public class ResultFileReader {

    public static TestResult readTestResultFromFile(File file,
                                                    RuleType ruleType,
                                                    boolean isMultiObjective) {
        TestResult result = new TestResult();

        String line;
        Fitness fitness = null;
        GPRule rule = null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (!(line = br.readLine()).equals("Best Individual of Run:")) {
                if (line.startsWith("Generation")) {
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    line = br.readLine();
                    fitness = readFitnessFromLine(line, isMultiObjective);
                    br.readLine();
                    String expression = br.readLine();

                    expression = LispSimplifier.simplifyExpression(expression);

                    rule = GPRule.readFromLispExpression(expression);
                    result.addGenerationalRule(rule);
                    result.addGenerationalTrainFitness(fitness);
                    result.addGenerationalValidationFitnesses((Fitness)fitness.clone());
                    result.addGenerationalTestFitnesses((Fitness)fitness.clone());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set the best rule as the rule in the last generation
        result.setBestRule(rule);
        result.setBestTrainingFitness(fitness);

        return result;
    }

    private static Fitness readFitnessFromLine(String line, boolean isMultiobjective) {
        if (isMultiobjective) {
            // TODO read multi-objective fitness line
            String[] spaceSegments = line.split("\\s+");
            String[] equation = spaceSegments[1].split("=");
            double fitness = Double.valueOf(equation[1]);
            KozaFitness f = new KozaFitness();
            f.setStandardizedFitness(null, fitness);

            return f;
        }
        else {
            String[] spaceSegments = line.split("\\s+");
            String[] fitVec = spaceSegments[1].split("\\[|\\]");
            double fitness = Double.valueOf(fitVec[1]);
            MultiObjectiveFitness f = new MultiObjectiveFitness();
            f.objectives = new double[1];
            f.objectives[0] = fitness;

            return f;
        }
    }

    public static DescriptiveStatistics readTimeFromFile(File file) {
        DescriptiveStatistics generationalTimeStat = new DescriptiveStatistics();

        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while(true) {
                line = br.readLine();

                if (line == null)
                    break;

                String[] commaSegments = line.split(",");
                generationalTimeStat.addValue(Double.valueOf(commaSegments[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return generationalTimeStat;
    }

    public static List<String> readLispExpressionFromFile(File file,
                                                          RuleType ruleType,
                                                          boolean isMultiObjective) {
        List<String> expressions = new ArrayList<>();

        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (!(line = br.readLine()).equals("Best Individual of Run:")) {
                if (line.startsWith("Generation")) {
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    String expression = br.readLine();

                    expression = LispSimplifier.simplifyExpression(expression);
                    expressions.add(expression);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return expressions;
    }
}
