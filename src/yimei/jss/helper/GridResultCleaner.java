package yimei.jss.helper;

import yimei.jss.FJSSMain;
import yimei.jss.jobshop.FlexibleStaticInstance;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Have results from the jobs run on the grid. There are 30 result files per
 * instance file, and we only need the best fitness from each result file. This
 * program should output a csv file containing the best fitness from each of the
 * 30 result files for each instance.
 *
 * We have 30 output files for each instance, and 50 generations per output file,
 * plus a 'best individual of run' output.
 *
 * For each generation/best, we have a fitness and the rule itself.
 * We SHOULD be able to derive makespan from the fitness, by calculating the benchmark makespan
 * that would have veeb used. We could also parse the rule(s) and re-calculate the makespan
 * from this. Once we have results back from the grid, can verify this, but calculating from
 * benchmark is definitely simpler.
 *
 * When looking through grid results, two main scenarios.
 * Either we only care about the best makespan for each file, or we also care about the
 * best makespan for each generation.
 *
 * May as well combine the two and store the best makespan from each generation,
 * plus the best makespan of any rule. Should store each of these on the same row,
 * and have 30 rows.
 *
 * Created by dyska on 8/07/17.
 */
public class GridResultCleaner {
    private static final char DEFAULT_SEPARATOR = ',';
    private String dataPath;
    private String outPath;
    private HashMap<String, Double> benchmarkMakespans;
    private boolean doIncludeGenerations;

    public GridResultCleaner(String dataPath, String outPath, boolean doIncludeGenerations) {
        this.dataPath = dataPath;
        this.outPath = outPath;
        this.doIncludeGenerations = doIncludeGenerations;
        benchmarkMakespans = InitBenchmarkMakespans();
    }

    private HashMap<String, Double> InitBenchmarkMakespans() {
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
            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName);
            Simulation simulation = new StaticSimulation(null, null, instance);
            simulations.add(simulation);
            SchedulingSet schedulingSet = new SchedulingSet(simulations, replications, objectives);

            double makeSpan = schedulingSet.getObjectiveLowerBoundMtx().getData()[0][0];
            fileName = fileName.substring(dataPath.length());
            makeSpans.put(fileName, makeSpan);
        }
        return makeSpans;
    }

    public void cleanResults() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dataPath))) {
            for (Path path: stream) {
                if (path.toFile().isDirectory()) {
                    if (path.toString().startsWith(dataPath+"/data-")) {
                        System.out.println(path.toString().substring(dataPath.length()+1));
                        HashMap<Integer, Double[]> makespans = parseMakespans(path.toString());
                        createResultFile(path.toString(), makespans);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Double[]> parseMakespans(String directoryPath) {
        List<String> fileNames = FJSSMain.getFileNames(new ArrayList(), Paths.get(directoryPath), ".stat");
        HashMap<Integer, Double[]> makespans = new HashMap<Integer, Double[]>();
        //we have a file, and the fitness of all rules evolved from this file is the makespan of that
        //rule divided by the benchmark makespan (which is constant)
        double benchmarkMakespan = getBenchmarkMakeSpan(directoryPath);

        //iterating through the output from each different seed value
        for (String fileName: fileNames) {
            Double[] fitnesses = GetFitnesses(fileName);

            String fileNumber = fileName.substring(fileName.indexOf("job")+"job.".length());
            int fileNum = Integer.parseInt(fileNumber.substring(0,fileNumber.indexOf(".out.stat")));

//            Double[] fileMakespans = new Double[fitnesses.length];
//            for (int i = 0; i < fitnesses.length; ++i) {
//                fileMakespans[i] = benchmarkMakespan * fitnesses[i];
//            }
            //makespans.put(fileNum, fileMakespans);
            makespans.put(fileNum, fitnesses);
        }
        return makespans;
    }

    public double getBenchmarkMakeSpan(String directoryPath) {
        String fileName = directoryPath.substring(directoryPath.indexOf("data-FJSS-")+"data-FJSS-".length());
        fileName = fileName.replace('-','/');
        fileName = fileName + ".fjs";

        return benchmarkMakespans.getOrDefault(fileName, -1.0);
    }

    public static Double[] GetFitnesses(String fileName) {
        BufferedReader br = null;
        List<Double> fitnesses = new ArrayList<Double>();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String sCurrentLine;
            boolean lastFitness = false;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.startsWith("Fitness")) {
                    //line should be in format "Fitness: [0.8386540120793787]"
                    sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf("[")+1, sCurrentLine.length()-1);
                    fitnesses.add(Double.parseDouble(sCurrentLine));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fitnesses.toArray(new Double[0]);
    }

    public void createResultFile(String directoryPath, HashMap<Integer, Double[]> makespanMap) {
        String outputFileName = directoryPath.substring(dataPath.length()+1+"data-".length())+".csv";
        String csvFile = outPath + "/"+ outputFileName;

        try (FileWriter writer = new FileWriter(csvFile)) {
            //add header first
            List<String> headers = new ArrayList<String>();
            headers.add("Seed");
            headers.add("Makespan");
            writeLine(writer, headers);

            for (Integer i: makespanMap.keySet()) {
                List<String> keyValuePair = new ArrayList<String>();
                keyValuePair.add(i.toString());
                String makeSpansString = "";
                Double[] makespans = makespanMap.get(i);
                for (Double makespan: makespans) {
                    makeSpansString += makespan.toString() +",";
                }
                keyValuePair.add(makeSpansString.substring(0, makeSpansString.length()-1));
                writeLine(writer, keyValuePair);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    * All the code below this line is not mine, taken from:
    * https://www.mkyong.com/java/how-to-export-data-to-csv-file-java/
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

    public static void main(String args[]) {
        GridResultCleaner grc = new GridResultCleaner("/Users/dyska/Desktop/Uni/COMP489/fjss-hardcoded-results",
                "/Users/dyska/Desktop/Uni/COMP489/GPJSS/out/grid_results", true);
        grc.cleanResults();
    }
}
