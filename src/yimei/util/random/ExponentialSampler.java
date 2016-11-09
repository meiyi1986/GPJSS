package yimei.util.random;

import org.apache.commons.math3.random.RandomDataGenerator;

public class ExponentialSampler extends AbstractRealSampler {

	private double mean;

	public ExponentialSampler() {
		super();
	}

	public ExponentialSampler(double mean) {
		super();
		this.mean = mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getMean() {
		return mean;
	}

	public double next(RandomDataGenerator rdg) {
		return rdg.nextExponential(mean);
	}

	@Override
	public void setLower(double lower) {
		// do nothing.
	}

	@Override
	public void setUpper(double upper) {
		// do nothing.

	}

	@Override
	public AbstractRealSampler clone() {
		return new ExponentialSampler(mean);
	}
}
