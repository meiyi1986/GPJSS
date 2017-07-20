package yimei.jss;

import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.*;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.operation.basic.*;
import yimei.jss.rule.operation.composite.*;
import yimei.jss.rule.operation.evolved.GPRule;
import yimei.jss.rule.operation.weighted.*;
import yimei.jss.rule.workcenter.basic.*;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static yimei.jss.ruleevaluation.RuleComparison.EvaluateOutput;

/**
 * The main program of job shop scheduling, for basic testing.
 *
 * Created by YiMei on 27/09/16.
 */
public class FJSSMain {

    private static void calculateFitness(boolean doStore, String fileName,
                                        List<Objective> objectives,
                                        List<AbstractRule> sequencingRules,
                                        List<AbstractRule> routingRules) {
        BufferedWriter writer = null;

        if (sequencingRules.size() == 0 || routingRules.size() == 0 || objectives.size() == 0) {
            return;
        }

        if (doStore) {
            writer = createFileWriter(fileName);
        }

        for (AbstractRule routingRule: routingRules) {
            MultiObjectiveFitness fitness = new MultiObjectiveFitness();
            fitness.objectives = new double[1];
            fitness.maxObjective = new double[1];
            fitness.maxObjective[0] = 1.0;
            fitness.minObjective = new double[1];
            fitness.maximize = new boolean[1];

            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName, routingRule);

            List<Integer> replications = new ArrayList<>();
            replications.add(1);

            List<Simulation> simulations = new ArrayList<>();
            //use first rule in sequencing rules to build simulation
            simulations.add(new StaticSimulation(sequencingRules.get(0), routingRule, instance));

            SchedulingSet set = new SchedulingSet(simulations, replications, objectives);

            for (AbstractRule sequencingRule: sequencingRules) {
                String fitnessResult = calcFitness(doStore, sequencingRule, fitness, set, objectives);

                //store fitness result with sequencing rule and routing rule
                if (doStore) {
                    try {
                        writer.write(String.format("RR:%s SR:%s - %s", getRuleName(routingRule),
                                getRuleName(sequencingRule), fitnessResult));
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (doStore) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static BufferedWriter createFileWriter(String filePath) {
        //replace /data/ with /out/test/
        String[] pathComponents = filePath.split("/data/");
        //to avoid heavy nesting in output, replace nested directory with filenames

        String output = pathComponents[0] + "/out/test/" + pathComponents[1].replace("/","-");

        File file = new File(output);
        try {
            //create file in this location if one does not exist
            if (!file.exists()) {
                file.createNewFile();
            }
            return new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getRuleName(AbstractRule rule) {
        if (rule instanceof GPRule) {
            //should return full rule, not just "GPRule"
            return ((GPRule) rule).getLispString();
        }
        return rule.getName();
    }

    private static String calcFitness(boolean doStore, AbstractRule rule, MultiObjectiveFitness fitness,
                                    SchedulingSet set, List<Objective> objectives) {
        String output = "";

        rule.calcFitness(fitness, null, set, objectives);
        if (!doStore) {
            double benchmarkMakeSpan = set.getObjectiveLowerBound(0,0);
            output += "Benchmark makespan: "+benchmarkMakeSpan+"\n";
            double ruleMakespan = fitness.fitness()*benchmarkMakeSpan;
            output += "Rule makespan: "+ruleMakespan+"\n";
        }

        output += "Fitness = " + fitness.fitnessToStringForHumans();
        System.out.println(output);
        return output;
    }

    public static List<String> getFileNames(List<String> fileNames, Path dir, String ext) {
        if (dir.toAbsolutePath().toString().endsWith(ext)) {
            //have been passed a file
            fileNames.add(dir.toString());
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path path : stream) {
                    if (path.toFile().isDirectory()) {
                        getFileNames(fileNames, path, ".fjs");
                    } else {
                        if (path.toString().endsWith(ext)) {
                            fileNames.add(path.toString());
                        }
                    }
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return fileNames;
    }

    public static void main(String[] args) {
        //path may be a directory path or a file path
        //example file path: Brandimarte_Data/Text/Mk01.fjs
        String path = "";
        if (args.length > 0) {
            //allow more specific folder or file paths to be used
            path = args[0];
        }
        path = (new File("")).getAbsolutePath() + "/data/FJSS/" + path;

        boolean doStore = false;
        List<Objective> objectives = new ArrayList<>();
        List<AbstractRule> sequencingRules = new ArrayList();
        List<AbstractRule> routingRules = new ArrayList();

        objectives.add(Objective.MAKESPAN);

        //we are evolving sequencing rules, hard-coding routing rules

        sequencingRules.add(GPRule.readFromLispExpression(" (min (+ (* (- TIS SL) (+ PT rDD))" +
                " (* (max (* (/ NWT PT) WIQ) (* PT (- SL NIQ))) (/ (+ NWT NPT) (- WKR NPT))))" +
                " (+ (/ (+ NWT NPT) (- WKR NPT)) WIQ))"));

        routingRules.add(new SBT());

//        sequencingRules.add(new FDD());
//        sequencingRules.add(new LCFS());
//        sequencingRules.add(new LPT());
//        sequencingRules.add(new LWKR());
//        sequencingRules.add(new MOPNR());
//        sequencingRules.add(new MWKR());
//        sequencingRules.add(new NPT());
//        sequencingRules.add(new PW());
//        sequencingRules.add(new SL());
//        sequencingRules.add(new Slack());
//        sequencingRules.add(new SPT());
//        sequencingRules.add(new ATC());
//        sequencingRules.add(new COVERT());
//        sequencingRules.add(new CRplusPT());
//        sequencingRules.add(new LWKRplusPT());
//        sequencingRules.add(new OPFSLKperPT());
//        sequencingRules.add(new PTplusPW());
//        sequencingRules.add(new PTplusPWplusFDD());
//        sequencingRules.add(new SlackperOPN());
//        sequencingRules.add(new SlackperRPTplusPT());
//        sequencingRules.add(new WATC());
//        sequencingRules.add(new WCOVERT());
//        sequencingRules.add(new WSPT());
//
//        //add work center specific rules, as other rules will always give the same values
//        routingRules.add(new LBT());
//        routingRules.add(new LRT());
//        routingRules.add(new NIQ());
//        routingRules.add(new SBT());
//        routingRules.add(new SRT());
//        routingRules.add(new WIQ());

        List<String> fileNames = getFileNames(new ArrayList(), Paths.get(path), ".fjs");
        for (int i = 0; i < fileNames.size(); ++i) {
            String fileName = fileNames.get(i);
            System.out.println("\nInstance "+(i+1)+" - Path: "+fileName);
            calculateFitness(doStore, fileName, objectives, sequencingRules, routingRules);
        }

        //EvaluateOutput("/out/test/", "RR");
    }
}