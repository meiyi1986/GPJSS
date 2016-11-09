package yimei.jss.gp.dimensionaware;

import ec.gp.GPNode;

/**
 * The dimension aware GP node.
 *
 * Created by yimei on 26/10/16.
 */
public abstract class DimensionAwareGPNode extends GPNode {

    protected DimensionExponentVector dimensionExponents;
    protected double dimensionInconsistency;

    public DimensionExponentVector getDimensionExponents() {
        return dimensionExponents;
    }

    public double getDimensionInconsistency() {
        return dimensionInconsistency;
    }

    public void setDimensionExponents(DimensionExponentVector dimensionExponents) {
        this.dimensionExponents = dimensionExponents;
    }

    public void setDimensionInconsistency(double dimensionInconsistency) {
        this.dimensionInconsistency = dimensionInconsistency;
    }
}
