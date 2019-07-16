package model.decomposition

class AtStartDecomposition extends Decomposition {

    @Override
    void decomposeEnergy(List<Double> activationEnergy) {
        activationEnergy[startIndex] = energy
    }

}
