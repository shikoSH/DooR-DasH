package game.gui.controllers;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class StartController {
    @FXML private ImageView backgroundImage;
    @FXML private ImageView logoImage;
    @FXML private ImageView scarerButton;
    @FXML private ImageView laugherButton;
    @FXML private Button instructions_button;
    @FXML private Button exit_button;
    @FXML private StackPane rootPane; // Add this line
    @FXML private ImageView backgroundImage1;

    @FXML
    private void initialize() {
        // --- Make background fill the screen ---
        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        // ... Keep all your existing glow/hover code below this! ...
    	// --- SCARER glow + grows on hover ---
    	DropShadow scarerGlow = new DropShadow();
    	scarerGlow.setColor(Color.RED);
    	scarerGlow.setRadius(25);
    	scarerGlow.setSpread(0.6);

    	scarerButton.setOnMouseEntered(e -> {
    	    scarerButton.setEffect(scarerGlow);
    	    playScale(scarerButton, 1.0, 1.1, 200); // grow
    	});
    	scarerButton.setOnMouseExited(e -> {
    	    scarerButton.setEffect(null);
    	    playScale(scarerButton, 1.1, 1.0, 200); // back to normal
    	});

        // --- LAUGHER glow + grows on hover ---
        DropShadow laugherGlow = new DropShadow();
        laugherGlow.setColor(Color.YELLOW);
        laugherGlow.setRadius(25);
        laugherGlow.setSpread(0.6);

        laugherButton.setOnMouseEntered(e -> {
            laugherButton.setEffect(laugherGlow);
            playScale(laugherButton, 1.0, 1.1, 200); // grow
        });
        laugherButton.setOnMouseExited(e -> {
            laugherButton.setEffect(null);
            playScale(laugherButton, 1.1, 1.0, 200); // back to normal
        });
    }

    // Smooth scale animation helper
    private void playScale(ImageView target, double from, double to, int durationMs) {
        ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), target);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        st.play();
    }

    @FXML
    private void handlePlayAsScarer(MouseEvent event) {
        SceneManager.getInstance().startGameScreen(game.engine.Role.SCARER);
    }

    @FXML
    private void handlePlayAsLaugher(MouseEvent event) {
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