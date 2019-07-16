package model.decomposition

class EqualDecomposition extends Decomposition {

    @Override
    void decomposeEnergy(List<Double> activationEnergy) {
        double energyPart = energy / (transformationRange + 1.0)
        for (int i = startIndex; i <= stopIndex; i++) {
            activationEnergy[i] = energyPart
        }
    }

}
