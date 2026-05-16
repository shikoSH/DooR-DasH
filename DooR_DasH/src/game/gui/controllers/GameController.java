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
import javafx.geometry.Pos;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.engine.Board;
import game.engine.cards.Card;
import game.engine.cells.*;

public class GameController {

    // === FXML injected ===
    @FXML private StackPane boardContainer;
    @FXML private GridPane grid;
    @FXML private VBox playerPanelContainer;
    @FXML private VBox opponentPanelContainer;
    @FXML private VBox actionLogContainer;
    @FXML private VBox diceContainer;
    @FXML private Label myLabel;

    // === Player panel labels ===
    private Label playerNameLabel;
    private Label playerPosLabel;
    private Label playerEnergyLabel;
    private Label playerTurnLabel;

    // === Opponent panel labels ===
    private Label opponentNameLabel;
    private Label opponentPosLabel;
    private Label opponentEnergyLabel;

    // === Dice / action log labels ===
    private Label diceResultLabel;
    private Label actionLine1;
    private Label actionLine2;
    private Label actionLine3;

    // === Panel assets ===
    private Image controlPanelImage;

    // === Cell images ===
    private Image normalImage;
    private Image ScarerdoorImage;
    private Image laugherdoorImage;
    private Image scarerOpenDoorImage;
    private Image laugherOpenDoorImage;
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
    private Image cardCellImage;

    // === Card face images ===
    private Image cardBackImage;
    private Image card_2319Alert;
    private Image cardContaminationCode;
    private Image cardMegaDrain;
    private Image cardMindScramble;
    private Image cardPositionSwap;
    private Image cardSmallSnatcher;
    private Image cardSneakyThief;
    private Image cardSuperShield;
    private Image cardTotalConfusion;

    private static final String IMG = "/game/gui/resources/images/";

    // === Grid cell views ===
    private ImageView[][] cellViews;

    // === Card overlay (lives inside boardContainer StackPane) ===
    private VBox    cardOverlay;
    private ImageView cardOverlayBack;
    private ImageView cardOverlayFace;
    private Label   cardOverlayName;
    private Label   cardOverlayDesc;

    // === Game state ===
    private Game game;

    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {

        // --- Panel assets ---
        controlPanelImage                   = loadImage(IMG + "ControlPanel.png");

        // --- Cell / monster images ---
        normalImage                         = loadImage(IMG + "NormalCell.png");
        ScarerdoorImage                     = loadImage(IMG + "Scarer_ClosedDoor_Cell2.png");
        laugherdoorImage                    = loadImage(IMG + "Laugher_ClosedDoor_Cell.png");
        scarerOpenDoorImage                 = loadImage(IMG + "Scarer_OpenDoor_Cell.png");
        laugherOpenDoorImage                = loadImage(IMG + "Laugher_OpenDoor_Cell.png");
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
        cardCellImage                       = loadImage(IMG + "CardCell.png");

        // --- Card images (exact filenames as requested) ---
        cardBackImage          = loadImage(IMG + "card_back_design.png");
        card_2319Alert         = loadImage(IMG + "2319_alert.png");
        cardContaminationCode  = loadImage(IMG + "contamination_code.png");
        cardMegaDrain          = loadImage(IMG + "mega_drain.png");
        cardMindScramble       = loadImage(IMG + "mind_scramble.png");
        cardPositionSwap       = loadImage(IMG + "position_swap.png");
        cardSmallSnatcher      = loadImage(IMG + "small_snatcher.png");
        cardSneakyThief        = loadImage(IMG + "sneaky_theif.png");  // exact filename kept
        cardSuperShield        = loadImage(IMG + "super_shield.png");
        cardTotalConfusion     = loadImage(IMG + "total_confusion.png");

        // --- Grid ImageViews ---
        cellViews = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                ImageView iv = new ImageView();
                iv.setPreserveRatio(false);
                iv.fitWidthProperty().bind(grid.widthProperty().divide(Constants.BOARD_COLS));
                iv.fitHeightProperty().bind(grid.heightProperty().divide(Constants.BOARD_ROWS));
                cellViews[row][col] = iv;
                grid.add(iv, col, row);
            }
        }

        // --- Player panel ---
        playerPanelContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label pTitle = makeLabel("YOUR MONSTER", "#ffcc00", 13, true);
        playerNameLabel   = makeLabel("-", "white", 12, false);
        playerPosLabel    = makeLabel("Pos: -", "white", 11, false);
        playerEnergyLabel = makeLabel("Energy: -", "white", 11, false);
        playerTurnLabel   = makeLabel("", "#00ff88", 12, true);
        playerPanelContainer.getChildren().addAll(
            pTitle, playerNameLabel, playerPosLabel, playerEnergyLabel, playerTurnLabel);

        // --- Opponent panel ---
        opponentPanelContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label oTitle = makeLabel("OPPONENT", "#ff6666", 13, true);
        opponentNameLabel   = makeLabel("-", "white", 12, false);
        opponentPosLabel    = makeLabel("Pos: -", "white", 11, false);
        opponentEnergyLabel = makeLabel("Energy: -", "white", 11, false);
        opponentPanelContainer.getChildren().addAll(
            oTitle, opponentNameLabel, opponentPosLabel, opponentEnergyLabel);

        // --- Dice panel ---
        diceContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label dTitle = makeLabel("DICE", "#ffcc00", 13, true);
        diceResultLabel = makeLabel("Press Roll!", "white", 14, true);
        diceContainer.getChildren().addAll(dTitle, diceResultLabel);

        // --- Action log ---
        actionLogContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label aTitle = makeLabel("ACTION LOG", "#ffcc00", 13, true);
        actionLine1 = makeLabel("", "white", 11, false);
        actionLine2 = makeLabel("", "white", 11, false);
        actionLine3 = makeLabel("", "white", 11, false);
        actionLine1.setWrapText(true);
        actionLine2.setWrapText(true);
        actionLine3.setWrapText(true);
        actionLogContainer.getChildren().addAll(aTitle, actionLine1, actionLine2, actionLine3);

        // --- Build card overlay (hidden by default) ---
        buildCardOverlay();

        System.out.println("DEBUG: GameController.initialize() complete");
    }

    // =========================================================
    //  CARD OVERLAY CONSTRUCTION
    //  The overlay sits in the StackPane (boardContainer) so it
    //  floats directly on top of the board.
    // =========================================================
    private void buildCardOverlay() {

        // Semi-transparent dark backdrop that covers the whole board
        cardOverlay = new VBox(12);
        cardOverlay.setAlignment(Pos.CENTER);
        cardOverlay.setStyle(
            "-fx-background-color: rgba(0,0,0,0.72);" +
            "-fx-padding: 30;" +
            "-fx-background-radius: 16;"
        );
        cardOverlay.setMaxWidth(260);
        cardOverlay.setMaxHeight(380);
        cardOverlay.setVisible(false);
        cardOverlay.setOpacity(0);

        // Card back image shown first
        cardOverlayBack = new ImageView();
        cardOverlayBack.setFitWidth(180);
        cardOverlayBack.setFitHeight(220);
        cardOverlayBack.setPreserveRatio(true);
        cardOverlayBack.setImage(cardBackImage);

        // Card face image revealed after flip delay
        cardOverlayFace = new ImageView();
        cardOverlayFace.setFitWidth(180);
        cardOverlayFace.setFitHeight(220);
        cardOverlayFace.setPreserveRatio(true);
        cardOverlayFace.setVisible(false);

        // Card name label
        cardOverlayName = new Label();
        cardOverlayName.setStyle(
            "-fx-text-fill: #ffcc00;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;"
        );
        cardOverlayName.setWrapText(true);
        cardOverlayName.setMaxWidth(220);
        cardOverlayName.setAlignment(Pos.CENTER);

        // Card description label
        cardOverlayDesc = new Label();
        cardOverlayDesc.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;"
        );
        cardOverlayDesc.setWrapText(true);
        cardOverlayDesc.setMaxWidth(220);
        cardOverlayDesc.setAlignment(Pos.CENTER);

        // "Tap to continue" hint
        Label tapHint = new Label("tap to continue");
        tapHint.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px; -fx-font-style: italic;");

        cardOverlay.getChildren().addAll(
            cardOverlayBack,
            cardOverlayFace,
            cardOverlayName,
            cardOverlayDesc,
            tapHint
        );

        // Clicking anywhere on the overlay dismisses it
        cardOverlay.setOnMouseClicked(e -> dismissCardOverlay());

        // Add overlay into the StackPane so it floats over the board
        boardContainer.getChildren().add(cardOverlay);
        StackPane.setAlignment(cardOverlay, Pos.CENTER);
    }

    // =========================================================
    //  SHOW CARD OVERLAY
    //  Sequence: fade-in back → pause → swap to face → pause →
    //            player taps to dismiss
    // =========================================================
    private void showCardOverlay(Card card) {
        // Set content
        cardOverlayName.setText(card.getName());
        cardOverlayDesc.setText(card.getDescription());
        cardOverlayFace.setImage(getCardImage(card.getName()));

        // Reset visibility
        cardOverlayBack.setVisible(true);
        cardOverlayFace.setVisible(false);
        cardOverlay.setVisible(true);

        // --- Animation sequence ---
        // 1. Fade the whole overlay in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cardOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // 2. Hold the card back for 800 ms so player sees it's a card
        PauseTransition showBack = new PauseTransition(Duration.millis(800));
        showBack.setOnFinished(e -> {
            // Swap back → face
            cardOverlayBack.setVisible(false);
            cardOverlayFace.setVisible(true);
        });

        SequentialTransition seq = new SequentialTransition(fadeIn, showBack);
        seq.play();
    }

    // Fade out and hide the overlay, then continue game flow
    private void dismissCardOverlay() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), cardOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            cardOverlay.setVisible(false);
            // Refresh board + UI after the player acknowledges the card
            refreshBoard();
            updateUI();
        });
        fadeOut.play();
    }

    // =========================================================
    //  MAP CARD NAME → IMAGE
    // =========================================================
    private Image getCardImage(String cardName) {
        switch (cardName) {
            case "2319 Alert":          return card_2319Alert;
            case "Contamination Code":  return cardContaminationCode;
            case "Mega Drain":          return cardMegaDrain;
            case "Mind Scramble":       return cardMindScramble;
            case "Position Swap":       return cardPositionSwap;
            case "Small Snatcher":      return cardSmallSnatcher;
            case "Sneaky Thief":        return cardSneakyThief;
            case "Super Shield":        return cardSuperShield;
            case "Total Confusion":     return cardTotalConfusion;
            default:                    return cardBackImage;
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private Label makeLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: " + size + "px; " +
            (bold ? "-fx-font-weight: bold;" : "")
        );
        l.setWrapText(true);
        l.setMaxWidth(180);
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

    // =========================================================
    //  GAME START
    // =========================================================
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

    // =========================================================
    //  BOARD RENDERING
    // =========================================================
    private void refreshBoard() {
        Cell[][] cells = game.getBoard().getBoardCells();

        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                Cell cell = cells[row][col];
                if (cell != null) setCellImage(row, col, cell);
                else cellViews[row][col].setImage(normalImage);
            }
        }

        drawMonsterOverlay(game.getPlayer());
        drawMonsterOverlay(game.getOpponent());
    }

    private void drawMonsterOverlay(Monster m) {
        if (m == null) return;
        int[] rc = indexToRowCol(m.getPosition());
        cellViews[rc[0]][rc[1]].setImage(getMonsterImage(m.getName()));
    }

    private int[] indexToRowCol(int index) {
        int cols = Constants.BOARD_COLS;
        int row  = index / cols;
        int col  = index % cols;
        if (row % 2 == 1) col = cols - 1 - col;
        row = Constants.BOARD_ROWS - 1 - row;
        return new int[]{row, col};
    }

    private Image getMonsterImage(String name) {
        switch (name) {
            case "Celia Mae":               return monsterImage_celia_mae;
            case "Fungus":                  return monsterImage_Fungus;
            case "Henry J. Waternoose III": return monsterImage_Henry_J_Waternoose_III;
            case "James Sullivan":          return monsterImage_James_sullivan;
            case "Mike Wazowski":           return monsterImage_Mike_Wazowski;
            case "Randall":                 return monsterImage_Randall;
            case "Roz":                     return monsterImage_Roz;
            case "Yeti":                    return monsterImage_Yeti;
            default:                        return normalImage;
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
            DoorCell door = (DoorCell) cell;
            Role role = door.getRole();
            if (door.isActivated()) {
                cellViews[row][col].setImage(role == Role.SCARER ? scarerOpenDoorImage : laugherOpenDoorImage);
            } else {
                cellViews[row][col].setImage(role == Role.SCARER ? ScarerdoorImage : laugherdoorImage);
            }
        } else if (cell instanceof ConveyorBelt) {
            cellViews[row][col].setImage(conveyorImage);
        } else if (cell instanceof ContaminationSock) {
            cellViews[row][col].setImage(contaminationImage);
        } else if (cell instanceof CardCell) {
            cellViews[row][col].setImage(cardCellImage);   // ← now uses CardCell.png
        } else {
            cellViews[row][col].setImage(normalImage);
        }
    }

    // =========================================================
    //  UI UPDATE
    // =========================================================
    private void updateUI() {
        if (game == null) return;

        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        Monster current  = game.getCurrent();

        playerNameLabel.setText(player.getName());
        playerPosLabel.setText("Pos: " + player.getPosition());
        playerEnergyLabel.setText("Energy: " + player.getEnergy());
        playerTurnLabel.setText(current == player ? "▶ YOUR TURN" : "");

        opponentNameLabel.setText(opponent.getName());
        opponentPosLabel.setText("Pos: " + opponent.getPosition());
        opponentEnergyLabel.setText("Energy: " + opponent.getEnergy());

        myLabel.setText(current == player ? "Your turn — press Roll!" : "Opponent's turn — press Roll!");
        myLabel.setStyle("-fx-font-size: 16px; -fx-padding: 8 0 8 0; -fx-text-fill: "
            + (current == player ? "#00ff88" : "#ff6666") + ";");
    }

    // =========================================================
    //  ROLL DICE HANDLER
    // =========================================================
    @FXML
    private void handleRollDice() {
        if (game == null) return;

        // Block rolling while the card overlay is showing
        if (cardOverlay.isVisible()) return;

        try {
            Monster current  = game.getCurrent();
            Monster opponent = current == game.getPlayer() ? game.getOpponent() : game.getPlayer();

            int oldPos       = current.getPosition();
            int oldEnergy    = current.getEnergy();
            int oldOppEnergy = opponent.getEnergy();

            // Snapshot top card BEFORE the turn so we can detect if one was drawn
            Card topCard = Board.cards.isEmpty() ? null : Board.cards.get(0);
            if (Board.cards.isEmpty()) Board.reloadCards();

            game.playTurn();

            int newPos       = current.getPosition();
            int newEnergy    = current.getEnergy();
            int newOppEnergy = opponent.getEnergy();

            // Detect whether a card was drawn this turn
            boolean cardWasDrawn = topCard != null &&
                (Board.cards.isEmpty() || Board.cards.get(0) != topCard);

            int moved = newPos - oldPos;
            if (moved < 0) moved += 100;

            diceResultLabel.setText("🎲 " + moved);

            // --- Action log lines ---
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

            // --- Show card overlay if a card was drawn ---
            if (cardWasDrawn && topCard != null) {
                // Refresh board first so the board state is correct behind the overlay
                refreshBoard();
                updateUI();
                showCardOverlay(topCard);
                // dismissCardOverlay() calls refreshBoard()+updateUI() again on tap,
                // so no duplicate call needed here.
            } else {
                refreshBoard();
                updateUI();
            }

            // --- Check for winner ---
            if (game.getWinner() != null) {
                myLabel.setText(game.getWinner().getName() + " WINS! 🎉");
                myLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffcc00;");
                SceneManager.getInstance().switchToGameOverScreen();
            }

        } catch (game.engine.exceptions.InvalidMoveException e) {
            actionLine1.setText("⚠ " + e.getMessage());
            actionLine2.setText("Roll again!");
            actionLine3.setText("");
            refreshBoard();
            updateUI();
        } catch (Exception e) {
            actionLine1.setText("Error: " + e.getMessage());
            System.err.println("ERROR in handleRollDice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    //  POWER-UP HANDLER
    // =========================================================
    @FXML
    private void handlePowerUp() {
        if (game == null) return;
        if (cardOverlay.isVisible()) return;   // block while card is showing

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

    // =========================================================
    //  DIALOGS
    // =========================================================
    private boolean showConfirmDialog(String title, String header, String content) {
        final boolean[] result = {false};
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Label headerLabel  = new Label(header);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        Button okButton     = new Button("OK");
        Button cancelButton = new Button("Cancel");
        okButton.setOnAction(e -> { result[0] = true; dialog.close(); });
        cancelButton.setOnAction(e -> dialog.close());
        HBox buttons = new HBox(10, okButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);
        VBox layout = new VBox(12, headerLabel, contentLabel, buttons);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.setAlignment(Pos.CENTER);
        dialog.setScene(new Scene(layout, 320, 160));
        dialog.showAndWait();
        return result[0];
    }

    private void showErrorAlert(String title, String message) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Label headerLabel  = new Label("Invalid Action");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: red;");
        Label contentLabel = new Label(message != null ? message : "Unknown error");
        contentLabel.setWrapText(true);
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialog.close());
        HBox buttons = new HBox(okButton);
        buttons.setAlignment(Pos.CENTER);
        VBox layout = new VBox(12, headerLabel, contentLabel, buttons);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.setAlignment(Pos.CENTER);
        dialog.setScene(new Scene(layout, 320, 140));
        dialog.showAndWait();
    }
}