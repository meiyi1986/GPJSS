package yimei.util.random;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * A uniform integer distribution sampler - [lower, upper] (endpoints included).
 *
 * @author yimei
 *
 */

public class UniformIntegerSampler extends AbstractIntegerSampler {

	private int lower;
	private int upper;

	public UniformIntegerSampler() {
		super();
	}

	public UniformIntegerSampler(int lower, int upper) {
		super();
		this.lower = lower;
		this.upper = upper;
	}

	public void set(int lower, int upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public void setLower(int lower) {
		this.lower = lower;
	}

	public void setUpper(int upper) {
		this.upper = upper;
	}

	public int getLower() {
		return lower;
	}

	public int getUpper() {
		return upper;
	}

	@Override
	public int next(RandomDataGenerator rdg) {
		return rdg.nextInt(lower, upper);
	}

	@Override
	public double getMean() {
		return (lower + upper) * 0.5;
	}

	@Override
	public void setMean(double mean) {
		// do nothing.
	}

	@Override
	public AbstractIntegerSampler clone() {
		return new UniformIntegerSampler(lower, upper);
	}


}
