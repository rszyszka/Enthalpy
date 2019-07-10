import javafx.fxml.FXMLLoader
import javafx.scene.Parent

import static groovyx.javafx.GroovyFX.start

start {
    stage(title: 'Entalpia', visible: true) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/enthalpy.fxml"))
        Parent root = loader.load()
        scene(root: root) {}
    }
}