package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class StartController {
    @FXML private ImageView backgroundImage;
    @FXML private ImageView logoImage;
    @FXML private ImageView scarerButton;    // replaces scarerRadio
    @FXML private ImageView laugherButton;   // replaces laugherRadio
    @FXML private Button instructions_button;
    @FXML private Button exit_button;

    @FXML
    private void initialize() {
        System.out.println("DEBUG: StartController.initialize() called");
        if (scarerButton == null)  System.err.println("ERROR: scarerButton not injected");
        if (laugherButton == null) System.err.println("ERROR: laugherButton not injected");
    }

    @FXML
    private void handlePlayAsScarer(MouseEvent event) {
        System.out.println("DEBUG: SCARER selected");
        SceneManager.getInstance().startGameScreen(game.engine.Role.SCARER);
    }

    @FXML
    private void handlePlayAsLaugher(MouseEvent event) {
        System.out.println("DEBUG: LAUGHER selected");
        SceneManager.getInstance().startGameScreen(game.engine.Role.LAUGHER);
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