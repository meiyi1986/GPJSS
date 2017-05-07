package yimei.jss.jobshop;

import java.io.*;
import java.util.*;

/**
 * A static instance. It includes:
 * (1) Number of work centers and jobs
 * (2) Ready time of each work center (a work center has only one machine)
 * (3) For each job
 *   (3.1) Arrival time
 *   (3.2) Due date
 *   (3.3) Weight
 *   (3.4) Number of operations
 *   (3.5) For each operation
 *     (3.5.1) Work center to process it
 *     (3.5.2) Processing time
 *
 * Created by yimei on 22/11/16.
 */

public class StaticInstance implements JSSInstance {

    public final int numWorkCenters;
    public final int numJobs;
    private List<Double> workCenterReadyTimes;
    private List<JobInformation> jobInformations;

    public StaticInstance(int numWorkCenters, int numJobs,
                          List<Double> workCenterReadyTimes,
                          List<JobInformation> jobInformations) {
        this.numWorkCenters = numWorkCenters;
        this.numJobs = numJobs;
        this.workCenterReadyTimes = workCenterReadyTimes;
        this.jobInformations = jobInformations;
    }

    public StaticInstance(int numWorkCenters, int numJobs,
                          List<Double> workCenterReadyTimes) {
        this(numWorkCenters, numJobs, workCenterReadyTimes,
                new ArrayList<>());
    }

    public StaticInstance(int numWorkCenters, int numJobs) {
        this(numWorkCenters, numJobs,
                new ArrayList<>(Collections.nCopies(numWorkCenters, 0.0)));
    }

    public int getNumJobs() { return numJobs; }

    public int getNumWorkCenters() { return numWorkCenters; }

    public List<Double> getWorkCenterReadyTimes() {
        return workCenterReadyTimes;
    }

    public List<JobInformation> getJobInformations() {
        return jobInformations;
    }

    public void addJob(double arrivalTime, double dueDate, double weight,
                       int numOps, List<Integer> route, List<Double> procTimes) {
        jobInformations.add(new JobInformation(arrivalTime, dueDate, weight,
                numOps, route, procTimes));
    }

    public class JobInformation {
        private double arrivalTime;
        private double dueDate;
        private double weight;
        private int numOps;
        private List<Integer> route;
        private List<Double> procTimes;

        public JobInformation(double arrivalTime,
                              double dueDate,
                              double weight,
                              int numOps,
                              List<Integer> route,
                              List<Double> procTimes) {
            this.arrivalTime = arrivalTime;
            this.dueDate = dueDate;
            this.weight = weight;
            this.numOps = numOps;
            this.route = route;
            this.procTimes = procTimes;
        }
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

            // Read the ready times of the work centers
            line = br.readLine();
            segments = line.split("\\s+");
            List<Double> readyTimes = new ArrayList<>();
            for (int i = 0; i < numWorkCenters; i++) {
                readyTimes.add(Double.valueOf(segments[i]));
            }

            instance = new StaticInstance(numWorkCenters, numJobs, readyTimes);

            // Read the jobs
            for (int j = 0; j < numJobs; j++) {
                line = br.readLine();
                segments = line.split("\\s+");

//                System.out.println(line);

                double arrivalTime = Double.valueOf(segments[0]);
                double dueDate = Double.valueOf(segments[1]);
                double weight = Double.valueOf(segments[2]);
                int numOps = Integer.valueOf(segments[3]);

                List<Integer> route = new ArrayList<>();
                List<Double> procTimes = new ArrayList<>();

                for (int i = 0; i < numOps; i++) {
                    int wcid = Integer.valueOf(segments[2 * i + 4]);
                    double procTime = Double.valueOf(segments[2 * i + 5]);

                    route.add(wcid);
                    procTimes.add(procTime);
                }

                instance.addJob(arrivalTime, dueDate, weight, numOps, route, procTimes);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instance;
    }

    public void printToFile(String fileName) {
        String projPath = (new File("")).getAbsolutePath();
        File datafile = new File(projPath + "/data/" + fileName);

        printToFile(datafile);
    }

    public void printToFile(File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Print numbers of jobs and machines/work centers
            bw.write(numJobs + " " + numWorkCenters);
            bw.newLine();
            // Print the ready time of the work centers
            for (double t : workCenterReadyTimes) {
                bw.write(t + " ");
            }
            bw.newLine();
            // Print each job
            for (JobInformation jobInfo : jobInformations) {
                bw.write(jobInfo.arrivalTime + " " + jobInfo.dueDate + " " +
                        jobInfo.weight + " " + jobInfo.numOps + " ");
                for (int i = 0; i < jobInfo.numOps; i++) {
                    bw.write(jobInfo.route.get(i) + " " + jobInfo.procTimes.get(i) + " ");
                }
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a shop class based on the information
     * @return the shop
     */
    public Shop createShop() {
        Shop shop = new Shop();

        // Create the work centers for the shop
        for (int i = 0; i < numWorkCenters; i++) {
            shop.addWorkCenter(new WorkCenter(i, 1, new LinkedList<>(),
                    new ArrayList<>(Collections.nCopies(1, workCenterReadyTimes.get(i))),
                    0, workCenterReadyTimes.get(i)));
        }

        // Create the jobs for the shop
        for (int j = 0; j < numJobs; j++) {
            JobInformation jobInfo = jobInformations.get(j);

            Job job = new Job(j, new ArrayList<>(), jobInfo.arrivalTime,
                    jobInfo.arrivalTime, jobInfo.dueDate, jobInfo.weight);
            for (int k = 0; k < jobInfo.numOps; k++) {
                Operation op = new Operation(job, k, jobInfo.procTimes.get(k),
                        shop.getWorkCenter(jobInfo.route.get(k)));

                job.addOperation(op);
            }

            job.linkOperations();

            // Add the job to the shop
            shop.addJob(job);
        }

        return shop;
    }

    /**
     * Reset the shop
     * @param shop the shop
     */
    public void resetShop(Shop shop) {
        // Reset the work centers
        for (int i = 0; i < numWorkCenters; i++) {
            shop.getWorkCenter(i).reset(workCenterReadyTimes.get(i));
        }
    }

    /**
     * The distance to another static instance.
     * In both instances, the jobs are assumed to already be sorted
     * by their arrival time.
     *
     * @param other the other instance
     * @return the distance
     */
    public double distance(StaticInstance other) {
        // If the two instances have different number of work centers,
        // the distance is infinity (no need to calculate)
        if (numWorkCenters != other.numWorkCenters)
            return Double.POSITIVE_INFINITY;

        // Step 1: permutate the work centers to match the job routes
        List<Integer> permutation = new ArrayList<>();
        for (int i = 0; i < numWorkCenters; i++)
            permutation.add(-1);

        int[][] counts = new int[numWorkCenters][numWorkCenters];
        // Count the number of corresponding work centers in the other instance.
        for (int j = 0; j < numJobs; j++) {
            if (j > other.numJobs)
                break;

            int wc1 = jobInformations.get(j).route.get(0);
            int wc2 = other.jobInformations.get(j).route.get(0);
            counts[wc1][wc2] ++;
        }

        for (int i = 0; i < numWorkCenters; i++) {
            // Select the unselected work center with maximal count
            int maxCount = Integer.MIN_VALUE;
            int maxCountWorkCenter1 = -1;
            int maxCountWorkCenter2 = -1;
            for (int k1 = 0; k1 < numWorkCenters; k1++) {
                if (permutation.get(k1) > -1)
                    continue;

                for (int k2 = 0; k2 < numWorkCenters; k2++) {
                    if (permutation.contains(k2))
                        continue;

                    if (maxCount < counts[k1][k2]) {
                        maxCount = counts[k1][k2];
                        maxCountWorkCenter1 = k1;
                        maxCountWorkCenter2 = k2;
                    }
                }

            }

            permutation.set(maxCountWorkCenter1, maxCountWorkCenter2);
        }

        permutateWorkCenter(permutation);

        double distance = 0;
        // Step 2: calculate the distance between each job
        for (int j = 0; j < numJobs; j++) {
            double jobDist = 0;
            if (j >= other.numJobs) {
                // There is no more job in the other instance
                for (int i = 0; i < jobInformations.get(j).numOps; i++) {
                    jobDist += jobInformations.get(j).procTimes.get(i);
                }
            }
            else {
                JobInformation jobInfo1 = jobInformations.get(j);
                JobInformation jobInfo2 = other.jobInformations.get(j);

                for (int i = 0; i < jobInfo1.numOps; i++) {
                    if (i >= jobInfo2.numOps) {
                        // There is no more operation for the current job in the other instance
                        jobDist += jobInfo1.procTimes.get(i);
                    }
                    else {
                        // Check whether the operations are on the same machine
                        if (jobInfo1.route.get(i) == jobInfo2.route.get(i)) {
                            jobDist += Math.abs(jobInfo1.procTimes.get(i)
                                    - jobInfo2.procTimes.get(i));
                        }
                        else {
                            jobDist += jobInfo1.procTimes.get(i)
                                    + jobInfo2.procTimes.get(i);
                        }
                    }
                }

                // Check if the current job in the other instance has more operations
                if (jobInfo2.numOps > jobInfo1.numOps) {
                    for (int i = jobInfo1.numOps; i < jobInfo2.numOps; i++) {
                        jobDist += jobInfo2.procTimes.get(i);
                    }
                }
            }

            distance += jobDist;
        }

        // Check if the other instance has more jobs
        if (other.numJobs > numJobs) {
            for (int j = numJobs; j < other.numJobs; j++) {
                double jobDist = 0;
                for (int i = 0; i < other.jobInformations.get(j).numOps; i++) {
                    jobDist += other.jobInformations.get(j).procTimes.get(i);
                }

                distance += jobDist;
            }
        }

        return distance;
    }

    public void permutateWorkCenter(List<Integer> permutation) {
        List<Double> permReadyTimes = new ArrayList<>();
        for (int i = 0; i < numWorkCenters; i++) {
            permReadyTimes.add(workCenterReadyTimes.get(permutation.get(i)));
        }
        workCenterReadyTimes = permReadyTimes;

        for (JobInformation jobInfo : jobInformations) {
            List<Integer> permRoute = new ArrayList<>();
            for (int i = 0; i < jobInfo.numOps; i++) {
                permRoute.add(permutation.get(jobInfo.route.get(i)));
            }
            jobInfo.route = permRoute;
        }
    }

    public String toString() {
        String string = numJobs + "  " + numWorkCenters + "\n";

        for (int i = 0; i < numWorkCenters; i++) {
            string += "  " + workCenterReadyTimes.get(i);
        }

        string += "\n";

        for (int j = 0; j < numJobs; j++) {
            JobInformation jobInfo = jobInformations.get(j);
            string += "  " + jobInfo.arrivalTime + "  " + jobInfo.dueDate
                    + "  " + jobInfo.weight + "  " + jobInfo.numOps;

            for (int k = 0; k < jobInfo.numOps; k++) {
                string += "  " + jobInfo.route.get(k)
                        + "  " + jobInfo.procTimes.get(k);
            }

            string += "\n";
        }

        return string;
    }

    public static void main(String[] args) {

//        StaticInstance instance = readFromFile("complete-20_5_0.txt");
//
//        System.out.println(instance.toString());
//
//        List<Integer> permutation = new ArrayList<>();
//        for (int i = 0; i < instance.numWorkCenters; i++) {
//            permutation.add(i);
//        }
//        Collections.shuffle(permutation, new Random(0));
//
//        instance.permutateWorkCenter(permutation);
//
//        System.out.println(permutation);
//
//        System.out.println(instance.toString());
//
//        instance.printToFile("complete-20_5_0-perm.txt");

        StaticInstance inst1 = readFromFile("complete-20_5_0.txt");
        StaticInstance inst2 = readFromFile("complete-20_5_0-perm.txt");

        System.out.println(inst2.distance(inst1));
    }
}