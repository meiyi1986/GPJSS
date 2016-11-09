package yimei.util.random;

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 *
 * The abstract property of a distribution.
 *
 * @author yimei
 *
 */

abstract public class AbstractIntegerSampler {

	abstract public int next(RandomDataGenerator rdg);

	abstract public void setLower(int lower);
	abstract public void setUpper(int upper);
	abstract public void setMean(double mean);
	abstract public double getMean();

	abstract public AbstractIntegerSampler clone();
}
