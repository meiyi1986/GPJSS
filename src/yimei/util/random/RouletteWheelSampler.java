package yimei.util.random;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomDataGenerator;

import ec.EvolutionState;

/**
 * The roulette wheel sampler -- sample an index based on the given cumulative frequencies.
 *
 * @author yimei
 *
 */

public class RouletteWheelSampler {

	private double[] cumFreqs; // the cumulative frequencies, starting from 0.

	public RouletteWheelSampler(double[] cumFreqs) {
		this.cumFreqs = cumFreqs;
	}

	public int next(RandomDataGenerator rdg) {
		double a = rdg.nextUniform(0, cumFreqs[cumFreqs.length-1] - 0.0000000001);
		int idx = Arrays.binarySearch(cumFreqs, a);

		if (idx < 0)
			idx = -idx-2;

		return idx;
	}

	public int next(EvolutionState state, int thread) {
		double a = state.random[thread].nextDouble() * cumFreqs[cumFreqs.length-1];
		int idx = Arrays.binarySearch(cumFreqs, a);

		if (idx < 0)
			idx = -idx-2;

		return idx;
	}

	public int size() {
		return cumFreqs.length - 1;
	}
}
