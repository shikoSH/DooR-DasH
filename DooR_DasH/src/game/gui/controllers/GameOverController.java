package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class GameOverController {

    @FXML private Label winsLabel;
    @FXML private StackPane rootPane;
    @FXML private javafx.scene.image.ImageView backgroundImage;
    @FXML private Button retryButton;
    @FXML private Button mainMenuButton;

    private game.engine.Role lastPlayerRole;


    @FXML private AnchorPane overlayPane;

    @FXML
    private void initialize() {
        if (backgroundImage != null && rootPane != null) {
            backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
            overlayPane.prefWidthProperty().bind(rootPane.widthProperty());
            overlayPane.prefHeightProperty().bind(rootPane.heightProperty());
        }
    }

    // Called from SceneManager when switching to this screen
    public void setWinner(String winnerName, String winnerRole, game.engine.Role playerRole) {
        winsLabel.setText(winnerName + " WINS!  |  Role: " + winnerRole);
        this.lastPlayerRole = playerRole;
    }

    @FXML
    private void handleReplay() {
        // Restart with the same role the player originally chose
        SceneManager.getInstance().startGameScreen(lastPlayerRole);
    }

    @FXML
    private void handleMainMenu() {
        SceneManager.getInstance().switchToStartScreen();
    }
}