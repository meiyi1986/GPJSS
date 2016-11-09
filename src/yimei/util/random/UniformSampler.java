package yimei.util.random;

import org.apache.commons.math3.random.RandomDataGenerator;

public class UniformSampler extends AbstractRealSampler {

	private double lower;
	private double upper;

	public UniformSampler() {
		super();
	}

	public UniformSampler(double lower, double upper) {
		super();
		this.lower = lower;
		this.upper = upper;
	}

	public void set(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public void setLower(double lower) {
		this.lower = lower;
	}

	public void setUpper(double upper) {
		this.upper = upper;
	}

	public double getLower() {
		return lower;
	}

	public double getUpper() {
		return upper;
	}

	@Override
	public double next(RandomDataGenerator rdg) {
		return rdg.nextUniform(lower, upper);
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
	public AbstractRealSampler clone() {
		return new UniformSampler(lower, upper);
	}
}
