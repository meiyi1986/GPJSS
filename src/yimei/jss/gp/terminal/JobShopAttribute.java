package yimei.jss.gp.terminal;

import org.apache.commons.lang3.math.NumberUtils;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;

import java.util.*;

/**
 * The attributes of the job shop.
 * NOTE: All the attributes are relative to the current time.
 *       This is for making the decision making process memoryless,
 *       i.e. independent of the current time.
 *
 * @author yimei
 */

public enum JobShopAttribute {
    CURRENT_TIME("t"), // the current time

    // The machine-related attributes (independent of the jobs in the queue of the machine).
    NUM_OPS_IN_QUEUE("NIQ"), // the number of operations in the queue
    WORK_IN_QUEUE("WIQ"), // the work in the queue
    MACHINE_READY_TIME("MRT"), // the ready time of the machine

    // The job/operation-related attributes (depend on the jobs in the queue).
    PROC_TIME("PT"), // the processing time of the operation
    NEXT_PROC_TIME("NPT"), // the processing time of the next operation
    OP_READY_TIME("ORT"), // the ready time of the operation
    NEXT_READY_TIME("NRT"), // the ready time of the next machine
    WORK_REMAINING("WKR"), // the work remaining
    NUM_OPS_REMAINING("NOR"), // the number of operations remaining
    WORK_IN_NEXT_QUEUE("WINQ"), // the work in the next queue
    NUM_OPS_IN_NEXT_QUEUE("NINQ"), // number of operations in the next queue
    FLOW_DUE_DATE("FDD"), // the flow due date
    DUE_DATE("DD"), // the due date
    WEIGHT("W"), // the job weight
    ARRIVAL_TIME("AT"), // the arrival time

    // Relative version of the absolute time attributes
    MACHINE_WAITING_TIME("MWT"), // the waiting time of the machine = t - MRT
    OP_WAITING_TIME("OWT"), // the waiting time of the operation = t - ORT
    NEXT_WAITING_TIME("NWT"), // the waiting time for the next machine to be ready = NRT - t
    RELATIVE_FLOW_DUE_DATE("rFDD"), // the relative flow due date = FDD - t
    RELATIVE_DUE_DATE("rDD"), // the relative due date = DD - t

    // Used in Su's paper
    TIME_IN_SYSTEM("TIS"), // time in system = t - releaseTime
    SLACK("SL"); // the slack

    private final String name;

    JobShopAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, JobShopAttribute> lookup = new HashMap<>();

    static {
        for (JobShopAttribute a : JobShopAttribute.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static JobShopAttribute get(String name) {
        return lookup.get(name);
    }

    public double value(Operation op, WorkCenter workCenter, SystemState systemState) {
        double value = -1;

        switch (this) {
            case CURRENT_TIME:
                value = systemState.getClockTime();
                break;
            case NUM_OPS_IN_QUEUE:
                value = workCenter.getQueue().size();
                break;
            case WORK_IN_QUEUE:
                value = workCenter.getWorkInQueue();
                break;
            case MACHINE_READY_TIME:
                value = workCenter.getReadyTime();
                break;
            case MACHINE_WAITING_TIME:
                value = systemState.getClockTime() - workCenter.getReadyTime();
                break;
            case PROC_TIME:
                value = op.getProcTime();
                break;
            case NEXT_PROC_TIME:
                value = op.getNextProcTime();
                break;
            case OP_READY_TIME:
                value = systemState.getClockTime();
                break;
            case OP_WAITING_TIME:
                value = systemState.getClockTime() - op.getReadyTime();
                break;
            case NEXT_READY_TIME:
                value = systemState.nextReadyTime(op);
                break;
            case NEXT_WAITING_TIME:
                value = systemState.nextReadyTime(op) - systemState.getClockTime();
                break;
            case WORK_REMAINING:
                value = op.getWorkRemaining();
                break;
            case NUM_OPS_REMAINING:
                value = op.getNumOpsRemaining();
                break;
            case WORK_IN_NEXT_QUEUE:
                value = systemState.workInNextQueue(op);
                break;
            case NUM_OPS_IN_NEXT_QUEUE:
                value = systemState.numOpsInNextQueue(op);
                break;
            case FLOW_DUE_DATE:
                value = op.getFlowDueDate();
                break;
            case RELATIVE_FLOW_DUE_DATE:
                value = op.getFlowDueDate() - systemState.getClockTime();
                break;
            case DUE_DATE:
                value = op.getJob().getDueDate();
                break;
            case RELATIVE_DUE_DATE:
                value = op.getJob().getDueDate() - systemState.getClockTime();
                break;
            case WEIGHT:
                value = op.getJob().getWeight();
                break;
            case ARRIVAL_TIME:
                value = op.getJob().getArrivalTime();
                break;
            case TIME_IN_SYSTEM:
                value = systemState.getClockTime() - op.getJob().getReleaseTime();
                break;
            case SLACK:
                value = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();
                break;
            default:
                System.err.println("Undefined attribute " + name);
                System.exit(1);
        }

        return value;
    }

    public static double valueOfString(String attribute, Operation op, WorkCenter workCenter,
                                       SystemState systemState,
                                       List<JobShopAttribute> ignoredAttributes) {
        JobShopAttribute a = get(attribute);
        if (a == null) {
            if (NumberUtils.isNumber(attribute)) {
                return Double.valueOf(attribute);
            } else {
                System.err.println(attribute + " is neither a defined attribute nor a number.");
                System.exit(1);
            }
        }

        if (ignoredAttributes.contains(a)) {
            return 1.0;
        } else {
            return a.value(op, workCenter, systemState);
        }
    }

    /**
     * Return the basic attributes.
     * @return the basic attributes.
     */
    public static JobShopAttribute[] basicAttributes() {
        return new JobShopAttribute[]{
                JobShopAttribute.CURRENT_TIME,
                JobShopAttribute.NUM_OPS_IN_QUEUE,
                JobShopAttribute.WORK_IN_QUEUE,
                JobShopAttribute.MACHINE_READY_TIME,
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.OP_READY_TIME,
                JobShopAttribute.NEXT_READY_TIME,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.WORK_IN_NEXT_QUEUE,
                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE,
                JobShopAttribute.FLOW_DUE_DATE,
                JobShopAttribute.DUE_DATE,
                JobShopAttribute.WEIGHT,

                JobShopAttribute.ARRIVAL_TIME,
                JobShopAttribute.SLACK
        };
    }

    /**
     * The attributes relative to the current time.
     * @return the relative attributes.
     */
    public static JobShopAttribute[] relativeAttributes() {
        return new JobShopAttribute[]{
                JobShopAttribute.NUM_OPS_IN_QUEUE,
                JobShopAttribute.WORK_IN_QUEUE,
                JobShopAttribute.MACHINE_WAITING_TIME,
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.OP_WAITING_TIME,
                JobShopAttribute.NEXT_WAITING_TIME,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.WORK_IN_NEXT_QUEUE,
                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE,
                JobShopAttribute.RELATIVE_FLOW_DUE_DATE,
                JobShopAttribute.RELATIVE_DUE_DATE,
                JobShopAttribute.WEIGHT,

                JobShopAttribute.TIME_IN_SYSTEM,
                JobShopAttribute.SLACK
        };
    }

    /**
     * The attributes for minimising mean weighted tardiness (Su's paper).
     * @return the attributes.
     */
    public static JobShopAttribute[] mwtAttributes() {
        return new JobShopAttribute[]{
                JobShopAttribute.TIME_IN_SYSTEM,
                JobShopAttribute.OP_WAITING_TIME,
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.DUE_DATE,
                JobShopAttribute.SLACK,
                JobShopAttribute.WEIGHT,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.WORK_IN_NEXT_QUEUE
        };
    }

    public static JobShopAttribute[] countAttributes() {
        return new JobShopAttribute[] {
                JobShopAttribute.NUM_OPS_IN_QUEUE,
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE
        };
    }

    public static JobShopAttribute[] weightAttributes() {
        return new JobShopAttribute[] {
                JobShopAttribute.WEIGHT
        };
    }

    public static JobShopAttribute[] timeAttributes() {
        return new JobShopAttribute[] {
                JobShopAttribute.MACHINE_WAITING_TIME,
                JobShopAttribute.OP_WAITING_TIME,
                JobShopAttribute.NEXT_READY_TIME,
                JobShopAttribute.FLOW_DUE_DATE,
                JobShopAttribute.DUE_DATE,

                JobShopAttribute.WORK_IN_QUEUE,
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.WORK_IN_NEXT_QUEUE,

                JobShopAttribute.TIME_IN_SYSTEM,
                JobShopAttribute.SLACK
        };
    }
}
