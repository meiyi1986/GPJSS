package yimei.jss.helper;

import yimei.jss.FJSSMain;

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
 * Created by dyska on 8/07/17.
 */
public class GridResultCleaner {
    private static final char DEFAULT_SEPARATOR = ',';

    private String dataPath;
    private String outPath;
    private BenchmarkRuleValue benchmarkRuleValues;

    public GridResultCleaner(String dataPath, String outPath) {
        this.dataPath = dataPath;
        this.outPath = outPath;
        this.benchmarkRuleValues = new BenchmarkRuleValue();
    }

    public void cleanResults() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dataPath))) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    if (path.toString().startsWith(dataPath+"/data-")) {
                        System.out.println(path.toString().substring(dataPath.length()+1));
                        createResultFile(path.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createResultFile(String directoryPath) {
        List<String> fileNames = FJSSMain.getFileNames(new ArrayList(), Paths.get(directoryPath), ".stat");
        HashMap<Integer, Double> makeSpans = new HashMap<Integer, Double>();
        double benchmarkMakespan = getMakeSpan(directoryPath);

        for (String fileName: fileNames) {
            double fitness = GetBestFitness(fileName);

            String fileNumber = fileName.substring(fileName.indexOf("job")+"job.".length());
            int fileNum = Integer.parseInt(fileNumber.substring(0,fileNumber.indexOf(".out.stat")));

            double makespan = benchmarkMakespan * fitness;
            makeSpans.put(fileNum, makespan);
        }
        String outputFileName = directoryPath.substring(dataPath.length()+1+"data-".length())+".csv";
        String csvFile = outPath + "/"+ outputFileName;

        try (FileWriter writer = new FileWriter(csvFile)) {
            for (Integer i: makeSpans.keySet()) {
                List<String> keyValuePair = new ArrayList<String>();
                keyValuePair.add(i.toString());
                keyValuePair.add(makeSpans.get(i).toString());
                writeLine(writer, keyValuePair);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getMakeSpan(String directoryPath) {
        HashMap<String, Double> makespans = benchmarkRuleValues.GetMakespans();

        String fileName = directoryPath.substring(directoryPath.indexOf("data-FJSS-")+"data-FJSS-".length());
        fileName = fileName.replace('-','/');
        fileName = fileName + ".fjs";

        return makespans.getOrDefault(fileName, -1.0);
    }

    public static double GetBestFitness(String fileName) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
            String sCurrentLine;
            boolean lastFitness = false;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.startsWith("Best Individual of Run:")) {
                    lastFitness = true;
                }
                if (lastFitness) {
                    if (sCurrentLine.startsWith("Fitness")) {
                        //line should be in format "Fitness: [0.8386540120793787]"
                        sCurrentLine = sCurrentLine.substring(sCurrentLine.indexOf("[")+1, sCurrentLine.length()-1);
                        return Double.parseDouble(sCurrentLine);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
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
                "/Users/dyska/Desktop/Uni/COMP489/GPJSS/out/test");
        grc.cleanResults();
    }
}
