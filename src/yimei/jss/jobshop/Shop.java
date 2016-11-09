package yimei.jss.jobshop;

import java.util.ArrayList;
import java.util.List;

/**
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
}
