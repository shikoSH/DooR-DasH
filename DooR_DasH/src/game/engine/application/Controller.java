package game.engine.application;


import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Controller {

    @FXML
    private Label myLabel;

    @FXML
    private void buttonClicked() {

        myLabel.setText("Hello JavaFX!");
    }
}

