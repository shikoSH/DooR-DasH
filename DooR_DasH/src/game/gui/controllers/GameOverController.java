package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GameOverController {
    
    @FXML
    private Label winnerLabel;
    
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
