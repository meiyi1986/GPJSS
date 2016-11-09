package yimei.util.random;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 *
 * The abstract property of a distribution.
 *
 * @author yimei
 *
 */

abstract public class AbstractRealSampler {

	abstract public double next(RandomDataGenerator rdg);

	abstract public void setLower(double lower);
	abstract public void setUpper(double upper);
	abstract public void setMean(double mean);
	abstract public double getMean();

	abstract public AbstractRealSampler clone();
}
