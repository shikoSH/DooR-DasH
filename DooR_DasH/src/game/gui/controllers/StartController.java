package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class StartController {

    // Images
    @FXML private ImageView backgroundImage;
    @FXML private ImageView logoImage;

    // Radio buttons — fx:id must match FXML exactly
    @FXML private RadioButton scarerRadio;
    @FXML private RadioButton laugherRadio;

    // Buttons — fx:id must match FXML exactly
    @FXML private Button start_game_button;
    @FXML private Button instructions_button;
    @FXML private Button exit_button;

    // Base path to your images folder
    private static final String IMG_PATH =
        "D:/Desktop/Game project/Repo directory/DooR-DasH/DooR_DasH/src/game/gui/resources/images/";

    // Called automatically by JavaFX when FXML finishes loading
    @FXML
    private void initialize() {
        loadImage(backgroundImage, "StartScreen_Background.png");
        loadImage(logoImage,       "DoorDash_Logo.png");
    }

    // Helper to load an image from disk into an ImageView
    private void loadImage(ImageView view, String filename) {
        File file = new File(IMG_PATH + filename);
        if (file.exists()) {
            view.setImage(new Image(file.toURI().toString()));
            System.out.println("OK: Loaded " + filename);
        } else {
            System.err.println("ERROR: Not found: " + file.getAbsolutePath());
        }
    }

    // Linked to start_game_button via onAction="#handleStartGame"
    @FXML
    private void handleStartGame() {
        game.engine.Role role = scarerRadio.isSelected()
            ? game.engine.Role.SCARER
            : game.engine.Role.LAUGHER;
        SceneManager.getInstance().startGameScreen(role);
    }

    // Linked to instructions_button via onAction="#handleInstructions"
    @FXML
    private void handleInstructions() {
        SceneManager.getInstance().switchToInstructionsScreen();
    }

    // Linked to exit_button via onAction="#handleExit"
    @FXML
    private void handleExit() {
        System.exit(0);
    }
}