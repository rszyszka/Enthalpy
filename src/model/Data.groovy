package model

import model.decomposition.Decomposition


class Data {

    private static boolean DATA_LOADED = false

    private List<Integer> temperatures = []
    private List<Double> specificHeats = []
    private List<Double> enthalpies = []
    private List<Double> activationEnergy = []


    static void copyValues(Data source, Data destination) {
        destination.temperatures = source.temperatures.collect()
        destination.activationEnergy = source.activationEnergy.collect()
        destination.specificHeats = source.specificHeats.collect()
        destination.enthalpies = source.enthalpies.collect()
    }


    Data(Data data) {
        this.temperatures = data.temperatures.collect()
        this.specificHeats = data.specificHeats.collect()
        this.enthalpies = data.enthalpies.collect()
        this.activationEnergy = data.activationEnergy.collect()
    }


    Data(File file) throws CustomException {
        loadDataFromFile(file)
        interpolateSpecificHeats()
        temperatures.eachWithIndex { it, i ->
            activationEnergy[i] = 0.0 as Double
        }
        computeEnthalpy()
        DATA_LOADED = true
    }


    private void loadDataFromFile(File file) throws CustomException {
        String line
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file))
            int counter = 0
            bufferedReader.readLine() //ignore first line
            while ((line = bufferedReader.readLine()) != null) {
                String[] numbers = line.split(" ")
                temperatures[counter] = Double.parseDouble(numbers[0]).toInteger()
                specificHeats[counter] = Double.parseDouble(numbers[1])
                counter++
            }
            bufferedReader.close()
        }
        catch (FileNotFoundException ignored) {
            throw new CustomException("Plik '" + file.name + "' nie istnieje.")
        }
        catch (IOException ignored) {
            throw new CustomException("Błąd podczas odczytu pliku '" + file.name + "'.")
        }
        catch (Exception ignored) {
            throw new CustomException("Nie można odczytać pliku '" + file.name + "' - Zły format danych.")
        }
    }


    private void interpolateSpecificHeats() {
        def temperaturesTemp = temperatures.collect()
        def specificsHeatsTemp = specificHeats.collect()

        int counter = 0
        for (int i = 0; i < specificsHeatsTemp.size() - 1; i++) {
            for (int k = temperaturesTemp[i]; k < temperaturesTemp[i + 1]; k++) {
                specificHeats[counter] = interpolate(temperaturesTemp[i], specificsHeatsTemp[i], temperaturesTemp[i + 1], specificsHeatsTemp[i + 1], k)
                temperatures[counter] = k
                counter++
            }
        }

        temperatures += temperaturesTemp.last()
        specificHeats += specificsHeatsTemp.last()
    }


    private double interpolate(x1, y1, x2, y2, x) {
        def a = (y2 - y1) / (x2 - x1)
        def b = (-x1 * (y2 - y1) - (x2 - x1) * (-y1)) / (x2 - x1)
        return a * x + b
    }


    void addTransformation(Transformation transformation) {

        Decomposition decomposition = Decomposition.create(transformation.decompositionType)
        decomposition.transformationRange = transformation.temp_stop - transformation.temp_start
        decomposition.startIndex = transformation.temp_start - temperatures[0]
        decomposition.stopIndex = transformation.temp_stop - temperatures[0]
        decomposition.energy = transformation.energy

        decomposition.decomposeEnergy(activationEnergy)
        computeEnthalpy()
    }


    void computeEnthalpy() {
        int temperatureDelta = temperatures[1] - temperatures[0]
        double meanPartialSpecificHeat = (specificHeats[1] + specificHeats[0]) / 2

        enthalpies[0] = temperatureDelta * meanPartialSpecificHeat + activationEnergy[0]

        for (int i = 1; i < temperatures.size() - 1; i++) {
            temperatureDelta = temperatures[i + 1] - temperatures[i]
            meanPartialSpecificHeat = (specificHeats[i + 1] + specificHeats[i]) / 2

            enthalpies[i] = enthalpies[i - 1] + temperatureDelta * meanPartialSpecificHeat + activationEnergy[i]
        }
    }


    def getEnthalpies() {
        return enthalpies
    }


    def getTemperatures() {
        return temperatures
    }


    def getSpecificHeats() {
        return specificHeats
    }


    static boolean isDataLoaded() {
        return DATA_LOADED
    }

}