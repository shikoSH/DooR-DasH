package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class StartController {
    
	// all our variables in the start
    @FXML
    private ImageView backgroundImage;
    

    @FXML
    private javafx.scene.control.RadioButton scarerRadio;

    private void initialize() {
		// Initialization logic here
        // Check if image loaded
        if (backgroundImage.getImage() == null) {
            System.err.println("ERROR: Background image is NULL - file not found!");
        } else if (backgroundImage.getImage().isError()) {
            System.err.println("ERROR: Background image failed to load!");
        } else {
            System.out.println("OK: Background image loaded successfully.");
        }
    	
    }

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
