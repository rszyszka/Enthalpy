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
            Alert alert = new Alert(Alert.AlertType.ERROR)
            alert.setTitle("Błąd")
            alert.setHeaderText("Nie udało się wczytać pliku")
            alert.setContentText(ex.message)
            alert.showAndWait()
        } catch (Exception ignored) {
        }
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
                Alert alert = new Alert(Alert.AlertType.ERROR)
                alert.setTitle("Błąd")
                alert.setHeaderText("Nie udało się zapisać obrazu")
                alert.setContentText(ex.message)
                alert.showAndWait()
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
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR)
                alert.setTitle("Błąd")
                alert.setHeaderText("Błąd podczas zapisu wyników")
                alert.setContentText(ex.message)
                alert.showAndWait()
            }
        }
    }

    @FXML
    void addTransformation() {
        try {
            if (!data) {
                throw new CustomException("Najpierw musisz wczytać dane wejściowe!")
            }
            int temp_start = Integer.parseInt(tempStartField.getText())
            int temp_stop = Integer.parseInt(tempStopField.getText())
            if (temp_stop <= temp_start) {
                throw new CustomException("Temperatura początkowa musi być mniejsza od końcowej!")
            }
            if (temp_start < data.temperatures[0] || temp_stop > data.temperatures.last()) {
                throw new CustomException("Temperatura końcowa musi mieścić się w przedziale wczytanych temperatur!")
            }
            double energy = Double.parseDouble(energyField.getText())
            Decomposition decomposition = decompositionComboBox.getSelectionModel().getSelectedItem()

            transformations.each {
                if (temp_start <= it.temp_stop && it.temp_start <= temp_stop)
                    throw new CustomException("Przedział temperatur zawiera się w istniejącej już przemianie!")
            }

            Transformation transformation = new Transformation(temp_start, temp_stop, energy, decomposition)
            transformations.add(transformation)

        } catch (CustomException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR)
            alert.setTitle("Błąd")
            alert.setHeaderText("Niepoprawne dane")
            alert.setContentText(ex.message)
            alert.showAndWait()
        } catch (Exception ignored) {
            Alert alert = new Alert(Alert.AlertType.ERROR)
            alert.setTitle("Błąd")
            alert.setHeaderText("Niepoprawne dane")
            alert.setContentText("Musisz wprowadzić poprawne dane!")
            alert.showAndWait()
        }

    }

    @FXML
    void editTransformation() {
        Transformation selectedItem = tableView.getSelectionModel().getSelectedItem()
        if (selectedItem) {
            try {
                int temp_start = Integer.parseInt(tempStartField.getText())
                int temp_stop = Integer.parseInt(tempStopField.getText())
                if (temp_stop <= temp_start) {
                    throw new CustomException("Temperatura początkowa musi być mniejsza od końcowej!")
                }
                if (temp_start < data.temperatures[0] || temp_stop > data.temperatures.last()) {
                    throw new CustomException("Temperatura końcowa musi mieścić się w przedziale wczytanych temperatur!")
                }
                double energy = Double.parseDouble(energyField.getText())
                Decomposition decomposition = decompositionComboBox.getSelectionModel().getSelectedItem()

                transformations.each {
                    if (it != selectedItem) {
                        if (temp_start <= it.temp_stop && it.temp_start <= temp_stop)
                            throw new CustomException("Przedział temperatur zawiera się w istniejącej już przemianie!")
                    }
                }
                selectedItem.temp_start = temp_start
                selectedItem.temp_stop = temp_stop
                selectedItem.energy = energy
                selectedItem.decomposition = decomposition
                tableView.refresh()
            } catch (CustomException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR)
                alert.setTitle("Błąd")
                alert.setHeaderText("Niepoprawne dane")
                alert.setContentText(ex.message)
                alert.showAndWait()
            } catch (Exception ignored) {
                Alert alert = new Alert(Alert.AlertType.ERROR)
                alert.setTitle("Błąd")
                alert.setHeaderText("Niepoprawne dane")
                alert.setContentText("Musisz wprowadzić poprawne dane!")
                alert.showAndWait()
            }
        }
    }

    @FXML
    void deleteTransformation() {
        Transformation selectedItem = tableView.getSelectionModel().getSelectedItem()
        transformations.remove(selectedItem)
    }

    @FXML
    void computeEnthalpy() {
        if (Data.isDataLoaded()) {
            Data.copyValues(data, originData)
            transformations.each {
                data.addTransformation(it)
            }
        } else {
            raiseLoadDataError()
        }
    }

    @FXML
    void drawChart() {
        computeEnthalpy()
        if (Data.isDataLoaded()) {

            Data.copyValues(data, originData)
            transformations.each {
                data.addTransformation(it)
            }

            XYChart.Series series = new XYChart.Series()
            String seriesName = "Entalpia" + (SERIES_COUNTER++)
            series.setName(seriesName)
            data.enthalpies.eachWithIndex { enthalpy, index ->
                series.getData().add(new XYChart.Data(data.temperatures[index], enthalpy))
            }
            lineChart.getData().add(series)
        } else {
            raiseLoadDataError()
        }
    }

    @FXML
    void clearChart() {
        lineChart.data.clear()
        SERIES_COUNTER = 1
    }

    @FXML
    void closeApp() {
        Platform.exit()
    }

    private void raiseLoadDataError() {
        Alert alert = new Alert(Alert.AlertType.ERROR)
        alert.setTitle("Błąd")
        alert.setHeaderText("Brak danych")
        alert.setContentText("Musisz najpierw wczytać dane!")
        alert.showAndWait()
    }
}