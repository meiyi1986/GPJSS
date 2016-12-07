package yimei.jss.jobshop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class for the job shop.
 * Created by yimei on 22/09/16.
 */
public class Shop {

    private List<Job> jobs;
    private List<WorkCenter> workCenters;

    public Shop() {
        this.jobs = new ArrayList<>();
        this.workCenters = new ArrayList<>();
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Job getJob(int idx) {
        return jobs.get(idx);
    }

    public List<WorkCenter> getWorkCenters() {
        return workCenters;
    }

    public WorkCenter getWorkCenter(int idx) {
        return workCenters.get(idx);
    }

    public void addJob(Job job) {
        jobs.add(job);
    }

    public void addWorkCenter(WorkCenter workCenter) {
        workCenters.add(workCenter);
    }

    public int numJobs() {
        return jobs.size();
    }

    public int numWorkCenters() {
        return workCenters.size();
    }

    public String toString() {
        String str = "";
        for (Job job : jobs) {
            str += job.toString() + "\n";
        }

        return str;
    }

    public static Shop readFromFile(String fileName) {
        String projPath = (new File("")).getAbsolutePath();
        File datafile = new File(projPath + "/data/" + fileName);

        return readFromFile(datafile);
    }

    public static Shop readFromFile(File file) {
        Shop shop = new Shop();

        String line;
        String[] segments;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Read the number of jobs and machines
            line = br.readLine();
            segments = line.split("\\s+");
            int numJobs = Integer.valueOf(segments[0]);
            int numWorkCenters = Integer.valueOf(segments[1]);

            // Add the work centers to the shop
            for (int i = 0; i < numWorkCenters; i++) {
                shop.addWorkCenter(new WorkCenter(i));
            }

            // Read the jobs
            for (int j = 0; j < numJobs; j++) {
                line = br.readLine();
                segments = line.split("\\s+");

                System.out.println(line);

                Job job = new Job(j, new ArrayList<>(), 0, 0, 0, 1);

                double totalProcTime = 0.0;
                for (int i = 0; i < numWorkCenters; i++) {
                    int wcid = Integer.valueOf(segments[2 * i + 1]);
                    double procTime = Double.valueOf(segments[2 * i + 2]);
                    totalProcTime += procTime;

                    Operation o = new Operation(job, i, procTime, shop.getWorkCenter(wcid));

                    job.addOperation(o);
                }

                job.linkOperations();

                double dueDate = job.getReleaseTime() + 4 * totalProcTime;
                job.setDueDate(dueDate);

                // Add job to the shop
                shop.addJob(job);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return shop;
    }

    public static void main(String[] args) {


        Shop shop = Shop.readFromFile("20_5_0.txt");

        System.out.println(shop.toString());

    }
}
