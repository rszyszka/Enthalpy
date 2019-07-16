package model.decomposition

class DiminishingDecomposition extends Decomposition {

    @Override
    void decomposeEnergy(List<Double> activationEnergy) {
        double stride = 4.0 / (transformationRange + 1.0)
        double sum = 0.0
        int counter = 0
        def values = []
        for (double k = -4; k <= 0; k += stride) {
            sum += -k
            values[counter++] = -k
        }
        counter = 0
        double testEnergy = 0.0
        for (int i = startIndex; i <= stopIndex; i++) {
            activationEnergy[i] = values[counter++] / sum * energy as Double
            testEnergy += activationEnergy[i]
        }
        double rest = energy - testEnergy
        testEnergy = rest / (transformationRange + 1.0)
        for (int i = startIndex; i <= stopIndex; i++) {
            activationEnergy[i] = activationEnergy[i] + testEnergy as Double
        }
    }

}
