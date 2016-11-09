package yimei.jss.analysis;

import yimei.jss.jobshop.Job;
import yimei.jss.rule.basic.FCFS;
import yimei.jss.rule.weighted.WATC;
import yimei.jss.simulation.Simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by yimei on 8/11/16.
 */
public class SimulationJobAnalysis {

    public static void writeJobsToCSV(Simulation simulation, File csvFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            writer.write("Job,NumOps,TotalWork,Weight,ArrivalTime,CompletionTime,DueDate," +
                    "FlowTime,Tardiness,WTardiness");
            writer.newLine();
            for (Job job : simulation.getSystemState().getJobsCompleted()) {
                double flowTime = job.getCompletionTime() - job.getArrivalTime();
                double tardiness = job.getCompletionTime() - job.getDueDate();
                if (tardiness < 0)
                    tardiness = 0;
                double wTardiness = job.getWeight() * tardiness;
                writer.write(job.getId() + "," + job.getOperations().size() + "," +
                        job.getTotalProcTime() + "," +
                        job.getWeight() + "," + job.getArrivalTime() + "," +
                        job.getCompletionTime() + "," + job.getDueDate() + "," +
                        flowTime + "," + tardiness + "," + wTardiness
                );
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Simulation simulation1 = Simulation.standardMissing(5832745,
                new WATC(), 10, 5000, 0, 0.95, 4.0);
        simulation1.run();
        File csvFile1 = new File("jobs-missing-0.9-4-WATC.csv");
        writeJobsToCSV(simulation1, csvFile1);

        Simulation simulation2 = Simulation.standardMissing(5832745,
                new FCFS(), 10, 5000, 0, 0.95, 4.0);
        simulation2.run();
        File csvFile2 = new File("jobs-missing-0.9-4-FCFS.csv");
        writeJobsToCSV(simulation2, csvFile2);
    }
}
