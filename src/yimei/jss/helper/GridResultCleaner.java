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
    public static String dataPath = "/Users/dyska/Desktop/Uni/COMP489/fjss-hardcoded-results";
    public static String outPath = "/Users/dyska/Desktop/Uni/COMP489/GPJSS/out/test";

    public static void main(String args[]) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dataPath))) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    //getFileNames(fileNames, path);
                    if (path.toString().startsWith(dataPath+"/data-")) {
                        //System.out.println(path.toString().substring(dataPath.length()+1));
                        createResultFile(path.toString());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createResultFile(String directoryPath) {
        List<String> fileNames = FJSSMain.getFileNames(new ArrayList(), Paths.get(directoryPath), ".stat");
        HashMap<Integer, Double> fitnesses = new HashMap<Integer, Double>();
        for (String fileName: fileNames) {
            double fitness = GetBestFitness(fileName);
            if (fitness == -1) {
                System.out.println("Something failed...");
            }
            String fileNumber = fileName.substring(fileName.indexOf("job")+"job.".length());
            int fileNum = Integer.parseInt(fileNumber.substring(0,fileNumber.indexOf(".out.stat")));

            //System.out.println(fileNum+": "+fitness);

            if (fitnesses.containsKey(fileNum)) {
                System.out.println("Not sure why this is happening...");
            }
            fitnesses.put(fileNum, fitness);
        }
        // /Users/dyska/Desktop/Uni/COMP489/fjss-hardcoded-results/data-FJSS-Barnes-Text-mt10c1
        String outputFileName = directoryPath.substring(dataPath.length()+1+"data-".length())+".csv";
        String csvFile = outPath + "/"+ outputFileName;

        try (FileWriter writer = new FileWriter(csvFile)) {
            for (Integer i: fitnesses.keySet()) {
                List<String> keyValuePair = new ArrayList<String>();
                keyValuePair.add(i.toString());
                keyValuePair.add(fitnesses.get(i).toString());
                BenchmarkRuleValue.writeLine(writer, keyValuePair);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
