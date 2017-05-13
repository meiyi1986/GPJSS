package yimei.jss;

import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.*;
import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.basic.*;
import yimei.jss.rule.composite.TwoPTplusWINQplusNPT;
import yimei.jss.rule.evolved.GPRule;
import yimei.jss.rule.weighted.WATC;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * The main program of job shop scheduling, for basic testing.
 *
 * Created by YiMei on 27/09/16.
 */
public class FJSSMain {

    public static void calculateFitness(List<Objective> objectives,
                                        GPRule sequencingRule1, AbstractRule sequencingRule2,
                                        AbstractRule sequencingRule3, FlexibleStaticInstance instance) {
        long start, finish, duration;
        MultiObjectiveFitness fitness = new MultiObjectiveFitness();
        fitness.objectives = new double[1];
        fitness.maxObjective = new double[1];
        fitness.minObjective = new double[1];
        fitness.maximize = new boolean[1];

        //System.out.println(instance);

        Simulation simulation = new StaticSimulation(sequencingRule1, instance);
        List<Simulation> simulations = new ArrayList<>();
        simulations.add(simulation);

        List<Integer> replications = new ArrayList<>();
        replications.add(1);

        SchedulingSet set = new SchedulingSet(simulations,
                replications, objectives);

        SchedulingSet originalSet = set;
        //SchedulingSet surrogateSet = originalSet.surrogate(5, 500, 100, objectives);

        set = originalSet;

        start = System.currentTimeMillis();

//        RealMatrix matrix = rule1.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        sequencingRule1.calcFitness(fitness, null, set, objectives);
        System.out.println("Fitness = " + fitness.fitnessToStringForHumans());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

        start = System.currentTimeMillis();

//        matrix = rule2.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        sequencingRule2.calcFitness(fitness, null, set, objectives);
        System.out.println("Fitness = " + fitness.fitnessToStringForHumans());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

        start = System.currentTimeMillis();

//        matrix = rule3.objectiveValueMatrix(schedulingSet, objectives);
//        System.out.println(matrix);

        sequencingRule3.calcFitness(fitness, null, set, objectives);
        System.out.println("Fitness = " + fitness.fitnessToStringForHumans());

        finish = System.currentTimeMillis();

        duration = finish - start;

        System.out.println("Duration = " + duration + " ms.");

    }

    private static List<String> getFileNames(List<String> fileNames, Path dir) {
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
        String path = "";
        //path may be a directory path or a file path
        //example file path: Hurink_Data/Text/sdata/la10.fjs
        if (args.length > 0) {
            //allow more specific folder or file paths to be used
            path = args[0];
        }
        path = (new File("")).getAbsolutePath() + "/data/FJSS/" + path;

        List<Objective> objectives = new ArrayList<>();
        objectives.add(Objective.MAKESPAN);
        //objectives.add(Objective.MEAN_FLOWTIME);
        //objectives.add(Objective.MEAN_FLOWTIME);

        GPRule rule1 = GPRule.readFromLispExpression("(* (max (- (* (* (/ SL WKR) (+ W WIQ)) NIQ) (+ TIS (- PT W))) (+ (- WKR NPT) PT)) (* PT (+ (+ (/ (min (+ OWT WINQ) (+ W WIQ)) W) (- PT W)) (- PT W))))");
        AbstractRule rule2 = new FDD();
        AbstractRule rule3 = new EDD();

        //the fitness is deterministic once the rule has been decided
        List<String> fileNames = getFileNames(new ArrayList<String>(), Paths.get(path));
        for (int i = 0; i < fileNames.size(); ++i) {
            String fileName = fileNames.get(i);
            System.out.println("\nInstance "+(i+1)+" - Path: "+fileName);
            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName, new LPT());
            calculateFitness(objectives, rule1, rule2, rule3, instance);
        }
    }

}
