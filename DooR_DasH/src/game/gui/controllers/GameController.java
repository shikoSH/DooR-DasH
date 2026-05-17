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
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.monsters.Monster;
import game.engine.Board;
import game.engine.cards.Card;
import game.engine.cells.*;
import javafx.beans.binding.NumberBinding;


public class GameController {

    // === FXML injected ===
    @FXML private StackPane boardContainer;
    @FXML private GridPane grid;
    @FXML private VBox playerPanelContainer;
    @FXML private VBox opponentPanelContainer;
    @FXML private VBox actionLogContainer;
    @FXML private VBox diceContainer;
    @FXML private Label myLabel;
    @FXML private StackPane backgroundRoot;
    @FXML private ImageView backgroundView;
    @FXML private ImageView controlPanelView;
    @FXML private ImageView boardImageView;

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

    // === Dice and Card Deck — created programmatically, NOT @FXML ===
    private ImageView diceImage      = new ImageView();
    private ImageView activeCardDeck = new ImageView();

    private Image[] diceImages = new Image[6];
    private javafx.animation.Timeline diceTimeline;

    // === Card deck images ===
    private Image deckFull;
    private Image deckMid;
    private Image deckLeast;

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

    // === Animation Lock ===
    private boolean isAnimating = false;

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

    // === Grid cell views (Layered) ===
    private ImageView[][] backgroundViews;
    private ImageView[][] monsterViews;

    // === Card overlay (lives inside boardContainer StackPane) ===
    private VBox      cardOverlay;
    private ImageView cardOverlayBack;
    private ImageView cardOverlayFace;
    private Label     cardOverlayName;
    private Label     cardOverlayDesc;

    // === Game state ===
    private Game game;

    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {

        // --- Background and panel fill screen ---
        backgroundView.fitWidthProperty().bind(backgroundRoot.widthProperty());
        backgroundView.fitHeightProperty().bind(backgroundRoot.heightProperty());

        controlPanelView.fitWidthProperty().bind(backgroundRoot.widthProperty());
        controlPanelView.fitHeightProperty().bind(backgroundRoot.heightProperty());

        // --- Board sizing ---
        NumberBinding boardSize = backgroundRoot.heightProperty().multiply(0.71);
        boardImageView.fitWidthProperty().bind(boardSize);
        boardImageView.fitHeightProperty().bind(boardSize);
        grid.maxWidthProperty().bind(boardSize);
        grid.maxHeightProperty().bind(boardSize);
        grid.minWidthProperty().bind(boardSize);
        grid.minHeightProperty().bind(boardSize);
        boardContainer.setTranslateY(-60);

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

        // --- Card face images ---
        cardBackImage         = loadImage(IMG + "card_back_design.png");
        card_2319Alert        = loadImage(IMG + "2319_alert.png");
        cardContaminationCode = loadImage(IMG + "contamination_code.png");
        cardMegaDrain         = loadImage(IMG + "mega_drain.png");
        cardMindScramble      = loadImage(IMG + "mind_scramble.png");
        cardPositionSwap      = loadImage(IMG + "position_swap.png");
        cardSmallSnatcher     = loadImage(IMG + "small_snatcher.png");
        cardSneakyThief       = loadImage(IMG + "sneaky_theif.png");
        cardSuperShield       = loadImage(IMG + "super_shield.png");
        cardTotalConfusion    = loadImage(IMG + "total_confusion.png");

        // --- Card deck images ---
        deckFull  = loadImage(IMG + "CardsDeck_Full.png");
        deckMid   = loadImage(IMG + "CardsDeck_Mid.png");
        deckLeast = loadImage(IMG + "CardsDeck_Least.png");

        // --- Dice face images ---
        for (int i = 1; i <= 6; i++) {
            diceImages[i - 1] = loadImage(IMG + "Dice_on_" + i + ".png");
        }

        // =========================================================
        //  CARD DECK — pinned to the CARDS slot on the panel
        //  Panel image is 1359x762. Cards slot center ≈ x=310, y=672
        //  As percentages: x=310/1359≈0.228, y=672/762≈0.882
        // =========================================================
        activeCardDeck.setPreserveRatio(true);
        activeCardDeck.setImage(deckFull);
        activeCardDeck.fitWidthProperty().bind(
            backgroundRoot.widthProperty().multiply(0.5));
        activeCardDeck.fitHeightProperty().bind(
            backgroundRoot.heightProperty().multiply(0.15));
        activeCardDeck.translateXProperty().bind(
            backgroundRoot.widthProperty().multiply(0.330)
                .subtract(backgroundRoot.widthProperty().divide(2)));
        activeCardDeck.translateYProperty().bind(
            backgroundRoot.heightProperty().multiply(0.882)
                .subtract(backgroundRoot.heightProperty().divide(2)));
        // =========================================================
        //  DICE — pinned to the center slot on the panel
        //  Panel image is 1359x762. Dice slot center ≈ x=560, y=668
        //  As percentages: x=560/1359≈0.412, y=668/762≈0.877
        // =========================================================
        diceImage.setPreserveRatio(true);
        diceImage.setImage(diceImages[0]);
        diceImage.fitWidthProperty().bind(
            backgroundRoot.widthProperty().multiply(0.115));
        diceImage.fitHeightProperty().bind(
            backgroundRoot.heightProperty().multiply(0.14));
        diceImage.translateXProperty().bind(
            backgroundRoot.widthProperty().multiply(0.500)
                .subtract(backgroundRoot.widthProperty().divide(2)));
        diceImage.translateYProperty().bind(
            backgroundRoot.heightProperty().multiply(0.877)
                .subtract(backgroundRoot.heightProperty().divide(2)));

        // Insert both AFTER controlPanelView (index 1) but BEFORE the BorderPane
        // index 0 = backgroundView, index 1 = controlPanelView, 2 = activeCardDeck, 3 = diceImage, 4 = BorderPane
        backgroundRoot.getChildren().add(2, activeCardDeck);
        backgroundRoot.getChildren().add(3, diceImage);

        // --- Grid ImageViews (Layered) ---
        backgroundViews = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        monsterViews    = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                StackPane cellStack = new StackPane();

                ImageView bgView = new ImageView();
                bgView.setPreserveRatio(false);
                bgView.fitWidthProperty().bind(grid.widthProperty().divide(Constants.BOARD_COLS));
                bgView.fitHeightProperty().bind(grid.heightProperty().divide(Constants.BOARD_ROWS));

                ImageView mView = new ImageView();
                mView.setPreserveRatio(true);
                mView.fitWidthProperty().bind(grid.widthProperty().divide(Constants.BOARD_COLS).multiply(0.8));
                mView.fitHeightProperty().bind(grid.heightProperty().divide(Constants.BOARD_ROWS).multiply(0.8));

                backgroundViews[row][col] = bgView;
                monsterViews[row][col]    = mView;

                int backendRow = Constants.BOARD_ROWS - 1 - row;
                int backendCol = (backendRow % 2 == 1) ? Constants.BOARD_COLS - 1 - col : col;
                int boardIndex = backendRow * Constants.BOARD_COLS + backendCol;

                Label indexLabel = new Label(String.valueOf(boardIndex));
                indexLabel.setStyle(
                    "-fx-font-size: 9px;" +
                    "-fx-text-fill: rgba(255,255,255,0.75);" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 1 2 0 0;"
                );
                StackPane.setAlignment(indexLabel, Pos.TOP_RIGHT);

                cellStack.getChildren().addAll(bgView, mView, indexLabel);
                grid.add(cellStack, col, row);
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

        // --- Dice panel (sidebar label only — actual image is pinned to panel) ---
        diceContainer.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-padding: 8; -fx-background-radius: 8;");
        Label dTitle = makeLabel("DICE", "#ffcc00", 13, true);
        diceResultLabel = makeLabel("—", "white", 20, true);
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

        // --- Build card overlay ---
        buildCardOverlay();

        System.out.println("DEBUG: GameController.initialize() complete");
    }

    // =========================================================
    //  CARD OVERLAY CONSTRUCTION
    // =========================================================
    private void buildCardOverlay() {
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

        cardOverlayBack = new ImageView();
        cardOverlayBack.setFitWidth(180);
        cardOverlayBack.setFitHeight(220);
        cardOverlayBack.setPreserveRatio(true);
        cardOverlayBack.setImage(cardBackImage);

        cardOverlayFace = new ImageView();
        cardOverlayFace.setFitWidth(180);
        cardOverlayFace.setFitHeight(220);
        cardOverlayFace.setPreserveRatio(true);
        cardOverlayFace.setVisible(false);

        cardOverlayName = new Label();
        cardOverlayName.setStyle(
            "-fx-text-fill: #ffcc00;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;"
        );
        cardOverlayName.setWrapText(true);
        cardOverlayName.setMaxWidth(220);
        cardOverlayName.setAlignment(Pos.CENTER);

        cardOverlayDesc = new Label();
        cardOverlayDesc.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;"
        );
        cardOverlayDesc.setWrapText(true);
        cardOverlayDesc.setMaxWidth(220);
        cardOverlayDesc.setAlignment(Pos.CENTER);

        Label tapHint = new Label("tap to continue");
        tapHint.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px; -fx-font-style: italic;");

        cardOverlay.getChildren().addAll(
            cardOverlayBack, cardOverlayFace, cardOverlayName, cardOverlayDesc, tapHint);

        cardOverlay.setOnMouseClicked(e -> dismissCardOverlay());
        boardContainer.getChildren().add(cardOverlay);
        StackPane.setAlignment(cardOverlay, Pos.CENTER);
    }

    // =========================================================
    //  SHOW / DISMISS CARD OVERLAY
    // =========================================================
    private void showCardOverlay(Card card) {
        cardOverlayName.setText(card.getName());
        cardOverlayDesc.setText(card.getDescription());
        cardOverlayFace.setImage(getCardImage(card.getName()));
        cardOverlayBack.setVisible(true);
        cardOverlayFace.setVisible(false);
        cardOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cardOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition showBack = new PauseTransition(Duration.millis(800));
        showBack.setOnFinished(e -> {
            cardOverlayBack.setVisible(false);
            cardOverlayFace.setVisible(true);
        });

        new SequentialTransition(fadeIn, showBack).play();
    }

    private void dismissCardOverlay() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), cardOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            cardOverlay.setVisible(false);
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
            case "2319 Alert":         return card_2319Alert;
            case "Contamination Code": return cardContaminationCode;
            case "Mega Drain":         return cardMegaDrain;
            case "Mind Scramble":      return cardMindScramble;
            case "Position Swap":      return cardPositionSwap;
            case "Small Snatcher":     return cardSmallSnatcher;
            case "Sneaky Thief":       return cardSneakyThief;
            case "Super Shield":       return cardSuperShield;
            case "Total Confusion":    return cardTotalConfusion;
            default:                   return cardBackImage;
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

        for (int row = 0; row < Constants.BOARD_ROWS; row++)
            for (int col = 0; col < Constants.BOARD_COLS; col++)
                monsterViews[row][col].setImage(null);

        for (int index = 0; index < 100; index++) {
            int backendRow = index / Constants.BOARD_COLS;
            int backendCol = index % Constants.BOARD_COLS;
            if (backendRow % 2 == 1) backendCol = Constants.BOARD_COLS - 1 - backendCol;
            Cell cell = cells[backendRow][backendCol];

            int[] guiRC = indexToRowCol(index);
            if (cell != null) setCellImage(guiRC[0], guiRC[1], cell);
            else backgroundViews[guiRC[0]][guiRC[1]].setImage(normalImage);
        }

        drawMonsterOverlay(game.getPlayer());
        drawMonsterOverlay(game.getOpponent());
        updateDeckImage();
    }

    private void drawMonsterOverlay(Monster m) {
        if (m == null) return;
        int[] rc = indexToRowCol(m.getPosition());
        monsterViews[rc[0]][rc[1]].setImage(getMonsterImage(m.getName()));
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
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":             return monsterImage_celia_mae;
            case "fungus":                return monsterImage_Fungus;
            case "henry j. waternoose":
            case "henry j. waternoose iii": return monsterImage_Henry_J_Waternoose_III;
            case "james p. sullivan":
            case "james sullivan":        return monsterImage_James_sullivan;
            case "mike wazowski":         return monsterImage_Mike_Wazowski;
            case "randall boggs":
            case "randall":               return monsterImage_Randall;
            case "roz":                   return monsterImage_Roz;
            case "yeti":                  return monsterImage_Yeti;
            default:
                System.err.println("WARNING: No image for monster: '" + name + "'");
                return null;
        }
    }

    private void setCellImage(int row, int col, Cell cell) {
        if (cell instanceof MonsterCell) {
            backgroundViews[row][col].setImage(normalImage);
        } else if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            Role role = door.getRole();
            if (door.isActivated())
                backgroundViews[row][col].setImage(role == Role.SCARER ? scarerOpenDoorImage : laugherOpenDoorImage);
            else
                backgroundViews[row][col].setImage(role == Role.SCARER ? ScarerdoorImage : laugherdoorImage);
        } else if (cell instanceof ConveyorBelt) {
            backgroundViews[row][col].setImage(conveyorImage);
        } else if (cell instanceof ContaminationSock) {
            backgroundViews[row][col].setImage(contaminationImage);
        } else if (cell instanceof CardCell) {
            backgroundViews[row][col].setImage(cardCellImage);
        } else {
            backgroundViews[row][col].setImage(normalImage);
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
    //  DECK IMAGE UPDATE
    // =========================================================
    private void updateDeckImage() {
        int remaining = Board.cards.size();
        int total     = Board.getOriginalCards().size();
        if (total == 0) return;

        double ratio = (double) remaining / total;
        if (ratio > 0.60)
            activeCardDeck.setImage(deckFull);
        else if (ratio > 0.25)
            activeCardDeck.setImage(deckMid);
        else
            activeCardDeck.setImage(deckLeast);
    }

    // =========================================================
    //  ROLL DICE HANDLER
    // =========================================================
    @FXML
    private void handleRollDice() {
        if (game == null) return;
        if (cardOverlay.isVisible() || isAnimating) return;

        try {
            isAnimating = true;

            Monster current  = game.getCurrent();
            Monster opponent = current == game.getPlayer() ? game.getOpponent() : game.getPlayer();

            int oldPos       = current.getPosition();
            int oldEnergy    = current.getEnergy();
            int oldOppEnergy = opponent.getEnergy();

            Card topCard = Board.cards.isEmpty() ? null : Board.cards.get(0);
            if (Board.cards.isEmpty()) Board.reloadCards();

            game.playTurn();

            int newPos       = current.getPosition();
            int newEnergy    = current.getEnergy();
            int newOppEnergy = opponent.getEnergy();

            boolean cardWasDrawn = topCard != null &&
                (Board.cards.isEmpty() || Board.cards.get(0) != topCard);

            int moved   = newPos - oldPos;
            if (moved < 0) moved += 100;
            int diceFace = Math.max(1, Math.min(6, moved));

            String line1 = current.getName() + " moved " + moved + " spaces.";
            String line2 = "";
            String line3 = "";

            if (newPos == 0 && oldPos != 0 && moved != 0)
                line2 = "SENT BACK TO START!";
            else if (cardWasDrawn && topCard != null)
                line2 = "Drew: " + topCard.getName();

            if (newEnergy != oldEnergy)
                line3 = current.getName() + " energy: " + oldEnergy + "→" + newEnergy;
            else if (newOppEnergy != oldOppEnergy)
                line3 = opponent.getName() + " energy: " + oldOppEnergy + "→" + newOppEnergy;

            actionLine1.setText(line1);
            actionLine2.setText(line2);
            actionLine3.setText(line3);

            animateDiceRoll(diceFace, () -> {
                Runnable onAnimationFinished = () -> {
                    if (cardWasDrawn && topCard != null) {
                        refreshBoard();
                        updateUI();
                        showCardOverlay(topCard);
                    } else {
                        refreshBoard();
                        updateUI();
                    }

                    if (game.getWinner() != null) {
                        myLabel.setText(game.getWinner().getName() + " WINS!");
                        myLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffcc00;");
                        SceneManager.getInstance().switchToGameOverScreen();
                    }

                    isAnimating = false;
                };

                animateMonsterMove(current, opponent, oldPos, newPos, onAnimationFinished);
            });

        } catch (game.engine.exceptions.InvalidMoveException e) {
            actionLine1.setText("⚠ " + e.getMessage());
            actionLine2.setText("Roll again!");
            actionLine3.setText("");
            refreshBoard();
            updateUI();
            isAnimating = false;
        } catch (Exception e) {
            actionLine1.setText("Error: " + e.getMessage());
            System.err.println("ERROR in handleRollDice: " + e.getMessage());
            e.printStackTrace();
            isAnimating = false;
        }
    }

    // =========================================================
    //  DICE ANIMATION
    // =========================================================
    private void animateDiceRoll(int finalFace, Runnable onFinished) {
        if (diceTimeline != null) diceTimeline.stop();

        diceTimeline = new javafx.animation.Timeline();
        java.util.Random rand = new java.util.Random();

        int[] delays = {60, 80, 100, 130, 160, 200, 250, 320, 400};
        int elapsed  = 0;

        for (int delay : delays) {
            elapsed += delay;
            final int face = rand.nextInt(6);
            diceTimeline.getKeyFrames().add(
                new javafx.animation.KeyFrame(Duration.millis(elapsed),
                    e -> diceImage.setImage(diceImages[face]))
            );
        }

        elapsed += 300;
        final int totalMs = elapsed;
        diceTimeline.getKeyFrames().add(
            new javafx.animation.KeyFrame(Duration.millis(totalMs),
                e -> {
                    diceImage.setImage(diceImages[finalFace - 1]);
                    diceResultLabel.setText("Rolled: " + finalFace);
                    if (onFinished != null) onFinished.run();
                })
        );

        diceTimeline.play();
    }

    // =========================================================
    //  MONSTER MOVEMENT ANIMATION
    // =========================================================
    private void animateMonsterMove(Monster currentMonster, Monster opponentMonster,
                                    int oldPos, int newPos, Runnable onFinished) {
        for (int r = 0; r < Constants.BOARD_ROWS; r++) {
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setImage(null);
                monsterViews[r][c].setTranslateX(0);
                monsterViews[r][c].setTranslateY(0);
            }
        }

        drawMonsterOverlay(opponentMonster);

        int[] startRC = indexToRowCol(oldPos);
        monsterViews[startRC[0]][startRC[1]].setImage(getMonsterImage(currentMonster.getName()));

        SequentialTransition seq = new SequentialTransition();
        int distance = Math.abs(newPos - oldPos);

        if (distance > 12 || oldPos == newPos) {
            seq.getChildren().add(createHop(currentMonster, opponentMonster, oldPos, newPos, 600));
        } else {
            int step = (newPos > oldPos) ? 1 : -1;
            for (int pos = oldPos; pos != newPos; pos += step)
                seq.getChildren().add(createHop(currentMonster, opponentMonster, pos, pos + step, 250));
        }

        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }

    private javafx.animation.Animation createHop(Monster moving, Monster stationary,
                                                   int fromPos, int toPos, int durationMillis) {
        int[] fromRC = indexToRowCol(fromPos);
        int[] toRC   = indexToRowCol(toPos);

        double cellWidth  = grid.getWidth()  / Constants.BOARD_COLS;
        double cellHeight = grid.getHeight() / Constants.BOARD_ROWS;
        double dx = (toRC[1] - fromRC[1]) * cellWidth;
        double dy = (toRC[0] - fromRC[0]) * cellHeight;

        ImageView movingView = monsterViews[fromRC[0]][fromRC[1]];

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMillis), movingView);
        tt.setByX(dx);
        tt.setByY(dy);
        tt.setOnFinished(e -> {
            movingView.setTranslateX(0);
            movingView.setTranslateY(0);
            movingView.setImage(null);
            if (stationary.getPosition() == fromPos)
                movingView.setImage(getMonsterImage(stationary.getName()));
            monsterViews[toRC[0]][toRC[1]].setImage(getMonsterImage(moving.getName()));
        });

        PauseTransition bringToFront = new PauseTransition(Duration.millis(1));
        bringToFront.setOnFinished(e -> movingView.getParent().toFront());

        return new SequentialTransition(bringToFront, tt);
    }

    // =========================================================
    //  POWER-UP HANDLER
    // =========================================================
    @FXML
    private void handlePowerUp() {
        if (game == null) return;
        if (cardOverlay.isVisible()) return;

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