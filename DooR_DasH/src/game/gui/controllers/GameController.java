package game.gui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import game.engine.*;
import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.monsters.Monster;

public class GameController {

    @FXML private StackPane  backgroundRoot;
    @FXML private ImageView  backgroundView;
    @FXML private ImageView  controlPanelView;
    @FXML private StackPane  boardContainer;
    @FXML private GridPane   grid;
    @FXML private ImageView  boardImageView;
    @FXML private ImageView  boardHolderView;
    @FXML private Label      myLabel;
    @FXML private VBox       playerPanelContainer;
    @FXML private VBox       opponentPanelContainer;
    @FXML private VBox       actionLogContainer;
    @FXML private VBox       diceContainer;
    @FXML private ImageView  cardDeckView;
    @FXML private ImageView  diceView;
    @FXML private Label      diceResultLabel;
    @FXML private ImageView  powerUpImageBtn;
    @FXML private ImageView  rollImageBtn;
    @FXML private AnchorPane controlBar;
    @FXML private BorderPane masterLayout;

    // Change font: "\"Courier New\", monospace" = LED, "\"Arial\", sans-serif" = modern
    private static final String LED = "\"Courier New\", monospace";

    // Change this to scale ALL text: 1.0 = normal, 1.4 = 40% bigger
    private static final double TEXT_SCALE = 1.4;

    private static final String IMG = "/game/gui/resources/images/";

    // Image cache — avoids reloading the same file multiple times
    private static final java.util.HashMap<String, Image> IMAGE_CACHE = new java.util.HashMap<>();

    // ── Grid views ────────────────────────────────────────────
    private ImageView[][] backgroundViews;
    private ImageView[][] monsterViews;
    private Label[][]     energyLabels;

    // ── Images ────────────────────────────────────────────────
    private Image normalImage, ScarerdoorImage, laugherdoorImage;
    private Image scarerOpenDoorImage, laugherOpenDoorImage;
    private Image monsterCellGreyImage, conveyorImage, contaminationImage, cardCellImage;
    private Image monsterImage_celia_mae, monsterImage_Fungus;
    private Image monsterImage_Henry_J_Waternoose_III, monsterImage_James_sullivan;
    private Image monsterImage_Mike_Wazowski, monsterImage_Randall, monsterImage_Roz, monsterImage_Yeti;
    private Image screenImage_celia_mae, screenImage_Fungus, screenImage_Henry;
    private Image screenImage_Mike, screenImage_Randall, screenImage_Roz, screenImage_Yeti;
    private Image screenImage_James;   // James P. Sullivan screen portrait
    private Image energy0, energy25, energy50, energy75, energy100;
    private Image deckFull, deckMid, deckLeast;
    private Image[] diceImages = new Image[6];
    private Timeline diceTimeline;
    private Image cardBackImage, card_2319Alert, cardContaminationCode;
    private Image cardMegaDrain, cardMindScramble, cardPositionSwap;
    private Image cardSmallSnatcher, cardSneakyThief, cardSuperShield, cardTotalConfusion;

    // ── Panel labels ──────────────────────────────────────────
    private ImageView playerPortrait    = new ImageView();
    private ImageView playerEnergyBar   = new ImageView();
    private ImageView opponentPortrait  = new ImageView();
    private ImageView opponentEnergyBar = new ImageView();
    private Label playerNameLabel, playerTypeLabel, playerOrigRoleLabel;
    private Label playerCurrRoleLabel, playerPosLabel, playerEnergyLabel;
    private Label playerStatusLabel, playerTurnLabel;
    private Label opponentNameLabel, opponentTypeLabel, opponentOrigRoleLabel;
    private Label opponentCurrRoleLabel, opponentPosLabel, opponentEnergyLabel;
    private Label opponentStatusLabel;
    private Label actionLine1, actionLine2, actionLine3;

    // ── Card overlay ──────────────────────────────────────────
    private VBox      cardOverlay;
    private ImageView cardOverlayBack, cardOverlayFace;
    private Label     cardOverlayName, cardOverlayDesc, cardOverlayEffect;

    // ── State ─────────────────────────────────────────────────
    private Game    game;
    private boolean isAnimating = false;

    // ── Parallax ──────────────────────────────────────────────
    private double targetX = 0, targetY = 0;
    private AnimationTimer parallaxTimer;

    // =========================================================
    //  LAYOUT MULTIPLIERS — EDIT THESE TO MOVE/RESIZE THINGS
    // =========================================================

    // BOARD: how much of the screen height the board takes up
    // Increase = bigger board, Decrease = smaller board
    private static final double BOARD_SIZE_MULT     = 0.66;

    // BOTTOM BAR: how much of screen height the control bar takes
    // Increase = taller bar, more room for buttons
    private static final double CONTROL_BAR_MULT    = 0.16;

    // SIDE PANELS: how much of screen width each side panel takes
    // Increase = wider panels
    private static final double SIDE_PANEL_WIDTH    = 0.16;

    // PORTRAIT: how wide the monster portrait is as fraction of panel width
    private static final double PORTRAIT_WIDTH_MULT = 0.85;

    // ENERGY BAR: height as fraction of screen height
    private static final double ENERGY_BAR_H_MULT   = 0.13;

    // TOP PADDING of panels: push content down below panel image text
    // Increase to push labels further down
    private static final double PANEL_TOP_PAD_MULT  = 0.12;

    // ── CONTROL BAR items ────────────────────────────────────

    // CARD DECK position and size
    // DECK_LEFT_MULT: from left (0.05=near left, 0.30=center)
    // DECK_TOP_MULT:  from top of bar (0.05=top, 0.30=lower)
    // DECK_W_MULT:    width fraction of screen width
    // DECK_H_MULT:    height fraction of screen height
    private static final double DECK_LEFT_MULT     = 0.245; // from left edge
    private static final double DECK_TOP_MULT      = -0.8;  // from top of bar
    private static final double DECK_W_MULT        = 0.162; // width
    private static final double DECK_H_MULT        = 0.324;  // height

    // DICE size (square — width == height)
    private static final double DICE_SIZE_MULT     = 0.27;  // of screen height
    private static final double DICE_TOP_MULT      = -0.8;  // from top of bar

    // BUTTONS size and position
    private static final double BTN_W_MULT         = 0.15;  // width of each button
    private static final double BTN_H_MULT         = 0.15;  // height of each button
    private static final double BTN_TOP_MULT       = -0.55;  // from top of bar
    private static final double ROLL_RIGHT_MULT    = 0.175;  // distance from right edge
    private static final double POWERUP_RIGHT_MULT = 0.28; // distance from right edge

    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {
        try {
            backgroundView.setImage(loadImage(IMG + "background_Game.png"));
            controlPanelView.setImage(loadImage(IMG + "ControlPanel.png"));

            setupParallax();
            loadAllImages();
            buildGrid();
            buildPlayerPanel();
            buildOpponentPanel();
            buildActionLog();
            buildDicePanel();
            buildCardOverlay();
            setupCheatCodes();
            setupResponsiveLayout();

            // Fire layout once scene is fully attached
            backgroundRoot.sceneProperty().addListener((obs, old, scene) -> {
                if (scene != null) Platform.runLater(this::applyAllLayout);
            });

            System.out.println("DEBUG: GameController.initialize() done");
        } catch (Exception e) {
            System.err.println("Init error: " + e.getMessage());
        }
    }

    // =========================================================
    //  RESPONSIVE LAYOUT — all sizing/positioning lives here
    // =========================================================
    private void setupResponsiveLayout() {
        // Background oversized for parallax
        backgroundView.fitWidthProperty().bind(
            backgroundRoot.widthProperty().multiply(1.06));
        backgroundView.fitHeightProperty().bind(
            backgroundRoot.heightProperty().multiply(1.06));

        // Control panel fills screen
        controlPanelView.fitWidthProperty().bind(backgroundRoot.widthProperty());
        controlPanelView.fitHeightProperty().bind(backgroundRoot.heightProperty());

        // Board holder slightly bigger than board
        javafx.beans.binding.NumberBinding boardSize =
            backgroundRoot.heightProperty().multiply(BOARD_SIZE_MULT);

        boardHolderView.fitWidthProperty().bind(boardSize.multiply(1.12));
        boardHolderView.fitHeightProperty().bind(boardSize.multiply(1.12));
        boardHolderView.setImage(loadImage(IMG + "Board_Holder.png"));

        // Board grid
        boardImageView.fitWidthProperty().bind(boardSize);
        boardImageView.fitHeightProperty().bind(boardSize);
        grid.maxWidthProperty().bind(boardSize);
        grid.maxHeightProperty().bind(boardSize);
        grid.minWidthProperty().bind(boardSize);
        grid.minHeightProperty().bind(boardSize);

        // Re-apply full layout on any resize
        javafx.beans.value.ChangeListener<Number> onResize =
            (obs, old, val) -> applyAllLayout();
        backgroundRoot.widthProperty().addListener(onResize);
        backgroundRoot.heightProperty().addListener(onResize);
    }

    /**
     * Single method that repositions and resizes every element in the control
     * bar (and side panels) using the multiplier constants above.
     * Called on every resize event and once after the scene attaches.
     */
    private void applyAllLayout() {
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W == 0 || H == 0) return;

        // ── BOARD CONTAINER margin ────────────────────────────
        double barH = H * CONTROL_BAR_MULT;
        BorderPane.setMargin(boardContainer, new Insets(6, 6, barH + 6, 6));
        controlBar.setPrefHeight(barH);

        // ── SIDE PANELS ───────────────────────────────────────
        double panelW = W * SIDE_PANEL_WIDTH;
        playerPanelContainer.setPrefWidth(panelW);
        if (masterLayout.getRight() != null)
            ((VBox) masterLayout.getRight()).setPrefWidth(panelW);

        // Portrait and energy bar widths
        double portraitW = panelW * PORTRAIT_WIDTH_MULT;
        playerPortrait.setFitWidth(portraitW);
        opponentPortrait.setFitWidth(portraitW);
        playerEnergyBar.setFitWidth(portraitW);
        opponentEnergyBar.setFitWidth(portraitW);

        // Energy bar heights
        double energyH = H * ENERGY_BAR_H_MULT * 1.8;
        playerEnergyBar.setFitHeight(energyH);
        opponentEnergyBar.setFitHeight(energyH);

        // Panel top padding
        double pad = H * PANEL_TOP_PAD_MULT;
        playerPanelContainer.setStyle("-fx-padding: " + pad + " 6 6 6;");
        opponentPanelContainer.setStyle("-fx-padding: " + pad + " 6 6 6;");

        // ── CARD DECK ──────────────────────────────────────────
        // DECK_LEFT_MULT  = how far from left (0.0 = left edge, 0.5 = center)
        // DECK_TOP_MULT   = how far from top of control bar
        // DECK_W_MULT     = width as fraction of screen width
        // DECK_H_MULT     = height as fraction of screen height
        cardDeckView.setFitWidth(W * DECK_W_MULT);
        cardDeckView.setFitHeight(H * DECK_H_MULT);
        AnchorPane.setLeftAnchor(cardDeckView,   W * DECK_LEFT_MULT);
        AnchorPane.setTopAnchor(cardDeckView,    barH * DECK_TOP_MULT);
        AnchorPane.setRightAnchor(cardDeckView,  null);
        AnchorPane.setBottomAnchor(cardDeckView, null);

        // ── DICE ───────────────────────────────────────────────
        // DICE_SIZE_MULT  = size as fraction of screen height (square)
        // DICE_TOP_MULT   = from top of control bar
        // Dice is always exactly centered horizontally
        double diceSize = H * DICE_SIZE_MULT;
        diceView.setFitWidth(diceSize);
        diceView.setFitHeight(diceSize);
        AnchorPane.setLeftAnchor(diceView,   (W / 2) - (diceSize / 2) + 6);
        AnchorPane.setTopAnchor(diceView,    barH * DICE_TOP_MULT);
        AnchorPane.setRightAnchor(diceView,  null);
        AnchorPane.setBottomAnchor(diceView, null);

        // Dice result label — centered below dice
        AnchorPane.setLeftAnchor(diceResultLabel,   (W / 2) - 60);
        AnchorPane.setBottomAnchor(diceResultLabel, 4.0);
        AnchorPane.setRightAnchor(diceResultLabel,  null);
        AnchorPane.setTopAnchor(diceResultLabel,    null);

        // ── BUTTONS ────────────────────────────────────────────
        // BTN_W_MULT      = button width as fraction of screen width
        // BTN_H_MULT      = button height as fraction of screen height
        // BTN_TOP_MULT    = from top of control bar
        // POWERUP_RIGHT_MULT = distance from right edge as fraction of screen width
        // ROLL_RIGHT_MULT    = distance from right edge as fraction of screen width
        double btnW = W * BTN_W_MULT;
        double btnH = H * BTN_H_MULT;

        powerUpImageBtn.setFitWidth(btnW);
        powerUpImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(powerUpImageBtn,  W * POWERUP_RIGHT_MULT);
        AnchorPane.setTopAnchor(powerUpImageBtn,    barH * BTN_TOP_MULT);
        AnchorPane.setLeftAnchor(powerUpImageBtn,   null);
        AnchorPane.setBottomAnchor(powerUpImageBtn, null);

        rollImageBtn.setFitWidth(btnW);
        rollImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(rollImageBtn,  W * ROLL_RIGHT_MULT);
        AnchorPane.setTopAnchor(rollImageBtn,    barH * BTN_TOP_MULT);
        AnchorPane.setLeftAnchor(rollImageBtn,   null);
        AnchorPane.setBottomAnchor(rollImageBtn, null);
    }

    // =========================================================
    //  SMOOTH PARALLAX
    // =========================================================
    private void setupParallax() {
        parallaxTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double currentX = backgroundView.getTranslateX();
                double currentY = backgroundView.getTranslateY();
                double newX = currentX + (targetX - currentX) * 0.08;
                double newY = currentY + (targetY - currentY) * 0.08;
                backgroundView.setTranslateX(newX);
                backgroundView.setTranslateY(newY);
            }
        };
        parallaxTimer.start();

        backgroundRoot.setOnMouseMoved(e -> {
            double w = backgroundRoot.getWidth();
            double h = backgroundRoot.getHeight();
            targetX = ((e.getX() / w) - 0.5) * -24;
            targetY = ((e.getY() / h) - 0.5) * -14;
        });
    }

    /** Stop parallax timer when leaving this screen to avoid memory leaks. */
    public void stopParallax() {
        if (parallaxTimer != null) parallaxTimer.stop();
    }

    // =========================================================
    //  LOAD IMAGES
    // =========================================================
    private void loadAllImages() {
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
        conveyorImage                       = loadImage(IMG + "Conveyor_belt_cell.png");
        contaminationImage                  = loadImage(IMG + "contamination_sock_cell.png");
        cardCellImage                       = loadImage(IMG + "CardCell.png");
        monsterCellGreyImage                = loadImage(IMG + "MonsterCell_Grey.png");

        // Screen images
        screenImage_James     = loadImage(IMG + "James_Screen.png");   // James uses Henry_Screen
        screenImage_celia_mae = loadImage(IMG + "Celia_Mae_Screen.png");
        screenImage_Fungus    = loadImage(IMG + "FungusScreen.png");
        screenImage_Henry     = loadImage(IMG + "Henry_Screen.png");
        screenImage_Mike      = loadImage(IMG + "Mike_Screen.png");
        screenImage_Randall   = loadImage(IMG + "Randal_Screen.png");
        screenImage_Roz       = loadImage(IMG + "Rose_Screen.png");
        screenImage_Yeti      = loadImage(IMG + "Yeti_Screen.png");

        energy0   = loadImage(IMG + "0_Energy_Player.png");
        energy25  = loadImage(IMG + "25_Energy_Player.png");
        energy50  = loadImage(IMG + "50_Energy_Player.png");
        energy75  = loadImage(IMG + "75_Energy_Player.png");
        energy100 = loadImage(IMG + "100_Energy_Player.png");

        deckFull  = loadImage(IMG + "CardsDeck_Full.png");
        deckMid   = loadImage(IMG + "CardsDeck_Mid.png");
        deckLeast = loadImage(IMG + "CardsDeck_Least.png");

        for (int i = 1; i <= 6; i++)
            diceImages[i - 1] = loadImage(IMG + "Dice_on_" + i + ".png");

        cardBackImage         = loadImage(IMG + "card_back_design.jpg");
        card_2319Alert        = loadImage(IMG + "2319_alert.png");
        cardContaminationCode = loadImage(IMG + "contamination_code.png");
        cardMegaDrain         = loadImage(IMG + "mega_drain.png");
        cardMindScramble      = loadImage(IMG + "mind_scramble.png");
        cardPositionSwap      = loadImage(IMG + "position_swap.png");
        cardSmallSnatcher     = loadImage(IMG + "small_snatcher.png");
        cardSneakyThief       = loadImage(IMG + "sneaky_theif.png");
        cardSuperShield       = loadImage(IMG + "super_shield.png");
        cardTotalConfusion    = loadImage(IMG + "total_confusion.png");

        if (powerUpImageBtn != null) {
            powerUpImageBtn.setImage(loadImage(IMG + "Power_Up_Button.png"));
            powerUpImageBtn.setOnMouseEntered(e -> powerUpImageBtn.setOpacity(0.80));
            powerUpImageBtn.setOnMouseExited(e  -> powerUpImageBtn.setOpacity(1.00));
        }
        if (rollImageBtn != null) {
            rollImageBtn.setImage(loadImage(IMG + "Roll_Button.png"));
            rollImageBtn.setOnMouseEntered(e -> rollImageBtn.setOpacity(0.80));
            rollImageBtn.setOnMouseExited(e  -> rollImageBtn.setOpacity(1.00));
        }
        if (cardDeckView != null) cardDeckView.setImage(deckFull);
        if (diceView     != null) diceView.setImage(diceImages[0]);
    }

    // =========================================================
    //  BUILD GRID
    // =========================================================
    private void buildGrid() {
        backgroundViews = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        monsterViews    = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        energyLabels    = new Label[Constants.BOARD_ROWS][Constants.BOARD_COLS];

        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            for (int col = 0; col < Constants.BOARD_COLS; col++) {
                StackPane cellStack = new StackPane();

                int backendRow = Constants.BOARD_ROWS - 1 - row;
                int backendCol = (backendRow % 2 == 1)
                    ? Constants.BOARD_COLS - 1 - col : col;
                int boardIndex = backendRow * Constants.BOARD_COLS + backendCol;

                boolean isMonsterSlot = false;
                for (int mi : Constants.MONSTER_CELL_INDICES)
                    if (mi == boardIndex) { isMonsterSlot = true; break; }

                ImageView bgView = new ImageView();
                bgView.setPreserveRatio(false);
                bgView.fitWidthProperty().bind(
                    grid.widthProperty().divide(Constants.BOARD_COLS));
                bgView.fitHeightProperty().bind(
                    grid.heightProperty().divide(Constants.BOARD_ROWS));

                ImageView mView = new ImageView();
                mView.setPreserveRatio(true);
                double mScale = isMonsterSlot ? 1.0 : 0.80;
                mView.fitWidthProperty().bind(
                    grid.widthProperty().divide(Constants.BOARD_COLS).multiply(mScale));
                mView.fitHeightProperty().bind(
                    grid.heightProperty().divide(Constants.BOARD_ROWS).multiply(mScale));

                Label indexLabel = new Label(String.valueOf(boardIndex));
                indexLabel.setStyle(
                    "-fx-font-family: " + LED + ";" +
                    "-fx-font-size: 7px;" +
                    "-fx-text-fill: rgba(255,255,255,0.65);" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 1 2 0 0;");
                StackPane.setAlignment(indexLabel, Pos.TOP_RIGHT);

                Label energyLabel = new Label("");
                energyLabel.setStyle(
                    "-fx-font-family: " + LED + ";" +
                    "-fx-font-size: 7px;" +
                    "-fx-text-fill: #FFD700;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 0 0 1 0;");
                energyLabel.setVisible(false);
                StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER);

                backgroundViews[row][col] = bgView;
                monsterViews[row][col]    = mView;
                energyLabels[row][col]    = energyLabel;

                cellStack.getChildren().addAll(bgView, mView, indexLabel, energyLabel);
                grid.add(cellStack, col, row);
            }
        }
    }

    // =========================================================
    //  PLAYER PANEL
    // =========================================================
    private void buildPlayerPanel() {
        playerPanelContainer.setAlignment(Pos.TOP_CENTER);
        playerPortrait.setPreserveRatio(true);
        playerEnergyBar.setPreserveRatio(true);
        VBox.setMargin(playerPortrait,  new Insets(11, -30, 0, 0));
        VBox.setMargin(playerEnergyBar, new Insets(0,  -30, 0, 0));

        playerNameLabel     = ledLabel("-",              "white",   12, true);
        playerTypeLabel     = ledLabel("Type: -",        "#aaaaaa", 10, false);
        playerOrigRoleLabel = ledLabel("Orig: -",        "white",   10, false);
        playerCurrRoleLabel = ledLabel("Curr: -",        "white",   10, false);
        playerPosLabel      = ledLabel("Pos: -",         "#00ffff", 11, true);
        playerEnergyLabel   = ledLabel("Energy: -/1000", "#00ff88", 11, true);
        playerStatusLabel   = ledLabel("Normal",         "#aaaaaa", 10, false);
        playerTurnLabel     = ledLabel("",               "#ffcc00", 12, true);

        playerPanelContainer.getChildren().addAll(
            playerPortrait, playerNameLabel, playerTypeLabel,
            playerOrigRoleLabel, playerCurrRoleLabel,
            playerPosLabel, playerEnergyLabel,
            playerEnergyBar, playerStatusLabel, playerTurnLabel
        );
    }

    // =========================================================
    //  OPPONENT PANEL
    // =========================================================
    private void buildOpponentPanel() {
        opponentPanelContainer.setAlignment(Pos.TOP_CENTER);
        opponentPortrait.setPreserveRatio(true);
        opponentEnergyBar.setPreserveRatio(true);

        opponentNameLabel     = ledLabel("-",              "white",   12, true);
        opponentTypeLabel     = ledLabel("Type: -",        "#aaaaaa", 10, false);
        opponentOrigRoleLabel = ledLabel("Orig: -",        "white",   10, false);
        opponentCurrRoleLabel = ledLabel("Curr: -",        "white",   10, false);
        opponentPosLabel      = ledLabel("Pos: -",         "#00ffff", 11, true);
        opponentEnergyLabel   = ledLabel("Energy: -/1000", "#ff6666", 11, true);
        opponentStatusLabel   = ledLabel("Normal",         "#aaaaaa", 10, false);

        opponentPanelContainer.getChildren().addAll(
            opponentPortrait, opponentNameLabel, opponentTypeLabel,
            opponentOrigRoleLabel, opponentCurrRoleLabel,
            opponentPosLabel, opponentEnergyLabel,
            opponentEnergyBar, opponentStatusLabel
        );
    }

    // =========================================================
    //  ACTION LOG
    // =========================================================
    private void buildActionLog() {
        actionLogContainer.setStyle("-fx-padding: 6;");
        Label title = ledLabel("ACTION LOG", "#ffcc00", 10, true);
        actionLine1 = ledLabel("", "white",   10, false);
        actionLine2 = ledLabel("", "#aaffaa", 10, false);
        actionLine3 = ledLabel("", "#aaaaff", 10, false);
        for (Label l : new Label[]{actionLine1, actionLine2, actionLine3}) {
            l.setWrapText(true);
            l.setMaxWidth(190);
        }
        actionLogContainer.getChildren().addAll(
            title, actionLine1, actionLine2, actionLine3);
    }

    // =========================================================
    //  DICE PANEL
    // =========================================================
    private void buildDicePanel() {
        diceContainer.setStyle("-fx-padding: 4;");
        diceContainer.setAlignment(Pos.CENTER);
        Label title = ledLabel("LAST ROLL", "#ffcc00", 10, true);
        diceContainer.getChildren().add(title);
    }

    // =========================================================
    //  CARD OVERLAY
    // =========================================================
    private void buildCardOverlay() {
        cardOverlay = new VBox(10);
        cardOverlay.setAlignment(Pos.CENTER);
        cardOverlay.setStyle(
            "-fx-background-color: rgba(0,0,0,0.82);" +
            "-fx-padding: 28; -fx-background-radius: 16;");
        cardOverlay.setMaxWidth(270);
        cardOverlay.setMaxHeight(460);
        cardOverlay.setVisible(false);
        cardOverlay.setOpacity(0);

        cardOverlayBack = new ImageView(cardBackImage);
        cardOverlayBack.setFitWidth(180);
        cardOverlayBack.setFitHeight(220);
        cardOverlayBack.setPreserveRatio(true);

        cardOverlayFace = new ImageView();
        cardOverlayFace.setFitWidth(180);
        cardOverlayFace.setFitHeight(220);
        cardOverlayFace.setPreserveRatio(true);
        cardOverlayFace.setVisible(false);

        cardOverlayName = new Label();
        cardOverlayName.setStyle(
            "-fx-text-fill: #ffcc00; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-font-family: " + LED + ";");
        cardOverlayName.setWrapText(true);
        cardOverlayName.setMaxWidth(230);
        cardOverlayName.setAlignment(Pos.CENTER);

        cardOverlayDesc = new Label();
        cardOverlayDesc.setStyle(
            "-fx-text-fill: white; -fx-font-size: 11px;" +
            "-fx-font-family: " + LED + ";");
        cardOverlayDesc.setWrapText(true);
        cardOverlayDesc.setMaxWidth(230);
        cardOverlayDesc.setAlignment(Pos.CENTER);

        cardOverlayEffect = new Label();
        cardOverlayEffect.setStyle(
            "-fx-text-fill: #00ffff; -fx-font-size: 11px;" +
            "-fx-font-weight: bold; -fx-font-family: " + LED + ";");
        cardOverlayEffect.setWrapText(true);
        cardOverlayEffect.setMaxWidth(230);
        cardOverlayEffect.setAlignment(Pos.CENTER);

        Label hint = new Label("tap to continue");
        hint.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-font-style: italic;");

        cardOverlay.getChildren().addAll(
            cardOverlayBack, cardOverlayFace,
            cardOverlayName, cardOverlayDesc, cardOverlayEffect, hint);
        cardOverlay.setOnMouseClicked(e -> dismissCardOverlay());
        boardContainer.getChildren().add(cardOverlay);
        StackPane.setAlignment(cardOverlay, Pos.CENTER);
    }

    private void showCardOverlay(Card card) {
        cardOverlayName.setText(card.getName());
        cardOverlayDesc.setText(card.getDescription());
        cardOverlayEffect.setText("Effect: " + getCardEffectType(card.getName()));
        cardOverlayFace.setImage(getCardImage(card.getName()));
        cardOverlayBack.setVisible(true);
        cardOverlayFace.setVisible(false);
        cardOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cardOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        PauseTransition hold = new PauseTransition(Duration.millis(800));
        hold.setOnFinished(e -> {
            cardOverlayBack.setVisible(false);
            cardOverlayFace.setVisible(true);
        });
        new SequentialTransition(fadeIn, hold).play();
    }

    private void dismissCardOverlay() {
        FadeTransition out = new FadeTransition(Duration.millis(200), cardOverlay);
        out.setFromValue(1);
        out.setToValue(0);
        out.setOnFinished(e -> {
            cardOverlay.setVisible(false);
            refreshBoard();
            updateUI();
        });
        out.play();
    }

    private String getCardEffectType(String cardName) {
        switch (cardName) {
            case "Position Swap":      return "SWAP POSITIONS";
            case "2319 Alert":         return "OPPONENT → START";
            case "Contamination Code": return "PLAYER → START";
            case "Small Snatcher":     return "STEAL 50 ENERGY";
            case "Sneaky Thief":       return "STEAL 100 ENERGY";
            case "Mega Drain":         return "STEAL 150 ENERGY";
            case "Super Shield":       return "SHIELD ACTIVATED";
            case "Mind Scramble":      return "CONFUSION 2 TURNS";
            case "Total Confusion":    return "CONFUSION 3 TURNS";
            default:                   return "SPECIAL EFFECT";
        }
    }

    // =========================================================
    //  GAME START
    // =========================================================
    public void startGame(Role playerRole) {
        try {
            this.game = new Game(playerRole);
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
        if (game == null) return;
        Cell[][] cells = game.getBoard().getBoardCells();

        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setImage(null);
                energyLabels[r][c].setVisible(false);
            }

        for (int index = 0; index < 100; index++) {
            int bRow = index / Constants.BOARD_COLS;
            int bCol = index % Constants.BOARD_COLS;
            if (bRow % 2 == 1) bCol = Constants.BOARD_COLS - 1 - bCol;
            Cell cell = cells[bRow][bCol];
            int[] rc = indexToRowCol(index);
            if (cell != null) {
                setCellImage(rc[0], rc[1], cell);
                if (cell instanceof DoorCell) {
                    DoorCell door = (DoorCell) cell;
                    if (!door.isActivated()) {
                        energyLabels[rc[0]][rc[1]].setText("⚡" + door.getEnergy());
                        energyLabels[rc[0]][rc[1]].setVisible(true);
                    }
                }
            } else {
                backgroundViews[rc[0]][rc[1]].setImage(normalImage);
            }
        }

        for (Monster m : Board.getStationedMonsters()) {
            int[] rc = indexToRowCol(m.getPosition());
            monsterViews[rc[0]][rc[1]].setImage(getMonsterImage(m.getName()));
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
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        row = Constants.BOARD_ROWS - 1 - row;
        return new int[]{row, col};
    }

    private void setCellImage(int row, int col, Cell cell) {
        if (cell instanceof MonsterCell)
            backgroundViews[row][col].setImage(monsterCellGreyImage);
        else if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            backgroundViews[row][col].setImage(door.isActivated()
                ? (door.getRole() == Role.SCARER ? scarerOpenDoorImage : laugherOpenDoorImage)
                : (door.getRole() == Role.SCARER ? ScarerdoorImage : laugherdoorImage));
        } else if (cell instanceof ConveyorBelt)
            backgroundViews[row][col].setImage(conveyorImage);
        else if (cell instanceof ContaminationSock)
            backgroundViews[row][col].setImage(contaminationImage);
        else if (cell instanceof CardCell)
            backgroundViews[row][col].setImage(cardCellImage);
        else
            backgroundViews[row][col].setImage(normalImage);
    }

    // =========================================================
    //  UI UPDATE
    // =========================================================
    private void updateUI() {
        if (game == null) return;
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        Monster current  = game.getCurrent();

        playerPortrait.setImage(getMonsterScreenImage(player.getName()));
        playerNameLabel.setText(player.getName());
        playerTypeLabel.setText("Type: " + player.getClass().getSimpleName());
        playerOrigRoleLabel.setText("Orig: " + player.getOriginalRole());
        playerCurrRoleLabel.setText("Curr: " + player.getRole());
        boolean pConfused = !player.getOriginalRole().equals(player.getRole());
        playerCurrRoleLabel.setStyle(
            "-fx-text-fill: " + (pConfused ? "#ff00ff" : "white") + ";" +
            "-fx-font-size: " + (int)(10 * TEXT_SCALE) + "px;" +
            "-fx-font-family: " + LED + ";" +
            (pConfused ? "-fx-font-weight: bold;" : ""));
        playerPosLabel.setText("Pos: " + player.getPosition());
        playerEnergyLabel.setText("Energy: " + player.getEnergy() + "/1000");
        animateEnergyBar(playerEnergyBar, player.getEnergy());
        playerStatusLabel.setText(buildStatusString(player));
        playerTurnLabel.setText(current == player ? "▶ YOUR TURN" : "");

        opponentPortrait.setImage(getMonsterScreenImage(opponent.getName()));
        opponentNameLabel.setText(opponent.getName());
        opponentTypeLabel.setText("Type: " + opponent.getClass().getSimpleName());
        opponentOrigRoleLabel.setText("Orig: " + opponent.getOriginalRole());
        opponentCurrRoleLabel.setText("Curr: " + opponent.getRole());
        boolean oConfused = !opponent.getOriginalRole().equals(opponent.getRole());
        opponentCurrRoleLabel.setStyle(
            "-fx-text-fill: " + (oConfused ? "#ff00ff" : "white") + ";" +
            "-fx-font-size: " + (int)(10 * TEXT_SCALE) + "px;" +
            "-fx-font-family: " + LED + ";" +
            (oConfused ? "-fx-font-weight: bold;" : ""));
        opponentPosLabel.setText("Pos: " + opponent.getPosition());
        opponentEnergyLabel.setText("Energy: " + opponent.getEnergy() + "/1000");
        animateEnergyBar(opponentEnergyBar, opponent.getEnergy());
        opponentStatusLabel.setText(buildStatusString(opponent));

        myLabel.setText(current == player
            ? "YOUR TURN — press ROLL!"
            : "OPPONENT'S TURN — press ROLL!");
        myLabel.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-font-family: " + LED + ";" +
            "-fx-padding: 5 0 3 0; -fx-text-fill: " +
            (current == player ? "#00ff88" : "#ff6666") + ";");
    }

    private String buildStatusString(Monster m) {
        StringBuilder sb = new StringBuilder();
        if (m.isShielded()) sb.append("[Shield] ");
        if (m.isConfused()) sb.append("[Confused:" + m.getConfusionTurns() + "t] ");
        if (m.isFrozen())   sb.append("[Frozen] ");
        if (m instanceof game.engine.monsters.Dasher) {
            int mt = ((game.engine.monsters.Dasher) m).getMomentumTurns();
            if (mt > 0) sb.append("[Rush:" + mt + "t] ");
        }
        if (m instanceof game.engine.monsters.MultiTasker) {
            int ft = ((game.engine.monsters.MultiTasker) m).getNormalSpeedTurns();
            if (ft > 0) sb.append("[Focus:" + ft + "t] ");
        }
        if (sb.length() == 0) sb.append("Normal");
        return sb.toString().trim();
    }

    private void animateEnergyBar(ImageView bar, int energy) {
        int pct = (int) Math.min(100, Math.max(0, (energy / 1000.0) * 100));
        Image target;
        if      (pct >= 75) target = energy100;
        else if (pct >= 50) target = energy75;
        else if (pct >= 25) target = energy50;
        else if (pct > 0)   target = energy25;
        else                target = energy0;
        if (bar.getImage() == target) return;
        FadeTransition out = new FadeTransition(Duration.millis(200), bar);
        out.setFromValue(1); out.setToValue(0);
        out.setOnFinished(e -> {
            bar.setImage(target);
            FadeTransition in = new FadeTransition(Duration.millis(200), bar);
            in.setFromValue(0); in.setToValue(1);
            in.play();
        });
        out.play();
    }

    private void updateDeckImage() {
        if (cardDeckView == null) return;
        int remaining = Board.cards.size();
        int total     = Board.getOriginalCards().size();
        if (total == 0) return;
        double ratio = (double) remaining / total;
        cardDeckView.setImage(ratio > 0.60 ? deckFull
            : ratio > 0.25 ? deckMid : deckLeast);
    }

    // =========================================================
    //  ROLL HANDLER
    // =========================================================
    @FXML
    private void handleRollDice() {
        if (game == null || cardOverlay.isVisible() || isAnimating) return;
        try {
            isAnimating = true;
            Monster current  = game.getCurrent();
            Monster opponent = current == game.getPlayer()
                ? game.getOpponent() : game.getPlayer();

            int oldPos       = current.getPosition();
            int oldEnergy    = current.getEnergy();
            int oldOppEnergy = opponent.getEnergy();
            boolean wasShielded = current.isShielded();

            Card topCard = Board.cards.isEmpty() ? null : Board.cards.get(0);
            if (Board.cards.isEmpty()) Board.reloadCards();

            if (current.isFrozen()) {
                game.playTurn();
                actionLine1.setText("❄ " + current.getName() + " FROZEN — skipped!");
                actionLine2.setText(""); actionLine3.setText("");
                refreshBoard(); updateUI();
                isAnimating = false;
                return;
            }

            game.playTurn();

            int newPos       = current.getPosition();
            int newEnergy    = current.getEnergy();
            int newOppEnergy = opponent.getEnergy();
            boolean cardDrawn = topCard != null &&
                (Board.cards.isEmpty() || Board.cards.get(0) != topCard);

            int moved    = newPos - oldPos;
            if (moved < 0) moved += 100;
            int diceFace = Math.max(1, Math.min(6, moved));

            actionLine1.setText(current.getName() + " → pos " + newPos + " (+" + moved + ")");
            actionLine2.setText(cardDrawn && topCard != null
                ? "🃏 " + topCard.getName() + ": " + getCardEffectType(topCard.getName()) : "");

            if (wasShielded && !current.isShielded())
                actionLine3.setText("🛡 Shield blocked energy loss!");
            else if (newEnergy != oldEnergy) {
                int diff = newEnergy - oldEnergy;
                actionLine3.setText(current.getName() + " energy "
                    + (diff > 0 ? "+" : "") + diff + " → " + newEnergy);
            } else if (newOppEnergy != oldOppEnergy) {
                int diff = newOppEnergy - oldOppEnergy;
                actionLine3.setText(opponent.getName() + " energy "
                    + (diff > 0 ? "+" : "") + diff + " → " + newOppEnergy);
            } else actionLine3.setText("");

            animateDiceRoll(diceFace, () -> {
                Runnable afterMove = () -> {
                    if (cardDrawn && topCard != null) {
                        refreshBoard(); updateUI();
                        showCardOverlay(topCard);
                    } else { refreshBoard(); updateUI(); }
                    checkWinner();
                    isAnimating = false;
                };
                animateMonsterMove(current, opponent, oldPos, newPos, afterMove);
            });

        } catch (game.engine.exceptions.InvalidMoveException ex) {
            actionLine1.setText("⚠ INVALID: " + ex.getMessage());
            actionLine2.setText("Roll again!"); actionLine3.setText("");
            refreshBoard(); updateUI();
            isAnimating = false;
            showErrorAlert("Invalid Move", ex.getMessage());
        } catch (Exception ex) {
            actionLine1.setText("Error: " + ex.getMessage());
            ex.printStackTrace(); isAnimating = false;
        }
    }

    // =========================================================
    //  CHECK WINNER
    // =========================================================
    private void checkWinner() {
        if (game.getWinner() == null) return;

        Monster winner   = game.getWinner();
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();

        // Stop parallax so it doesn't keep running after we leave
        if (parallaxTimer != null) parallaxTimer.stop();

        myLabel.setText(winner.getName() + " WINS! 🏆");
        myLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;" +
            "-fx-font-family: " + LED + "; -fx-text-fill: #ffcc00;");

        SceneManager.getInstance().switchToGameOverScreen(
            winner.getName(),
            winner.getRole().toString(),
            player.getRole(),
            player.getName(),
            player.getRole().toString(),
            player.getEnergy(),
            opponent.getName(),
            opponent.getRole().toString(),
            opponent.getEnergy()
        );
    }

    // =========================================================
    //  DICE ANIMATION
    // =========================================================
    private void animateDiceRoll(int finalFace, Runnable onFinished) {
        if (diceTimeline != null) diceTimeline.stop();
        diceTimeline = new Timeline();
        java.util.Random rand = new java.util.Random();
        int[] delays = {60, 80, 100, 130, 160, 200, 250, 320, 400};
        int elapsed = 0;
        for (int delay : delays) {
            elapsed += delay;
            final int face = rand.nextInt(6);
            diceTimeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(elapsed),
                e -> diceView.setImage(diceImages[face])));
        }
        elapsed += 300;
        final int total = elapsed;
        diceTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(total), e -> {
            diceView.setImage(diceImages[finalFace - 1]);
            diceResultLabel.setText("Rolled: " + finalFace);
            if (onFinished != null) onFinished.run();
        }));
        diceTimeline.play();
    }

    // =========================================================
    //  MONSTER MOVEMENT
    // =========================================================
    private void animateMonsterMove(Monster current, Monster opponent,
                                    int oldPos, int newPos, Runnable onFinished) {
        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setTranslateX(0);
                monsterViews[r][c].setTranslateY(0);
                monsterViews[r][c].setImage(null);
            }

        for (Monster m : Board.getStationedMonsters()) {
            int[] rc = indexToRowCol(m.getPosition());
            monsterViews[rc[0]][rc[1]].setImage(getMonsterImage(m.getName()));
        }

        drawMonsterOverlay(opponent);
        int[] startRC = indexToRowCol(oldPos);
        monsterViews[startRC[0]][startRC[1]].setImage(getMonsterImage(current.getName()));

        SequentialTransition seq = new SequentialTransition();
        int dist = Math.abs(newPos - oldPos);
        if (dist > 12 || oldPos == newPos)
            seq.getChildren().add(createHop(current, opponent, oldPos, newPos, 600));
        else {
            int step = newPos > oldPos ? 1 : -1;
            for (int pos = oldPos; pos != newPos; pos += step)
                seq.getChildren().add(createHop(current, opponent, pos, pos + step, 250));
        }
        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }

    private Animation createHop(Monster moving, Monster stationary,
                                 int from, int to, int ms) {
        int[] fromRC = indexToRowCol(from);
        int[] toRC   = indexToRowCol(to);
        double cellW = grid.getWidth()  / Constants.BOARD_COLS;
        double cellH = grid.getHeight() / Constants.BOARD_ROWS;
        double dx = (toRC[1] - fromRC[1]) * cellW;
        double dy = (toRC[0] - fromRC[0]) * cellH;

        ImageView mv = monsterViews[fromRC[0]][fromRC[1]];
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), mv);
        tt.setByX(dx); tt.setByY(dy);
        tt.setOnFinished(e -> {
            mv.setTranslateX(0); mv.setTranslateY(0); mv.setImage(null);
            for (Monster stationed : Board.getStationedMonsters())
                if (stationed.getPosition() == from) {
                    mv.setImage(getMonsterImage(stationed.getName())); break;
                }
            if (stationary.getPosition() == from && mv.getImage() == null)
                mv.setImage(getMonsterImage(stationary.getName()));
            monsterViews[toRC[0]][toRC[1]].setImage(getMonsterImage(moving.getName()));
        });

        PauseTransition front = new PauseTransition(Duration.millis(1));
        front.setOnFinished(e -> mv.getParent().toFront());
        return new SequentialTransition(front, tt);
    }

    // =========================================================
    //  POWER-UP
    // =========================================================
    @FXML
    private void handlePowerUp() {
        if (game == null || cardOverlay.isVisible()) return;
        boolean ok = showConfirmDialog("Use Powerup", "Activate Powerup?",
            "Costs " + Constants.POWERUP_COST + " energy. Proceed?");
        if (ok) {
            try {
                String name = game.getCurrent().getName();
                game.usePowerup();
                actionLine1.setText("⚡ " + name + " activated powerup!");
                actionLine2.setText(""); actionLine3.setText("");
                refreshBoard(); updateUI();
            } catch (Exception ex) {
                showErrorAlert("Powerup Failed", ex.getMessage());
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
        Label h = new Label(header);
        h.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label c = new Label(content); c.setWrapText(true);
        Button ok = new Button("OK"), no = new Button("Cancel");
        ok.setOnAction(e -> { result[0] = true; dialog.close(); });
        no.setOnAction(e -> dialog.close());
        HBox btns = new HBox(10, ok, no); btns.setAlignment(Pos.CENTER);
        VBox layout = new VBox(12, h, c, btns);
        layout.setPadding(new Insets(20)); layout.setAlignment(Pos.CENTER);
        dialog.setScene(new Scene(layout, 320, 160));
        dialog.showAndWait();
        return result[0];
    }

    private void showErrorAlert(String title, String msg) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Label h = new Label("⚠ " + title);
        h.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: red;");
        Label c = new Label(msg != null ? msg : "Unknown error"); c.setWrapText(true);
        Button ok = new Button("OK"); ok.setOnAction(e -> dialog.close());
        HBox btns = new HBox(ok); btns.setAlignment(Pos.CENTER);
        VBox layout = new VBox(12, h, c, btns);
        layout.setPadding(new Insets(20)); layout.setAlignment(Pos.CENTER);
        dialog.setScene(new Scene(layout, 320, 140));
        dialog.showAndWait();
    }

    // =========================================================
    //  CHEAT CODES
    // =========================================================
    private void setupCheatCodes() {
        backgroundRoot.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) scene.setOnKeyPressed(this::handleCheatKeys);
        });
    }

    private void handleCheatKeys(KeyEvent e) {
        if (game == null || isAnimating || cardOverlay.isVisible()) return;
        Monster current = game.getCurrent();
        if (e.getCode() == KeyCode.W) {
            current.setPosition(99);
            actionLine1.setText("CHEAT: warped!");
            actionLine2.setText(""); actionLine3.setText("");
            refreshBoard(); updateUI(); checkWinner();
        } else if (e.getCode() == KeyCode.E) {
            current.setEnergy(current.getEnergy() + 50);
            actionLine1.setText("CHEAT: +50 energy!");
            actionLine2.setText(""); actionLine3.setText("");
            refreshBoard(); updateUI();
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    /**
     * Loads an image from the classpath, caching the result so each file is
     * read from disk only once.  Large background/panel images are capped at
     * 1280×920 to reduce heap usage; all other images are loaded at full size.
     * Add {@code -Xmx512m} to VM args if you still see OutOfMemoryErrors.
     */
    private Image loadImage(String path) {
        if (IMAGE_CACHE.containsKey(path))
            return IMAGE_CACHE.get(path);

        java.io.InputStream s = getClass().getResourceAsStream(path);
        if (s == null) {
            System.err.println("WARNING: not found: " + path);
            IMAGE_CACHE.put(path, null);
            return null;
        }
        try {
            boolean isBackground = path.contains("background") || path.contains("ControlPanel")
                || path.contains("BoardHolder") || path.contains("Board.png");
            Image img = isBackground
                ? new Image(s, 1280, 920, false, true)
                : new Image(s);
            IMAGE_CACHE.put(path, img);
            return img;
        } catch (OutOfMemoryError oom) {
            System.err.println("OOM loading: " + path + " — add -Xmx512m to VM args");
            IMAGE_CACHE.put(path, null);
            return null;
        } catch (Exception e) {
            System.err.println("ERROR loading image: " + path + " — " + e.getMessage());
            IMAGE_CACHE.put(path, null);
            return null;
        }
    }

    private Image getMonsterImage(String name) {
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":               return monsterImage_celia_mae;
            case "fungus":                  return monsterImage_Fungus;
            case "henry j. waternoose":
            case "henry j. waternoose iii": return monsterImage_Henry_J_Waternoose_III;
            case "james p. sullivan":
            case "james sullivan":          return monsterImage_James_sullivan;
            case "mike wazowski":           return monsterImage_Mike_Wazowski;
            case "randall boggs":
            case "randall":                 return monsterImage_Randall;
            case "roz":                     return monsterImage_Roz;
            case "yeti":                    return monsterImage_Yeti;
            default:                        return null;
        }
    }

    private Image getMonsterScreenImage(String name) {
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":               return screenImage_celia_mae;
            case "fungus":                  return screenImage_Fungus;
            case "henry j. waternoose":
            case "henry j. waternoose iii": return screenImage_Henry;
            case "mike wazowski":           return screenImage_Mike;
            case "randall boggs":
            case "randall":                 return screenImage_Randall;
            case "roz":                     return screenImage_Roz;
            case "yeti":                    return screenImage_Yeti;
            case "james p. sullivan":
            case "james sullivan":          return screenImage_James;
            default:                        return getMonsterImage(name);
        }
    }

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

    /**
     * Creates a styled label using the LED font.
     * TEXT_SCALE at the top of the file resizes all labels uniformly.
     */
    private Label ledLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        int scaledSize = (int)(size * TEXT_SCALE);
        l.setStyle(
            "-fx-text-fill: " + color + ";" +
            "-fx-font-size: " + scaledSize + "px;" +
            "-fx-font-family: " + LED + ";" +
            (bold ? "-fx-font-weight: bold;" : ""));
        l.setWrapText(true);
        l.setMaxWidth(200);
        return l;
    }
}