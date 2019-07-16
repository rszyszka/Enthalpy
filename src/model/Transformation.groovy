package model

import model.decomposition.DecompositionType


class Transformation {

    private int temp_start
    private int temp_stop
    private double energy
    private DecompositionType decompositionType


    Transformation(int temp_start, int temp_stop, double energy, DecompositionType decompositionType) {
        this.temp_start = temp_start
        this.temp_stop = temp_stop
        this.energy = energy
        this.decompositionType = decompositionType
    }


    int getTemp_start() {
        return temp_start
    }

    void setTemp_start(int temp_start) {
        this.temp_start = temp_start
    }

    int getTemp_stop() {
        return temp_stop
    }

    void setTemp_stop(int temp_stop) {
        this.temp_stop = temp_stop
    }

    double getEnergy() {
        return energy
    }

    void setEnergy(double energy) {
        this.energy = energy
    }

    DecompositionType getDecompositionType() {
        return decompositionType
    }

    void setDecompositionType(DecompositionType decompositionType) {
        this.decompositionType = decompositionType
    }
}
