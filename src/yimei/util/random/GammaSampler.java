package yimei.util.random;

import org.apache.commons.math3.random.RandomDataGenerator;

public class GammaSampler extends AbstractRealSampler {

	private double shape;
	private double scale;

	public GammaSampler() {
		super();
	}

	public GammaSampler(double shape, double scale) {
		super();
		this.shape = shape;
		this.scale = scale;
	}

	public void setShape(double shape) {
		this.shape = shape;
	}

	public double getShape() {
		return shape;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getScale() {
		return scale;
	}

	@Override
	public double next(RandomDataGenerator rdg) {
		return rdg.nextGamma(shape, scale);
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
		return shape * scale;
	}

	@Override
	public AbstractRealSampler clone() {
		return new GammaSampler(shape, scale);
	}
}
