package model.decomposition


import static model.decomposition.DecompositionType.*

abstract class Decomposition {

    protected int transformationRange
    protected int startIndex
    protected int stopIndex
    protected double energy


    static Decomposition create(DecompositionType type) {
        switch (type) {
            case EQUAL:
                return new EqualDecomposition()
            case AT_START:
                return new AtStartDecomposition()
            case AT_END:
                return new AtEndDecomposition()
            case GROWING:
                return new GrowingDecomposition()
            case DIMINISHING:
                return new DiminishingDecomposition()
            case EXP_DIMINISHING:
                return new ExpDiminishingDecomposition()
            case EXP_GROWING:
                return new ExpGrowingDecomposition()
            case RAND:
                return new RandomDecomposition()
        }
    }


    abstract void decomposeEnergy(List<Double> activationEnergy)


    void setTransformationRange(int transformationRange) {
        this.transformationRange = transformationRange
    }

    void setStartIndex(int startIndex) {
        this.startIndex = startIndex
    }

    void setStopIndex(int stopIndex) {
        this.stopIndex = stopIndex
    }

    void setEnergy(double energy) {
        this.energy = energy
    }

}
