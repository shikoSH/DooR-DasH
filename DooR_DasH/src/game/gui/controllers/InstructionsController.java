package game.gui.controllers;

import javafx.fxml.FXML;

public class InstructionsController {
    
    @FXML
    private void handleBack() {
        SceneManager.getInstance().switchToStartScreen();
    }
}
