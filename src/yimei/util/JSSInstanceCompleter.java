package yimei.util;

import yimei.jss.jobshop.StaticInstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yimei on 29/11/16.
 */
public class JSSInstanceCompleter {

    public static void completeStaticInstance(String fileName) {
        StaticInstance instance = readFromFile(fileName);

        String projPath = (new File("")).getAbsolutePath();
        File outFile = new File(projPath + "/data/complete-" + fileName);

        instance.printToFile(outFile);
    }

    public static StaticInstance readFromFile(String fileName) {
        String projPath = (new File("")).getAbsolutePath();
        File datafile = new File(projPath + "/data/" + fileName);

        return readFromFile(datafile);
    }

    public static StaticInstance readFromFile(File file) {
        StaticInstance instance = null;

        String line;
        String[] segments;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Read the number of jobs and machines
            line = br.readLine();
            segments = line.split("\\s+");
            int numJobs = Integer.valueOf(segments[0]);
            int numWorkCenters = Integer.valueOf(segments[1]);

            instance = new StaticInstance(numWorkCenters, numJobs);

            // Read the jobs
            List<StaticInstance.JobInformation> jobInformations = new ArrayList<>();
            for (int j = 0; j < numJobs; j++) {
                line = br.readLine();
                segments = line.split("\\s+");

                System.out.println(line);

                double arrivalTime = 0;
                double totalProcTime = 0;
                double weight = 1;
                int numOps = numWorkCenters;
                List<Integer> route = new ArrayList<>();
                List<Double> procTimes = new ArrayList<>();

                for (int i = 0; i < numWorkCenters; i++) {
                    int wcid = Integer.valueOf(segments[2 * i + 1]);
                    double procTime = Double.valueOf(segments[2 * i + 2]);
                    totalProcTime += procTime;

                    route.add(wcid);
                    procTimes.add(procTime);
                }

                double dueDate = arrivalTime + 4 * totalProcTime;

                instance.addJob(arrivalTime, dueDate, weight, numOps, route, procTimes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instance;
    }

    public static void main(String[] args) {
        completeStaticInstance("20_5_0.txt");
    }
}
