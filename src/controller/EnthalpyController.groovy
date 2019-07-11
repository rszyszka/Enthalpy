package controller

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.scene.SnapshotParameters
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.WritableImage
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import model.CustomException
import model.Data
import model.Decomposition
import model.Transformation

import javax.imageio.ImageIO


class EnthalpyController {

    static int SERIES_COUNTER = 1

    private ObservableList<Transformation> transformations
    private Data data
    private Data originData

    @FXML
    AnchorPane rootAnchorPane
    @FXML
    LineChart<Double, Double> lineChart
    @FXML
    TableView<Transformation> tableView
    @FXML
    TableColumn tempStartColumn
    @FXML
    TableColumn tempStopColumn
    @FXML
    TableColumn energyColumn
    @FXML
    TableColumn decompositionColumn
    @FXML
    TextField tempStartField
    @FXML
    TextField tempStopField
    @FXML
    TextField energyField
    @FXML
    ComboBox<Decomposition> decompositionComboBox
    @FXML
    MenuItem saveResultsMenuItem
    @FXML
    MenuItem saveChartMenuItem


    @FXML
    void initialize() {
        initializeTransformationsTableView()
        initializeLineChartAxis()
        initializeDecompositionComboBox()
        saveResultsMenuItem.setDisable(true)
        saveChartMenuItem.setDisable(true)
    }


    private void initializeTransformationsTableView() {
        transformations = FXCollections.observableArrayList()
        tempStartColumn.setCellValueFactory(
                new PropertyValueFactory<Transformation, Integer>("temp_start")
        )
        tempStopColumn.setCellValueFactory(
                new PropertyValueFactory<Transformation, Integer>("temp_stop")
        )
        energyColumn.setCellValueFactory(
                new PropertyValueFactory<Transformation, Double>("energy")
        )
        decompositionColumn.setCellValueFactory(
                new PropertyValueFactory<Transformation, Decomposition>("decomposition")
        )
        tableView.setEditable(true)
        tableView.setItems(transformations)
    }


    private void initializeLineChartAxis() {
        lineChart.getXAxis().label = "Temperatura [°C]"
        lineChart.getYAxis().label = "Entalpia [J/g]"
        lineChart.setCreateSymbols(false)
    }


    private void initializeDecompositionComboBox() {
        decompositionComboBox.setItems(FXCollections.observableArrayList(Decomposition.values()))
        decompositionComboBox.getSelectionModel().selectFirst()
    }


    @FXML
    void loadFile() {
        FileChooser fileChooser = new FileChooser()
        fileChooser.initialDirectory = new File(".")
        File file = fileChooser.showOpenDialog((Stage) rootAnchorPane.getScene().getWindow())
        try {
            originData = new Data(file)
            data = new Data(originData)
            saveResultsMenuItem.setDisable(false)
            saveChartMenuItem.setDisable(false)
            drawChart()
        } catch (CustomException ex) {
            raiseErrorAlert("Nie udało się wczytać pliku", ex.message)
        } catch (Exception ignored) {
        }
    }


    @FXML
    void clearChart() {
        lineChart.data.clear()
        SERIES_COUNTER = 1
    }


    @FXML
    void drawChart() {
        if (Data.isDataLoaded()) {
            reAddTransformations()
            addEnthalpySeries()
        } else {
            raiseErrorAlert("Brak danych", "Musisz najpierw wczytać dane!")
        }
    }


    private void reAddTransformations() {
        Data.copyValues(originData, data)
        transformations.each {
            data.addTransformation(it)
        }
    }


    private void addEnthalpySeries() {
        XYChart.Series series = new XYChart.Series()
        String seriesName = "Entalpia" + (SERIES_COUNTER++)
        series.setName(seriesName)
        data.enthalpies.eachWithIndex { enthalpy, index ->
            series.getData().add(new XYChart.Data(data.temperatures[index], enthalpy))
        }
        lineChart.getData().add(series)
    }


    @FXML
    void saveChart() {
        WritableImage image = lineChart.snapshot(new SnapshotParameters(), null)
        FileChooser fileChooser = new FileChooser()
        fileChooser.setTitle("Zapisz wykres")
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png")
        )
        fileChooser.initialDirectory = new File(".")
        File file = fileChooser.showSaveDialog((Stage) rootAnchorPane.getScene().getWindow())
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
            } catch (IOException ex) {
                raiseErrorAlert("Nie udało się zapisać obrazu", ex.message)
            }
        }
    }


    @FXML
    void saveResults() {
        FileChooser fileChooser = new FileChooser()
        fileChooser.setTitle("Zapisz wyniki")
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT", "*.txt")
        )
        fileChooser.initialDirectory = new File(".")
        File file = fileChooser.showSaveDialog((Stage) rootAnchorPane.getScene().getWindow())
        if (file != null) {
            try {
                writeResultsToFile(file)
            } catch (IOException ex) {
                raiseErrorAlert("Błąd podczas zapisu wyników", ex.message)
            }
        }
    }


    private void writeResultsToFile(File file) {
        PrintWriter writer = new PrintWriter(file)
        writer.println("PRZEMIANY")
        transformations.eachWithIndex { it, i ->
            writer.println("Przemiana $i Temp_start: $it.temp_start [°C] Temp_stop: $it.temp_stop [°C] Entalpia: $it.energy [J/g] Rozkład: $it.decomposition")
        }
        writer.println("\nWYNIKI")
        writer.println("Temperatura [°C]\tEntalpia [J/g]")
        data.enthalpies.eachWithIndex { enthalpy, i ->
            writer.println(data.temperatures[i] + "\t\t\t$enthalpy")
        }
        writer.close()
    }


    @FXML
    void deleteTransformation() {
        Transformation selectedItem = tableView.getSelectionModel().getSelectedItem()
        transformations.remove(selectedItem)
    }


    @FXML
    void addTransformation() {
        try {
            if (!data) {
                throw new CustomException("Najpierw musisz wczytać dane wejściowe!")
            }
            int temp_start = Integer.parseInt(tempStartField.getText())
            int temp_stop = Integer.parseInt(tempStopField.getText())

            checkOverlappingConditions(temp_start, temp_stop)

            double energy = Double.parseDouble(energyField.getText())
            Decomposition decomposition = decompositionComboBox.getSelectionModel().getSelectedItem()

            Transformation transformation = new Transformation(temp_start, temp_stop, energy, decomposition)
            transformations.add(transformation)

        } catch (CustomException ex) {
            raiseErrorAlert("Niepoprawne dane", ex.message)
        } catch (Exception ignored) {
            raiseErrorAlert("Niepoprawne dane", "Musisz wprowadzić poprawne dane!")
        }
    }


    @FXML
    void editTransformation() {
        Transformation selectedItem = tableView.getSelectionModel().getSelectedItem()
        if (selectedItem) {
            try {
                Decomposition decomposition = decompositionComboBox.getSelectionModel().getSelectedItem()
                double energy = Double.parseDouble(energyField.getText())
                int temp_start = Integer.parseInt(tempStartField.getText())
                int temp_stop = Integer.parseInt(tempStopField.getText())

                checkOverlappingConditions(temp_start, temp_stop, selectedItem)

                selectedItem.temp_start = temp_start
                selectedItem.temp_stop = temp_stop
                selectedItem.energy = energy
                selectedItem.decomposition = decomposition

                tableView.refresh()

            } catch (CustomException ex) {
                raiseErrorAlert("Niepoprawne dane", ex.message)
            } catch (Exception ignored) {
                raiseErrorAlert("Niepoprawne dane", "Musisz wprowadzić poprawne dane!")
            }
        }
    }


    private void checkOverlappingConditions(int temp_start, int temp_stop, Transformation selectedItem = null) throws CustomException {
        if (temp_start >= temp_stop) {
            throw new CustomException("Temperatura początkowa musi być mniejsza od końcowej!")
        }
        if (temp_start < data.temperatures[0] || temp_stop > data.temperatures.last()) {
            throw new CustomException("Temperatura początkowa i końcowa musi mieścić się w przedziale wczytanych z pliku temperatur!")
        }
        transformations.each {
            if (it != selectedItem) {
                if (temp_start <= it.temp_stop && it.temp_start <= temp_stop) {
                    throw new CustomException("Przedział temperatur zawiera się w istniejącej już przemianie!")
                }
            }
        }
    }


    private void raiseErrorAlert(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR)
        alert.setTitle("Błąd")
        alert.setHeaderText(headerText)
        alert.setContentText(contentText)
        alert.showAndWait()
    }


    @FXML
    void closeApp() {
        Platform.exit()
    }

}