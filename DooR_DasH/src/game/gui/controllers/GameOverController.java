package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class GameOverController {
    
    @FXML
    private Label winnerLabel;
    @FXML private StackPane rootPane;
    @FXML private javafx.scene.image.ImageView backgroundImage;

    @FXML
    private void initialize() {
        if (backgroundImage != null && rootPane != null) {
            backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }
    public void setWinner(String winnerName) {
        winnerLabel.setText(winnerName + " WINS!");
    }

    @FXML
    private void handleReplay() {
        // We'll need to re-initialize the game screen
        SceneManager.getInstance().switchToStartScreen();
    }

    @FXML
    private void handleMenu() {
        SceneManager.getInstance().switchToStartScreen();
    }
}