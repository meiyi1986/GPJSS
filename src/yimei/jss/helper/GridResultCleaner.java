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
    private static final String GRID_PATH = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/grid_results";
    private String dataPath;
    private String outPath;
    private HashMap<String, Integer> benchmarkMakespans;
    private boolean doIncludeGenerations;
    private int numPops;

    public GridResultCleaner(String dirName, int numPops, boolean doIncludeGenerations) {
        this.dataPath = GRID_PATH + "/raw/" + dirName;
        this.numPops = numPops;
        this.outPath = GRID_PATH + "/cleaned/" + dirName;
        this.doIncludeGenerations = doIncludeGenerations;
        benchmarkMakespans = InitBenchmarkMakespans();
    }

    private HashMap<String, Integer> InitBenchmarkMakespans() {
        String homePath = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/";
        String dataPath = homePath + "data/FJSS/";
        List<Objective> objectives = new ArrayList<Objective>();
        objectives.add(Objective.MAKESPAN);
        List<Integer> replications = new ArrayList<Integer>();
        replications.add(new Integer(1));

        List<String> fileNames = FJSSMain.getFileNames(new ArrayList(), Paths.get(dataPath), ".fjs");
        HashMap<String, Integer> makeSpans = new HashMap<String, Integer>();

        for (String fileName: fileNames) {
            List<Simulation> simulations = new ArrayList<Simulation>();
            FlexibleStaticInstance instance = FlexibleStaticInstance.readFromAbsPath(fileName);
            Simulation simulation = new StaticSimulation(null, null, instance);
            simulations.add(simulation);
            SchedulingSet schedulingSet = new SchedulingSet(simulations, replications, objectives);

            int benchmarkMakespan = roundMakespan(schedulingSet.getObjectiveLowerBoundMtx().getData()[0][0]);
            fileName = fileName.substring(dataPath.length());
            makeSpans.put(fileName, benchmarkMakespan);
        }
        return makeSpans;
    }

    public void cleanResults() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dataPath))) {
            for (Path path: stream) {
                if (path.toFile().isDirectory()) {
                    if (path.toString().startsWith(dataPath+"/data-")) {
                        HashMap<Integer, Integer[]> makespans = parseMakespans(path.toString());
                        if (makespans != null) {
                            System.out.println("Creating results file for: "+
                                    path.toString().substring(dataPath.length()+1));
                            createResultFile(path.toString(), makespans);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Integer[]> parseMakespans(String directoryPath) {
        List<String> fileNames = FJSSMain.getFileNames(new ArrayList(), Paths.get(directoryPath), ".stat");
        if (fileNames.isEmpty()) {
            //must not be a diretory for this file
            return null;
        }
        HashMap<Integer, Integer[]> makespans = new HashMap<Integer, Integer[]>();
        //we have a file, and the fitness of all rules evolved from this file is the makespan of that
        //rule divided by the benchmark makespan (which is constant)
        int benchmarkMakespan = roundMakespan(getBenchmarkMakeSpan(directoryPath));

        //iterating through the output from each different seed value
        for (String fileName: fileNames) {
            Double[] fitnesses = GetFitnesses(fileName);

            String fileNumber = fileName.substring(fileName.indexOf("job")+"job.".length());
            int fileNum = Integer.parseInt(fileNumber.substring(0,fileNumber.indexOf(".out.stat")));

            Integer[] fileMakespans = new Integer[fitnesses.length];
            for (int i = 0; i < fitnesses.length; ++i) {
                fileMakespans[i] = roundMakespan(benchmarkMakespan * fitnesses[i]);
            }
            makespans.put(fileNum, fileMakespans);
        }
        return makespans;
    }

    public int roundMakespan(double makespan) {
        //makespans are being calculated by multiplying benchmark by fitness
        //should be extremely close to an integer value
        int makespanInt = (int) Math.round(makespan);
        if (Math.abs(makespanInt - makespan) > 0.0000001) {
            //arbitrary value, but should be very very close
            System.out.println("Why is the value not an integer?");
            return -1;
        }
        return makespanInt;
    }

    public double getBenchmarkMakeSpan(String directoryPath) {
        String fileName = directoryPath.substring(directoryPath.indexOf("data-FJSS-")+"data-FJSS-".length());
        fileName = fileName.replace('-','/');
        fileName = fileName + ".fjs";

        return benchmarkMakespans.getOrDefault(fileName, -1);
    }

    public Double[] GetFitnesses(String fileName) {
        BufferedReader br = null;
        List<Double> bestFitnesses = new ArrayList<Double>();
        try {
            br = new BufferedReader(new FileReader(fileName));
            String sCurrentLine;
            //may be multiple fitnesses per generation if numpops > 1
            Double[] fitnesses = new Double[numPops]; //should be reset every generation
            int numFound = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                if ((sCurrentLine.startsWith("Generation") ||
                        sCurrentLine.startsWith("Best Individual "))
                        && numFound > 0) { //check numFound so won't enter after init
                    //quickly sort the fitnesses - only want lower one (best)
                    Double best = fitnesses[0];
                    if (fitnesses.length == 2) {
                        if (fitnesses[1] < best) {
                            best = fitnesses[1];
                        }
                    }
                    bestFitnesses.add(best);
                    //reset
                    fitnesses = new Double[numPops];
                    numFound = 0;
                }
                else if (sCurrentLine.startsWith("Fitness")) {
                    //line should be in format "Fitness: [0.8386540120793787]"
                    sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf("[")+1, sCurrentLine.length()-1);
                    if (numFound > 1) {
                        System.out.println("");
                    }
                    fitnesses[numFound] = Double.parseDouble(sCurrentLine);
                    numFound++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bestFitnesses.toArray(new Double[0]);
    }

    public void createResultFile(String directoryPath, HashMap<Integer, Integer[]> makespanMap) {
        String outputFileName = directoryPath.substring(dataPath.length()+1+"data-".length())+".csv";
        String csvFile = outPath + "/"+ outputFileName;

        try (FileWriter writer = new FileWriter(csvFile)) {
            //add header first
            List<String> headers = new ArrayList<String>();
            //expecting the same number of generations for all seeds, so just get any value
            Integer[] entry = makespanMap.get(makespanMap.keySet().iterator().next());
            for (int i = 0; i < entry.length-1; ++i) {
                headers.add("Gen"+i);
            }
            headers.add("Best");
            writeLine(writer, headers);

            for (Integer i: makespanMap.keySet()) {
                List<String> makespanCSV = new ArrayList<String>();
                String makeSpansString = "";
                Integer[] makespans = makespanMap.get(i);
                for (Integer makespan: makespans) {
                    makeSpansString += makespan.toString() +",";
                }
                makespanCSV.add(makeSpansString.substring(0, makeSpansString.length()-1));
                writeLine(writer, makespanCSV);
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
        GridResultCleaner grc = new GridResultCleaner("fjss_hardcoded_results_updated", 1, true );
        grc.cleanResults();
    }
}
