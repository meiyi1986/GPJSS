package yimei.jss.helper;

import yimei.jss.FJSSMain;
import yimei.jss.jobshop.FlexibleStaticInstance;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * To calculate the makespan that a given dispatching rule will yield,
 * can multiply that rule's fitness with the benchmark rule's makespan.
 * This class should calculate the makespan of the benchmark rule for all
 * files.
 *
 * Created by dyska on 8/07/17.
 */
public class BenchmarkRuleValue {

    private HashMap<String, Double> makeSpans;

    public BenchmarkRuleValue() {
        this.makeSpans = InitMakespans();
    }

    public HashMap<String, Double> GetMakespans() {
        return makeSpans;
    }

    private HashMap<String, Double> InitMakespans() {
        String homePath = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/";
        String dataPath = homePath + "data/FJSS/";
        List<Objective> objectives = new ArrayList<Objective>();
        objectives.add(Objective.MAKESPAN);
        List<Integer> replications = new ArrayList<Integer>();
        replications.add(new Integer(1));

        List<String> fileNames = FJSSMain.getFileNames(new ArrayList(), Paths.get(dataPath), ".fjs");
        HashMap<String, Double> makeSpans = new HashMap<String, Double>();
        for (String fileName: fileNames) {
            List<Simulation> simulations = new ArrayList<Simulation>();
            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName, null);
            Simulation simulation = new StaticSimulation(null, null, instance);
            simulations.add(simulation);
            SchedulingSet schedulingSet = new SchedulingSet(simulations, replications, objectives);

            double makeSpan = schedulingSet.getObjectiveLowerBoundMtx().getData()[0][0];
            fileName = fileName.substring(dataPath.length());
            makeSpans.put(fileName, makeSpan);
        }
        return makeSpans;
    }


}
