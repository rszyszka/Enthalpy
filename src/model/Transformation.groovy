package model

class Transformation {
    private int temp_start
    private int temp_stop
    private double energy
    private Decomposition decomposition

    Transformation(int temp_start, int temp_stop, double energy, Decomposition decomposition) {
        this.temp_start = temp_start
        this.temp_stop = temp_stop
        this.energy = energy
        this.decomposition = decomposition
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

    Decomposition getDecomposition() {
        return decomposition
    }

    void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition
    }
}
