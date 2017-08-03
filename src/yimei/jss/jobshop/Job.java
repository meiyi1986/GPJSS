package yimei.jss.jobshop;

import yimei.jss.simulation.event.ProcessFinishEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A job.
 *
 * Created by yimei on 22/09/16.
 */
public class Job implements Comparable<Job> {

    private final int id;
    private List<Operation> operations;
    private List<ProcessFinishEvent> processFinishEvents;
    private final double arrivalTime;
    private final double releaseTime;
    private double dueDate;
    private final double weight;

    private double totalProcTime;
    private double avgProcTime;

    private double completionTime;

    public Job(int id,
               List<Operation> operations,
               double arrivalTime,
               double releaseTime,
               double dueDate,
               double weight) {
        this.id = id;
        this.operations = operations;
        this.arrivalTime = arrivalTime;
        this.releaseTime = releaseTime;
        this.dueDate = dueDate;
        this.weight = weight;
        this.processFinishEvents = new ArrayList<ProcessFinishEvent>();
    }

    public Job(int id, List<Operation> operations) {
        this(id, operations,
                0, 0, Double.POSITIVE_INFINITY, 1.0);
    }

    public int getId() {
        return id;
    }

    public List<Operation> getOperations() {
        return operations;
    }

//    public List<ProcessFinishEvent> getProcessFinishEvents() { return processFinishEvents; }
//
//    public void addProcessFinishEvent(ProcessFinishEvent processFinishEvent) {
//        for (ProcessFinishEvent p: processFinishEvents) {
//            if (p.getProcess().getOperationOption().getOperation().getId() ==
//                    processFinishEvent.getProcess().getOperationOption().getOperation().getId()) {
//                System.out.println("Shouldn't happen");
//            }
//        }
//        processFinishEvents.add(processFinishEvent);
//    }

    public Operation getOperation(int idx) {
        return operations.get(idx);
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getReleaseTime() {
        return releaseTime;
    }

    public double getDueDate() {
        return dueDate;
    }

    public double getWeight() {
        return weight;
    }

    public double getTotalProcTime() {
        return totalProcTime;
    }

    public double getAvgProcTime() {
        return avgProcTime;
    }

    public double getCompletionTime() {
        return completionTime;
    }

    public void setDueDate(double dueDate) {
        this.dueDate = dueDate;
    }

    public void setCompletionTime(double completionTime) {
        this.completionTime = completionTime;
    }

    public double flowTime() {
        return completionTime - arrivalTime;
    }

    public double weightedFlowTime() {
        return weight * flowTime();
    }

    public double tardiness() {
        double tardiness = completionTime - dueDate;
        if (tardiness < 0)
            tardiness = 0;

        return tardiness;
    }

    public double weightedTardiness() {
        return weight * tardiness();
    }

    public void addOperation(Operation op) {
        operations.add(op);
    }

    public void linkOperations() {
        Operation next = null;
        double nextProcTime = 0.0;

        //double fdd = releaseTime;

//        for (int i = 0; i < operations.size(); i++) {
//            Operation operation = operations.get(i);
//            for (OperationOption option: operation.getOperationOptions()) {
//                option.setFlowDueDate(fdd + option.getProcTime());
//            }
//            fdd += operation.getOperationOption().getProcTime();
//        }


        //TODO: Ask Yi and Meng
        double workRemaining = 0.0;
        int numOpsRemaining = 0;
        for (int i = operations.size()-1; i > -1; i--) {
            Operation operation = operations.get(i);
            for (OperationOption option: operation.getOperationOptions()) {

                option.setWorkRemaining(workRemaining + option.getProcTime());

                option.setNumOpsRemaining(numOpsRemaining);

                option.setNextProcTime(nextProcTime);
            }

            numOpsRemaining ++;
            OperationOption worstOption = operation.getOperationOption();
            workRemaining += worstOption.getProcTime(); //worst case scenario

            operation.setNext(next);

            next = operation;
            nextProcTime = worstOption.getProcTime(); //pessimistic guess
        }
        totalProcTime = workRemaining;
        avgProcTime = totalProcTime / operations.size();
    }

    @Override
    public String toString() {
        String string = String.format("Job %d, arrives at %.1f, due at %.1f, weight is %.1f. It has %d operations:\n",
                id, arrivalTime, dueDate, weight, operations.size());
        for (Operation operation: operations) {
            string += operation.toString();
        }

        return string;
    }

    public boolean equals(Job other) {
        return id == other.id;
    }

    @Override
    public int compareTo(Job other) {
        if (arrivalTime < other.arrivalTime)
            return -1;

        if (arrivalTime > other.arrivalTime)
            return 1;

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        if (id != job.id) return false;
        if (Double.compare(job.arrivalTime, arrivalTime) != 0) return false;
        if (Double.compare(job.releaseTime, releaseTime) != 0) return false;
        if (Double.compare(job.dueDate, dueDate) != 0) return false;
        if (Double.compare(job.weight, weight) != 0) return false;
        if (Double.compare(job.totalProcTime, totalProcTime) != 0) return false;
        if (Double.compare(job.avgProcTime, avgProcTime) != 0) return false;
        if (Double.compare(job.completionTime, completionTime) != 0) return false;
        if (operations != null ? !operations.equals(job.operations) : job.operations != null) return false;
        return processFinishEvents != null ? processFinishEvents.equals(job.processFinishEvents) : job.processFinishEvents == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (operations != null ? operations.hashCode() : 0);
        result = 31 * result + (processFinishEvents != null ? processFinishEvents.hashCode() : 0);
        temp = Double.doubleToLongBits(arrivalTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(releaseTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dueDate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(totalProcTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(avgProcTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(completionTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
