package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

public class StartController {

    @FXML private ImageView backgroundImage;
    @FXML private ImageView logoImage;

    @FXML private ToggleGroup roleGroup;
    @FXML private RadioButton scarerRadio;
    @FXML private RadioButton laugherRadio;

    @FXML private Button start_game_button;
    @FXML private Button instructions_button;
    @FXML private Button exit_button;

    @FXML
    private void initialize() {
        System.out.println("DEBUG: StartController.initialize() called");

        if (scarerRadio == null)   System.err.println("ERROR: scarerRadio not injected");
        if (laugherRadio == null)  System.err.println("ERROR: laugherRadio not injected");
        if (roleGroup == null)     System.err.println("ERROR: roleGroup not injected");
        if (start_game_button == null) System.err.println("ERROR: start_game_button not injected");

        if (scarerRadio != null && laugherRadio != null && roleGroup != null) {
            System.out.println("DEBUG: All FXML fields injected OK");
        }
    }

    @FXML
    private void handleStartGame() {
        System.out.println("DEBUG: handleStartGame() called");

        if (roleGroup == null || roleGroup.getSelectedToggle() == null) {
            System.err.println("ERROR: No role selected or roleGroup is null");
            return;
        }

        game.engine.Role role = scarerRadio.isSelected()
            ? game.engine.Role.SCARER
            : game.engine.Role.LAUGHER;

        System.out.println("DEBUG: Role selected = " + role);
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