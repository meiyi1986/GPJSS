package yimei.ec.localsearch;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

/**
 * Created by yimei on 10/10/16.
 */
public class LocalSearchPipline extends BreedingPipeline {

    public static final String P_ITERATIONS = "iterations";
    public static final String P_NUM_NEIGHBOURS = "num-neighbours";
    public static final String P_NEIGHBOUR_GENERATOR = "neighbour-generator";

    public static final String P_LS = "ls";
    public static final int NUM_SOURCES = 1;

    private int iterations;
    private int numNeighbours;
    private BreedingPipeline neighbourGenerator;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        iterations = state.parameters.getIntWithDefault(base.push(P_ITERATIONS), null, -1);
        numNeighbours = state.parameters.getIntWithDefault(base.push(P_NUM_NEIGHBOURS), null, -1);

        Parameter def = defaultBase();
        Parameter p = base.push(P_NEIGHBOUR_GENERATOR);
        Parameter d = def.push(P_NEIGHBOUR_GENERATOR);
        neighbourGenerator = (BreedingPipeline)
                (state.parameters.getInstanceForParameter(p, d, BreedingPipeline.class));
        neighbourGenerator.setup(state, p);
    }

    @Override
    public int produce(int min,
                       int max,
                       int start,
                       int subpopulation,
                       Individual[] inds,
                       EvolutionState state, int thread) {
        return 0;
    }

    @Override
    public int numSources() {
        return NUM_SOURCES;
    }

    @Override
    public Parameter defaultBase() {
        return new Parameter(P_LS);
    }
}
