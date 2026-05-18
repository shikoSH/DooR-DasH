package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameOverController {

    @FXML private Label     winsLabel;
    @FXML private StackPane rootPane;
    @FXML private javafx.scene.image.ImageView backgroundImage;
    @FXML private Button    retryButton;
    @FXML private Button    mainMenuButton;
    @FXML private AnchorPane overlayPane;

    // Stat card labels — player
    private Label playerCardTitle;
    private Label playerCardName;
    private Label playerCardRole;
    private Label playerCardEnergy;

    // Stat card labels — opponent
    private Label opponentCardTitle;
    private Label opponentCardName;
    private Label opponentCardRole;
    private Label opponentCardEnergy;

    private game.engine.Role lastPlayerRole;

    @FXML
    private void initialize() {
        if (backgroundImage != null && rootPane != null) {
            backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
            overlayPane.prefWidthProperty().bind(rootPane.widthProperty());
            overlayPane.prefHeightProperty().bind(rootPane.heightProperty());
        }
        buildStatCards();
    }

    private void buildStatCards() {
        // ── Player stat card ──────────────────────────────────
        playerCardTitle  = makeLabel("YOUR MONSTER", "#ffcc00", 13, true);
        playerCardName   = makeLabel("-", "white", 15, true);
        playerCardRole   = makeLabel("Role: -", "#00ffff", 12, false);
        playerCardEnergy = makeLabel("Final Energy: -", "#00ff88", 12, true);

        VBox playerCard = new VBox(8,
            playerCardTitle, playerCardName, playerCardRole, playerCardEnergy);
        playerCard.setAlignment(Pos.CENTER);
        playerCard.setStyle(
            "-fx-background-color: rgba(0,0,0,0.70);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #ffcc00;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 2;" +
            "-fx-padding: 20;");
        playerCard.setPrefWidth(220);

        // ── Opponent stat card ────────────────────────────────
        opponentCardTitle  = makeLabel("OPPONENT", "#ff6666", 13, true);
        opponentCardName   = makeLabel("-", "white", 15, true);
        opponentCardRole   = makeLabel("Role: -", "#00ffff", 12, false);
        opponentCardEnergy = makeLabel("Final Energy: -", "#ff6666", 12, true);

        VBox opponentCard = new VBox(8,
            opponentCardTitle, opponentCardName, opponentCardRole, opponentCardEnergy);
        opponentCard.setAlignment(Pos.CENTER);
        opponentCard.setStyle(
            "-fx-background-color: rgba(0,0,0,0.70);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #ff6666;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 2;" +
            "-fx-padding: 20;");
        opponentCard.setPrefWidth(220);

        // ── Row holding both cards ────────────────────────────
        HBox statsRow = new HBox(40, playerCard, opponentCard);
        statsRow.setAlignment(Pos.CENTER);

        // Pin the row near the bottom of the overlay
        overlayPane.getChildren().add(statsRow);
        AnchorPane.setBottomAnchor(statsRow, 110.0);
        AnchorPane.setLeftAnchor(statsRow,   0.0);
        AnchorPane.setRightAnchor(statsRow,  0.0);
    }

    // Called from SceneManager
    public void setWinner(String winnerName, String winnerRole,
                          game.engine.Role playerRole,
                          String playerName,   String playerRoleStr,   int playerEnergy,
                          String opponentName, String opponentRoleStr, int opponentEnergy) {

        // ── Winner banner ─────────────────────────────────────
        winsLabel.setText(winnerName + " WINS!  |  Role: " + winnerRole);
        this.lastPlayerRole = playerRole;

        // ── Player card ───────────────────────────────────────
        boolean playerWon = playerName.equals(winnerName);
        playerCardTitle.setText(playerWon ? "★ YOUR MONSTER  ★" : "YOUR MONSTER");
        playerCardTitle.setStyle(
            "-fx-text-fill: " + (playerWon ? "#ffcc00" : "#aaaaaa") + ";" +
            "-fx-font-size: 13px; -fx-font-weight: bold;");
        playerCardName.setText(playerName);
        playerCardRole.setText("Role: " + playerRoleStr);
        playerCardEnergy.setText("Final Energy: " + playerEnergy);

        // ── Opponent card ─────────────────────────────────────
        boolean opponentWon = opponentName.equals(winnerName);
        opponentCardTitle.setText(opponentWon ? "★ OPPONENT  ★" : "OPPONENT");
        opponentCardTitle.setStyle(
            "-fx-text-fill: " + (opponentWon ? "#ffcc00" : "#aaaaaa") + ";" +
            "-fx-font-size: 13px; -fx-font-weight: bold;");
        opponentCardName.setText(opponentName);
        opponentCardRole.setText("Role: " + opponentRoleStr);
        opponentCardEnergy.setText("Final Energy: " + opponentEnergy);
    }

    // Kept for the cheat W key path which calls the old 3-arg version
    public void setWinner(String winnerName, String winnerRole,
                          game.engine.Role playerRole) {
        winsLabel.setText(winnerName + " WINS!  |  Role: " + winnerRole);
        this.lastPlayerRole = playerRole;
    }

    @FXML
    private void handleReplay() {
        SceneManager.getInstance().startGameScreen(lastPlayerRole);
    }

    @FXML
    private void handleMainMenu() {
        SceneManager.getInstance().switchToStartScreen();
    }

    private Label makeLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-text-fill: " + color + ";" +
            "-fx-font-size: " + size + "px;" +
            (bold ? "-fx-font-weight: bold;" : ""));
        l.setWrapText(true);
        l.setMaxWidth(200);
        l.setAlignment(Pos.CENTER);
        return l;
    }
}