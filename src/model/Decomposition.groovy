package model

enum Decomposition {
    EQUAL("Równomierny"),
    AT_START("Na początku"),
    AT_END("Na końcu"),
    GROWING("Rosnący"),
    DIMINISHING("Malejący"),
    EXP_DIMINISHING("Exponenta malejąco"),
    EXP_GROWING("Exponenta rosnąco"),
    RAND("Losowy")

    private final String name

    private Decomposition(String s) {
        name = s
    }

    String toString() {
        return this.name
    }

}
