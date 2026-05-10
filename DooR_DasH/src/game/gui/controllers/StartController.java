package game.gui.controllers;

import javafx.fxml.FXML;

public class StartController {
    
    @FXML
    private void initialize() {
        // Initialization logic here
    }

    @FXML
    private javafx.scene.control.RadioButton scarerRadio;

    @FXML
    private void handleStartGame() {
        game.engine.Role role = scarerRadio.isSelected() ? game.engine.Role.SCARER : game.engine.Role.LAUGHER;
        SceneManager.getInstance().startGameScreen(role);
    }

    @FXML
    private void handleInstructions() {
        SceneManager.getInstance().switchToInstructionsScreen();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}
