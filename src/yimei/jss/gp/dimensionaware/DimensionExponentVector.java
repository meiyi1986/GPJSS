package yimei.jss.gp.dimensionaware;

import java.util.ArrayList;
import java.util.List;

/**
 * The dimension exponent vector.
 *
 * Created by yimei on 26/10/16.
 */
public class DimensionExponentVector {

    private final List<Dimension> dimensions;
    private List<Double> exponents;

    public DimensionExponentVector(List<Dimension> dimensions, List<Double> exponents) {
        this.dimensions = dimensions;
        this.exponents = exponents;
    }

    public DimensionExponentVector(List<Dimension> dimensions) {
        this(dimensions, new ArrayList<>());
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public List<Double> getExponents() {
        return exponents;
    }

    public double getExponent(int index) {
        return exponents.get(index);
    }

    public double getExponent(Dimension dimension) {
        int index = dimensions.indexOf(dimension);
        return getExponent(index);
    }

    public void setExponents(List<Double> exponents) {
        this.exponents = exponents;
    }

    public void setExponent(int index, double value) {
        exponents.set(index, value);
    }

    public void setExponent(Dimension dimension, double value) {
        int index = dimensions.indexOf(dimension);
        setExponent(index, value);
    }

    public double inconsistency(DimensionExponentVector other) {
        double value = 0d;

        for (int i = 0; i < exponents.size(); i++) {
            value += Math.abs(exponents.get(i) - other.exponents.get(i));
        }

        return value;
    }

    public boolean equals(DimensionExponentVector other) {
        return (dimensions.equals(other.dimensions)
                && exponents.equals(other.exponents));
    }
}
