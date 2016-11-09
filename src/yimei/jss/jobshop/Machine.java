package yimei.jss.jobshop;

/**
 * Created by yimei on 22/09/16.
 */
public class Machine {

    private final int id;
    private final WorkCenter workCenter;

    // For simulation.
    private double readyTime;

    public Machine(int id, WorkCenter workCenter, double readyTime) {
        this.id = id;
        this.workCenter = workCenter;
        this.readyTime = readyTime;
    }

    public Machine(int id, WorkCenter workCenter) {
        this.id = id;
        this.workCenter = workCenter;
    }

    public int getId() {
        return id;
    }

    public WorkCenter getWorkCenter() {
        return workCenter;
    }

    public double getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(double readyTime) {
        this.readyTime = readyTime;
    }

    public boolean equals(Machine other) {
        return id == other.id;
    }
}
