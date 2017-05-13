package yimei.jss.jobshop;

import java.util.List;

/**
 * A job.
 *
 * Created by yimei on 22/09/16.
 */
public class Job implements Comparable<Job> {

    private final int id;
    private List<Operation> operations;
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

        double fdd = releaseTime;

        for (int i = 0; i < operations.size(); i++) {
            Operation operation = operations.get(i);
            for (OperationOption option: operation.getOperationOptionSet()) {
                option.setFlowDueDate(fdd + option.getProcTime());
            }
            //fdd needs to be incremented
            //don't know which option to go with yet - will choose highest time for now
            //TODO: Talk to Yi about this
            fdd += operation.getOperationOption().getProcTime();
        }

        double workRemaining = 0.0;
        int numOpsRemaining = 0;
        for (int i = operations.size()-1; i > -1; i--) {
            Operation operation = operations.get(i);
            for (OperationOption option: operation.getOperationOptionSet()) {

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

            //TODO: Same assumption as above

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
}
