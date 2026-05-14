package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.engine.Board;
import game.engine.cards.Card;
import game.engine.cells.*;

public class GameController {

    // === FXML injected from GameScreen.fxml ===
    @FXML private StackPane boardContainer;
    @FXML private GridPane grid;
    @FXML private VBox playerPanelContainer;
    @FXML private VBox opponentPanelContainer;
    @FXML private VBox actionLogContainer;
    @FXML private VBox diceContainer;
    @FXML private Label myLabel;  // the top "Messages" label in FXML

    // === Labels we create and inject into the FXML VBoxes ===
    private Label playerNameLabel;
    private Label playerPosLabel;
    private Label playerEnergyLabel;
    private Label playerTurnLabel;

    private Label opponentNameLabel;
    private Label opponentPosLabel;
    private Label opponentEnergyLabel;

    private Label diceResultLabel;
    private Label actionLine1;
    private Label actionLine2;
    private Label actionLine3;

    // === Images ===
    private Image normalImage;
    private Image ScarerdoorImage;
    private Image laugherdoorImage;
    private Image monsterImage_celia_mae;
    private Image monsterImage_Fungus;
    private Image monsterImage_Henry_J_Waternoose_III;
    private Image monsterImage_James_sullivan;
    private Image monsterImage_Mike_Wazowski;
    private Image monsterImage_Randall;
    private Image monsterImage_Roz;
    private Image monsterImage_Yeti;
    private Image conveyorImage;
    private Image contaminationImage;
    private Image cardImage;

    private static final String IMG = "/game/gui/resources/images/";

    // === Grid cell views ===
    private ImageView[][] cellViews;

    // === Game state ===
    private Game game;

    @FXML
    private void initialize() {
        // --- Load images ---
        normalImage                         = loadImage(IMG + "NormalCell.png");
        ScarerdoorImage                     = loadImage(IMG + "Scarer_ClosedDoor_Cell2.png");
        laugherdoorImage                    = loadImage(IMG + "Laugher_ClosedDoor_Cell.png");
        monsterImage_celia_mae              = loadImage(IMG + "celia mae.png");
        monsterImage_Fungus                 = loadImage(IMG + "Fungus.png");
        monsterImage_Henry_J_Waternoose_III = loadImage(IMG + "Henry_J._Waternoose_III.png");
        monsterImage_James_sullivan         = loadImage(IMG + "James sullivan.png");
        monsterImage_Mike_Wazowski          = loadImage(IMG + "Mike_Wazowski.png");
        monsterImage_Randall                = loadImage(IMG + "Randall.png");
        monsterImage_Roz                    = loadImage(IMG + "Roz.png");
        monsterImage_Yeti                   = loadImage(IMG + "Yeti.png");
        conveyorImage                       = loadImage(IMG + "conveyor.png");
        contaminationImage                  = loadImage(IMG + "sock.png");
        cardImage                           = loadImage(IMG + "card.png");

        // --- Set up grid ImageViews (37x37 to match FXML constraints) ---
        cellViews = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                ImageView iv = new ImageView();
                iv.setFitWidth(37);
                iv.setFitHeight(37);
                iv.setPreserveRatio(false);
                cellViews[row][col] = iv;
                grid.add(iv, col, row);
            }
        }

        // --- Build player panel inside the FXML VBox ---
        playerPanelContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label pTitle = makeLabel("YOUR MONSTER", "#ffcc00", 13, true);
        playerNameLabel   = makeLabel("-", "white", 12, false);
        playerPosLabel    = makeLabel("Pos: -", "white", 11, false);
        playerEnergyLabel = makeLabel("Energy: -", "white", 11, false);
        playerTurnLabel   = makeLabel("", "#00ff88", 12, true);
        playerPanelContainer.getChildren().addAll(
            pTitle, playerNameLabel, playerPosLabel, playerEnergyLabel, playerTurnLabel
        );

        // --- Build opponent panel inside the FXML VBox ---
        opponentPanelContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label oTitle = makeLabel("OPPONENT", "#ff6666", 13, true);
        opponentNameLabel   = makeLabel("-", "white", 12, false);
        opponentPosLabel    = makeLabel("Pos: -", "white", 11, false);
        opponentEnergyLabel = makeLabel("Energy: -", "white", 11, false);
        opponentPanelContainer.getChildren().addAll(
            oTitle, opponentNameLabel, opponentPosLabel, opponentEnergyLabel
        );

        // --- Build dice display inside the FXML VBox ---
        diceContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label dTitle = makeLabel("DICE", "#ffcc00", 13, true);
        diceResultLabel = makeLabel("Press Roll!", "white", 14, true);
        diceContainer.getChildren().addAll(dTitle, diceResultLabel);

        // --- Build action log inside the FXML VBox ---
        actionLogContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label aTitle = makeLabel("ACTION LOG", "#ffcc00", 13, true);
        actionLine1 = makeLabel("", "white", 11, false);
        actionLine2 = makeLabel("", "white", 11, false);
        actionLine3 = makeLabel("", "white", 11, false);
        actionLine1.setWrapText(true);
        actionLine2.setWrapText(true);
        actionLine3.setWrapText(true);
        actionLogContainer.getChildren().addAll(aTitle, actionLine1, actionLine2, actionLine3);

        System.out.println("DEBUG: GameController.initialize() complete");
    }

    // Helper to make styled labels
    private Label makeLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: " + size + "px; " +
            (bold ? "-fx-font-weight: bold;" : "")
        );
        l.setWrapText(true);
        l.setMaxWidth(140);
        return l;
    }

    private Image loadImage(String path) {
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("WARNING: Image not found: " + path);
            return null;
        }
        System.out.println("OK: Loaded " + path);
        return new Image(stream);
    }

    public void startGame(Role playerRole) {
        try {
            this.game = new Game(playerRole);
            System.out.println("DEBUG: Game started. Player=" + game.getPlayer().getName()
                + " Opponent=" + game.getOpponent().getName());
            refreshBoard();
            updateUI();
        } catch (Exception e) {
            System.err.println("ERROR: startGame() failed");
            e.printStackTrace();
        }
    }

    private void refreshBoard() {
        Cell[][] cells = game.getBoard().getBoardCells();
        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                Cell cell = cells[row][col];
                if (cell != null) setCellImage(row, col, cell);
            }
        }
    }

    private void setCellImage(int row, int col, Cell cell) {
        if (cell instanceof MonsterCell) {
            Monster m = ((MonsterCell) cell).getMonster();
            if (m == null) { cellViews[row][col].setImage(normalImage); return; }
            switch (m.getName()) {
                case "Celia Mae":                cellViews[row][col].setImage(monsterImage_celia_mae); break;
                case "Fungus":                   cellViews[row][col].setImage(monsterImage_Fungus); break;
                case "Henry J. Waternoose III":  cellViews[row][col].setImage(monsterImage_Henry_J_Waternoose_III); break;
                case "James Sullivan":           cellViews[row][col].setImage(monsterImage_James_sullivan); break;
                case "Mike Wazowski":            cellViews[row][col].setImage(monsterImage_Mike_Wazowski); break;
                case "Randall":                  cellViews[row][col].setImage(monsterImage_Randall); break;
                case "Roz":                      cellViews[row][col].setImage(monsterImage_Roz); break;
                case "Yeti":                     cellViews[row][col].setImage(monsterImage_Yeti); break;
                default:                         cellViews[row][col].setImage(normalImage); break;
            }
        } else if (cell instanceof DoorCell) {
            cellViews[row][col].setImage(
                ((DoorCell) cell).getRole() == Role.SCARER ? ScarerdoorImage : laugherdoorImage);
        } else if (cell instanceof ConveyorBelt) {
            cellViews[row][col].setImage(conveyorImage);
        } else if (cell instanceof ContaminationSock) {
            cellViews[row][col].setImage(contaminationImage);
        } else if (cell instanceof CardCell) {
            cellViews[row][col].setImage(cardImage);
        } else {
            cellViews[row][col].setImage(normalImage);
        }
    }

    private void updateUI() {
        if (game == null) return;

        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        Monster current  = game.getCurrent();

        // Player panel
        playerNameLabel.setText(player.getName());
        playerPosLabel.setText("Pos: " + player.getPosition());
        playerEnergyLabel.setText("Energy: " + player.getEnergy());
        playerTurnLabel.setText(current == player ? "▶ YOUR TURN" : "");

        // Opponent panel
        opponentNameLabel.setText(opponent.getName());
        opponentPosLabel.setText("Pos: " + opponent.getPosition());
        opponentEnergyLabel.setText("Energy: " + opponent.getEnergy());

        // Top message label from FXML
        myLabel.setText(current == player ? "Your turn — press Roll!" : "Opponent's turn — press Roll!");
        myLabel.setStyle("-fx-font-size: 16px; -fx-padding: 8 0 8 0; -fx-text-fill: " 
            + (current == player ? "#00ff88" : "#ff6666") + ";");
    }

    @FXML
    private void handleRollDice() {
        if (game == null) return;
        try {
            Monster current  = game.getCurrent();
            Monster opponent = current == game.getPlayer() ? game.getOpponent() : game.getPlayer();

            int oldPos       = current.getPosition();
            int oldEnergy    = current.getEnergy();
            int oldOppEnergy = opponent.getEnergy();

            if (Board.cards.isEmpty()) Board.reloadCards();
            Card topCard = Board.cards.isEmpty() ? null : Board.cards.get(0);

            game.playTurn();

            int newPos       = current.getPosition();
            int newEnergy    = current.getEnergy();
            int newOppEnergy = opponent.getEnergy();

            boolean cardWasDrawn = topCard != null &&
                (Board.cards.isEmpty() || Board.cards.get(0) != topCard);

            int moved = newPos - oldPos;
            if (moved < 0) moved += 100;

            // Update dice
            diceResultLabel.setText("🎲 " + moved);

            // Update action log (shift lines down)
            String line1 = current.getName() + " moved " + moved + " spaces.";
            String line2 = "";
            String line3 = "";

            if (newPos == 0 && oldPos != 0 && moved != 0)
                line2 = "⚠ SENT BACK TO START!";
            else if (cardWasDrawn && topCard != null)
                line2 = "🃏 Drew: " + topCard.getName();

            if (newEnergy != oldEnergy)
                line3 = current.getName() + " energy: " + oldEnergy + "→" + newEnergy;
            else if (newOppEnergy != oldOppEnergy)
                line3 = opponent.getName() + " energy: " + oldOppEnergy + "→" + newOppEnergy;

            actionLine1.setText(line1);
            actionLine2.setText(line2);
            actionLine3.setText(line3);

            refreshBoard();
            updateUI();

            if (game.getWinner() != null) {
                myLabel.setText(game.getWinner().getName() + " WINS! 🎉");
                myLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffcc00;");
                SceneManager.getInstance().switchToGameOverScreen();
            }

        } catch (game.engine.exceptions.InvalidMoveException e) {
            // This is normal gameplay — just show the message and let them roll again
            actionLine1.setText("⚠ " + e.getMessage());
            actionLine2.setText("Roll again!");
            actionLine3.setText("");
            // Still refresh UI so board stays consistent
            refreshBoard();
            updateUI();
        } catch (Exception e) {
            actionLine1.setText("Error: " + e.getMessage());
            System.err.println("ERROR in handleRollDice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePowerUp() {
        if (game == null) return;
        boolean confirmed = showConfirmDialog("Use Powerup", "Activate Powerup?",
                "This will consume energy. Do you want to proceed?");
        if (confirmed) {
            try {
                String name = game.getCurrent().getName();
                game.usePowerup();
                actionLine1.setText(name + " used Powerup!");
                actionLine2.setText("");
                actionLine3.setText("");
                updateUI();
            } catch (Exception e) {
                showErrorAlert("Powerup Failed", e.getMessage());
                actionLine1.setText("Powerup failed: " + e.getMessage());
            }
        }
    }

    private boolean showConfirmDialog(String title, String header, String content) {
        final boolean[] result = {false};
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Label headerLabel = new Label(header);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        okButton.setOnAction(e -> { result[0] = true; dialog.close(); });
        cancelButton.setOnAction(e -> dialog.close());
        HBox buttons = new HBox(10, okButton, cancelButton);
        buttons.setAlignment(javafx.geometry.Pos.CENTER);
        VBox layout = new VBox(12, headerLabel, contentLabel, buttons);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        dialog.setScene(new Scene(layout, 320, 160));
        dialog.showAndWait();
        return result[0];
    }

    private void showErrorAlert(String title, String message) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Label headerLabel = new Label("Invalid Action");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: red;");
        Label contentLabel = new Label(message != null ? message : "Unknown error");
        contentLabel.setWrapText(true);
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialog.close());
        HBox buttons = new HBox(okButton);
        buttons.setAlignment(javafx.geometry.Pos.CENTER);
        VBox layout = new VBox(12, headerLabel, contentLabel, buttons);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        dialog.setScene(new Scene(layout, 320, 140));
        dialog.showAndWait();
    }
}