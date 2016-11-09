package yimei.jss.jobshop;

import yimei.jss.rule.AbstractRule;
import yimei.jss.rule.basic.EDD;
import yimei.jss.rule.basic.FDD;
import yimei.jss.rule.composite.ATC;
import yimei.jss.rule.composite.TwoPTplusWINQplusNPT;
import yimei.jss.rule.weighted.WATC;

import java.util.HashMap;
import java.util.Map;

/**
 * The enumeration of all the objectives that may be optimised in job shop scheduling.
 * All the objectives are assumed to be minimised.
 *
 * Created by yimei on 28/09/16.
 *
 */
public enum Objective {

    MAKESPAN("makespan"),
    MEAN_FLOWTIME("mean-flowtime"),
    MAX_FLOWTIME("max-flowtime"),
    MEAN_TARDINESS("mean-tardiness"),
    MAX_TARDINESS("max-tardiness"),
    MEAN_WEIGHTED_FLOWTIME("mean-weighted-flowtime"),
    MAX_WEIGHTED_FLOWTIME("max-weighted-flowtime"),
    MEAN_WEIGHTED_TARDINESS("mean-weighted-tardiness"),
    MAX_WEIGHTED_TARDINESS("max-weighted-tardiness"),
    PROP_TARDY_JOBS("prop-tardy-jobs");

    private final String name;

    Objective(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, Objective> lookup = new HashMap<>();

    static {
        for (Objective a : Objective.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static Objective get(String name) {
        return lookup.get(name);
    }

    public AbstractRule benchmarkRule() {
        switch (this) {
            case MAKESPAN:
                return new FDD();
            case MEAN_FLOWTIME:
                return new TwoPTplusWINQplusNPT();
            case MEAN_TARDINESS:
                return new ATC();
            case MEAN_WEIGHTED_TARDINESS:
                return new WATC();
            case MAX_TARDINESS:
                return new EDD();
        }

        return null;
    }
}
