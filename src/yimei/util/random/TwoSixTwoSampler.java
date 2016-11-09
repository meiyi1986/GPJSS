package yimei.util.random;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * The 2-6-2 job weight sampler: 20% to have weight 1, 60% to have weight 2 and 20% to have weight 4.
 * @author yimei
 *
 */

public class TwoSixTwoSampler extends AbstractRealSampler {

	@Override
	public double next(RandomDataGenerator rdg) {
		double value = 1;
		double r = rdg.nextUniform(0, 1);
		if (r < 0.2) {
			value = 4;
		}
		else if (r < 0.8) {
			value = 2;
		}

		return value;
	}

	@Override
	public void setLower(double lower) {

	}

	@Override
	public void setUpper(double upper) {

	}

	@Override
	public void setMean(double mean) {

	}

	@Override
	public double getMean() {
		return 2d;
	}

	@Override
	public AbstractRealSampler clone() {
		return new TwoSixTwoSampler();
	}
}
