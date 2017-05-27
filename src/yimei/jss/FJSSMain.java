package yimei.jss;

import ec.multiobjective.MultiObjectiveFitness;
import org.apache.commons.math3.analysis.function.Abs;
import yimei.jss.jobshop.*;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.basic.*;
import yimei.jss.rule.composite.TwoPTplusWINQplusNPT;
import yimei.jss.rule.evolved.GPRule;
import yimei.jss.rule.weighted.WATC;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

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
            fitness.minObjective = new double[1];
            fitness.maximize = new boolean[1];

            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName, routingRule);

            List<Integer> replications = new ArrayList<>();
            replications.add(1);

            //use first rule in sequencing rules to build simulation
            Simulation simulation = new StaticSimulation(sequencingRules.get(0), routingRule, instance);
            List<Simulation> simulations = new ArrayList<>();
            simulations.add(simulation);

            SchedulingSet set = new SchedulingSet(simulations, replications, objectives);

            for (AbstractRule sequencingRule: sequencingRules) {
                String fitnessResult = calcFitness(sequencingRule, fitness, set, objectives);

                //store fitness result with sequencing rule and dispatching rule
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

    private static String calcFitness(AbstractRule rule, MultiObjectiveFitness fitness,
                                    SchedulingSet set, List<Objective> objectives) {
        String output = "";
        long start = System.currentTimeMillis();

        rule.calcFitness(fitness, null, set, objectives);
        output = "Fitness = " + fitness.fitnessToStringForHumans();
        System.out.println(output);

        long finish = System.currentTimeMillis();

        long duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");
        return output;
    }

    public static List<String> getFileNames(List<String> fileNames, Path dir) {
        if (dir.toAbsolutePath().toString().endsWith(".fjs")) {
            //have been passed a file
            fileNames.add(dir.toString());
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path path : stream) {
                    if (path.toFile().isDirectory()) {
                        getFileNames(fileNames, path);
                    } else {
                        if (path.toString().endsWith(".fjs")) {
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
        //example file path: Hurink_Data/Text/sdata/la10.fjs
        String path = "";
        if (args.length > 0) {
            //allow more specific folder or file paths to be used
            path = args[0];
        }
        path = (new File("")).getAbsolutePath() + "/data/FJSS/" + path;

        boolean doStore = true;
        List<Objective> objectives = new ArrayList<>();
        List<AbstractRule> sequencingRules = new ArrayList();
        List<AbstractRule> dispatchingRules = new ArrayList();

        objectives.add(Objective.MAKESPAN);

        //sequencingRules.add(GPRule.readFromLispExpression(
                //"(* (max (- (* (* (/ SL WKR) (+ W WIQ)) NIQ) (+ TIS (- PT W))) (+ (- WKR NPT) PT)) (* PT (+ (+ (/ (min (+ OWT WINQ) (+ W WIQ)) W) (- PT W)) (- PT W))))")
        //);
        //sequencingRules.add(new FDD());
        sequencingRules.add(new EDD());

        //dispatchingRules.add(new FCFS());
        dispatchingRules.add(new SPT());

        //There is some randomness component - seed should be set
        //We get same result every time we run the whole thing, but if the same instance
        //is run multiple times it changes

        List<String> fileNames = getFileNames(new ArrayList(), Paths.get(path));
        for (int i = 0; i < fileNames.size(); ++i) {
            String fileName = fileNames.get(i);
            System.out.println("\nInstance "+(i+1)+" - Path: "+fileName);
            calculateFitness(doStore, fileName, objectives, sequencingRules, dispatchingRules);
        }

    }
}