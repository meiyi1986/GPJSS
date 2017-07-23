package yimei.jss.jobshop;

import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.workcenter.basic.SBT;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A flexible static instance. It includes:
 * (1) Number of work centres and jobs
 * (2) For each job
 *   (2.1) Number of operations
 *   (2.2) For each operation
 *     (2.2.1) Works centres which can process operation
 *     (2.2.2) For each work centre
 *       (2.2.2.1) Work centre number
 *       (2.2.2.2) Time taken to complete job on this work centre
 *
 * Created by dyska on 21/04/17.
 */
public class FlexibleStaticInstance implements JSSInstance {
    public final int numWorkCenters;
    public final int numJobs;
    private List<JobInformation> jobInformations;
    private List<Double> workCenterReadyTimes;
    private String filePath;

    public FlexibleStaticInstance(int numWorkCenters, int numJobs, String filePath) {
        this.numWorkCenters = numWorkCenters;
        this.numJobs = numJobs;
        this.jobInformations = new ArrayList<>();
        this.filePath = filePath;
        this.workCenterReadyTimes = new ArrayList<>(
                Collections.nCopies(numWorkCenters, 0.0));
    }

    public FlexibleStaticInstance(int numWorkCenters, int numJobs,
                          List<JobInformation> jobInformations, List<Double> workCenterReadyTimes) {
        this.numWorkCenters = numWorkCenters;
        this.numJobs = numJobs;
        this.jobInformations = jobInformations;
        this.workCenterReadyTimes = workCenterReadyTimes;
    }

    public static FlexibleStaticInstance readFromAbsPath(String filePath) {
        File datafile = new File(filePath);
        return readFromFile(datafile);
    }

    public static FlexibleStaticInstance readFromPath(String filePath) {
        String projPath = (new File("")).getAbsolutePath();
        File datafile = new File(projPath + "/data/" + filePath);

        return readFromFile(datafile);
    }

    public static FlexibleStaticInstance readFromFile(File file) {
        FlexibleStaticInstance instance = null;

        String line;
        String[] segments;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // Read the number of jobs and machines

            line = br.readLine();
            segments = line.split("\\s+");
            int numJobs = Integer.valueOf(segments[0]);
            int numWorkCenters = Integer.valueOf(segments[1]); //work centers = machines

            instance = new FlexibleStaticInstance(numWorkCenters,numJobs,file.getPath());

            int numOperations;
            //Read in the jobs
            for (int i = 0; i < numJobs; ++i) {
                int index = 0;
                line = br.readLine();
                segments = line.split("\\s+");
                if (segments[0].equals("")) {
                    //brandimarte_data starts rows with space, this is a workaround
                    index++;
                }
                numOperations = Integer.valueOf(segments[index]);
                index++;

                JobInformation job = new JobInformation(numOperations);
                int numOperatableMachines;
                for (int j = 0; j < numOperations; ++j) {

                    numOperatableMachines = Integer.valueOf(segments[index]);
                    OperationInformation operation = new OperationInformation();

                    for (int k = 0; k < numOperatableMachines; ++k) {
                        //read in k (machine, processing time) pairs
                        int workCentreNumber = Integer.valueOf(segments[2*k+index+1]);
                        int processingTime = Integer.valueOf(segments[2*k+index+2]);
                        operation.getOperationOptions().add(
                                new OperationOptionInformation(workCentreNumber, processingTime)
                        );
                    }
                    job.getOperations().add(operation);
                    index += numOperatableMachines * 2 + 1;
                }
                instance.getJobInformations().add(job);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find file "+file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instance;
    }

    @Override
    public String toString() {
        String msg = "Number of jobs: "+numJobs+ ", number of work centers: "+numWorkCenters+"\n\n";
        int i = 1;
        for (JobInformation job: jobInformations ) {
            msg += "Job "+ i +" has "+job.getNumOps()+" steps.\n";
            int j = 1;
            for (OperationInformation operation: job.getOperations()) {
                int k = 1;
                msg += "Operation "+j+" - ";
                for (OperationOptionInformation operationOption: operation.getOperationOptions()) {
                    msg += "option "+k+": work center number: "
                            +operationOption.getWorkCenterId()+", time: "+operationOption.getTime()+", ";
                    k++;
                }
                msg = msg.substring(0, msg.length()-2); //remove last ", "
                msg += "\n";
                j++;
            }
            msg += "\n";
            i++;
        }

        return msg;
    }

    public List<JobInformation> getJobInformations() {
        return jobInformations;
    }

    public Shop createShop() {

        Shop shop = new Shop();
        //Create the work centers of the shop
        for (int i = 0; i < numWorkCenters; ++i) {
            //created without work center ready times
            //files start work center numbers from 1, alter to start from 0
            shop.addWorkCenter(new WorkCenter(i, 1, new LinkedList<>(),
                    new ArrayList<>(Collections.nCopies(1, workCenterReadyTimes.get(i))),
                    0, workCenterReadyTimes.get(i)));
        }

        //Create the jobs for the shop
        for (int j = 0; j < numJobs; ++j) {
            JobInformation jobInfo = jobInformations.get(j);

            Job job = new Job(j, new ArrayList<>(),
                    jobInfo.getArrivalTime(), jobInfo.getArrivalTime(),
                    jobInfo.getDueDate(), jobInfo.getWeight());

            for (int k = 0; k < jobInfo.getNumOps(); ++k) {
                OperationInformation operationInfo = jobInfo.getOperations().get(k);
                Operation operation = new Operation(job, k);

                for (int l = 0; l < operationInfo.getOperationOptions().size(); ++l) {
                    OperationOptionInformation operationOptionInformation = operationInfo.getOperationOptions().get(l);
                    operation.addOperationOption(new OperationOption(
                            operation,l,operationOptionInformation.getTime(),
                             shop.getWorkCenter(operationOptionInformation.getWorkCenterId()-1)));
                }
                job.addOperation(operation);
            }

            job.linkOperations();

            // Add the job to the shop
            shop.addJob(job);
        }

        return shop;
    }

    @Override
    public void resetShop(Shop shop) {
        // Reset the work centers
        for (int i = 0; i < numWorkCenters; i++) {
            shop.getWorkCenter(i).reset(workCenterReadyTimes.get(i));
        }
    }

    @Override
    public int getNumWorkCenters() {
        return numWorkCenters;
    }

    @Override
    public int getNumJobs() {
        return numJobs;
    }

    public String getFilePath() { return filePath; }

    @Override
    public List<Double> getWorkCenterReadyTimes() {
        return workCenterReadyTimes;
    }

    public static class JobInformation {
        private int numOps;
        private List<OperationInformation> operations;
        private double arrivalTime;
        private double dueDate;
        private double weight;

        public JobInformation(int numOps) {
            this.numOps = numOps;
            this.arrivalTime = 0.0;
            this.dueDate = 0.0;
            this.weight = 1.0;
            this.operations = new ArrayList<>();
        }

        //Overloaded constructor in case file does contain additional information
        public JobInformation(int numOps, double arrivalTime,
                              double dueDate, double weight) {
            this.numOps = numOps;
            this.arrivalTime = arrivalTime;
            this.dueDate = dueDate;
            this.weight = weight;
            this.operations = new ArrayList<>();
        }

        public List<OperationInformation> getOperations() {
            return operations;
        }

        public int getNumOps() {
            return numOps;
        }

        public double getArrivalTime() {return arrivalTime; }

        public double getDueDate() { return dueDate; }

        public double getWeight() { return weight; }
    }

    public static class OperationInformation {
        private List<OperationOptionInformation> options;
        private OperationOptionInformation chosenOption;

        public void chooseOperationOption() {
            if (chosenOption == null) {
                if (options.size() > 1) {
                    OperationOptionInformation bestOption = null;
                    double leastProcTime = Double.MAX_VALUE;
                    for (OperationOptionInformation op: options) {
                        if (op.getTime() < leastProcTime) {
                            leastProcTime = op.getTime();
                            bestOption = op;
                        }
                    }
                    this.chosenOption = bestOption;
                }
                else if (options.size() == 1) {
                    this.chosenOption = options.iterator().next();
                }
            }
        }

        public OperationOptionInformation getChosenOption() { return chosenOption; }

        public OperationInformation() {
            this.options = new ArrayList<>();
        }

        public List<OperationOptionInformation> getOperationOptions() {
            return options;
        }
    }

    public static class OperationOptionInformation {
        private int workCenterId;
        private int time;

        public OperationOptionInformation(int workCenterId, int time) {
            this.workCenterId = workCenterId;
            this.time = time;
        }

        public int getWorkCenterId() {
            return workCenterId;
        }

        public int getTime() {
            return time;
        }
    }

    public static void main(String[] args) {
        FlexibleStaticInstance instance = FlexibleStaticInstance.readFromPath(
                "FJSS/Brandimarte_Data/Text/Mk01.fjs");
        //System.out.println(instance.toString());
        Shop shop = instance.createShop();
        System.out.println(shop.toString());
    }
}
