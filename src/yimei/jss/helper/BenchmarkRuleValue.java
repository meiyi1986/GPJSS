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
 * To calcualte the makespan that a given dispatching rule will yield,
 * can multiply that rule's fitness with the benchmark rule's makespan.
 * This class should calculate the makespan of the benchmark rule for all
 * files.
 *
 * Created by dyska on 8/07/17.
 */
public class BenchmarkRuleValue {

    private static final char DEFAULT_SEPARATOR = ',';

    public static void main(String args[]) {
        String homePath = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/";
        String dataPath = homePath + "data/FJSS/";
        List<Objective> objectives = new ArrayList<Objective>();
        objectives.add(Objective.MAKESPAN);
        List<Integer> replications = new ArrayList<Integer>();
        replications.add(new Integer(1));

        List<String> fileNames = FJSSMain.getFileNames(new ArrayList(), Paths.get(dataPath), ".fjs");
        HashMap<String, Double> map = new HashMap<String, Double>();
        for (String fileName: fileNames) {
            List<Simulation> simulations = new ArrayList<Simulation>();
            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName, null);
            Simulation simulation = new StaticSimulation(null, null, instance);
            simulations.add(simulation);
            SchedulingSet schedulingSet = new SchedulingSet(simulations, replications, objectives);

            double makeSpan = schedulingSet.getObjectiveLowerBoundMtx().getData()[0][0];
            fileName = fileName.substring(dataPath.length());
            map.put(fileName, makeSpan);
        }

        //now write to csv file
        String csvFile = homePath+"out/test/benchmarkmakespan.csv";
        try (FileWriter writer = new FileWriter(csvFile)) {
            for (String fileName: map.keySet()) {
                List<String> keyValuePair = new ArrayList<String>();
                keyValuePair.add(fileName);
                keyValuePair.add(map.get(fileName).toString());
                writeLine(writer, keyValuePair);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    All the code below this line is not mine, taken from:
    https://www.mkyong.com/java/how-to-export-data-to-csv-file-java/
     */

    public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());
    }

    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;
    }

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }
}
