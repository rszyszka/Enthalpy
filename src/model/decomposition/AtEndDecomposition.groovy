package model.decomposition

class AtEndDecomposition extends Decomposition {

    @Override
    void decomposeEnergy(List<Double> activationEnergy) {
        activationEnergy[stopIndex] = energy
    }

}
