package game.gui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import game.engine.*;
import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.monsters.Monster;

public class GameController {

    // =========================================================
    //  FXML FIELDS
    // =========================================================
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

    // =========================================================
    //  FONT
    // =========================================================
    private static final String FONT      = "ARCADECLASSIC";
    private static final String FONT_PATH = "/game/gui/resources/fonts/ARCADECLASSIC.TTF";

    // =========================================================
    //  TEXT SIZES
    // =========================================================
    private static final int TXT_PLAYER_NAME   = 20;
    private static final int TXT_PLAYER_TYPE   = 18;
    private static final int TXT_PLAYER_ROLE   = 18;
    private static final int TXT_PLAYER_POS    = 20;
    private static final int TXT_PLAYER_ENERGY = 20;
    private static final int TXT_PLAYER_STATUS = 20;
    private static final int TXT_PLAYER_TURN   = 15;
    private static final int TXT_ACTION_LOG    = 20;
    private static final int TXT_TOP_LABEL     = 14;
    private static final int TXT_DICE_RESULT   = 20;
    private static final int TXT_CARD_NAME     = 20;
    private static final int TXT_CARD_BODY     = 16;
    private static final int TXT_CELL_INDEX    =  7;
    private static final int TXT_DOOR_ENERGY   =  7;

    // =========================================================
    //  SIZES
    // =========================================================
    private static final double BOARD_SIZE_MULT   = 0.66;
    private static final double SIDE_PANEL_W      = 0.155;
    private static final double PORTRAIT_W_MULT   = 0.88;
    private static final double ENERGY_BAR_W_MULT = 0.2;
    private static final double PANEL_TOP_PAD     = 0.08;
    private static final double LIGHT_SIZE_MULT   = 0.16;
    private static final double PROFILE_W_MULT    = 0.90;
    private static final double ACTION_LOG_W_MULT = 0.90;
    private static final double CONTROL_BAR_H     = 0.16;
    private static final double DECK_LEFT         = 0.25;
    private static final double DECK_TOP_FRAC     = -1.8;
    private static final double DECK_W            = 0.1584;
    private static final double DECK_H            = 0.3456;
    private static final double DICE_SIZE         = 0.25;
    private static final double DICE_TOP_FRAC     = -1.6;
    private static final double BTN_W             = 0.17;
    private static final double BTN_H             = 0.17;
    private static final double BTN_TOP_FRAC      = -1.4;
    private static final double ROLL_RIGHT        = 0.185;
    private static final double POWERUP_RIGHT     = 0.284;
    private static final double PARALLAX_X        = 28;
    private static final double PARALLAX_Y        = 18;
    private static final double BG_OVERSIZE       = 1.08;
    private static final double CARD_W_MULT       = 0.38;
    private static final double CARD_H_MULT       = 0.50;

    // =========================================================
    //  IMAGE PATHS
    // =========================================================
    private static final String IMG = "/game/gui/resources/images/";

    private static final String IMG_NORMAL       = "NormalCell.png";
    private static final String IMG_DOOR_SC      = "Scarer_ClosedDoor_Cell2.png";
    private static final String IMG_DOOR_LC      = "Laugher_ClosedDoor_Cell.png";
    private static final String IMG_DOOR_SO      = "Scarer_OpenDoor_Cell.png";
    private static final String IMG_DOOR_LO      = "Laugher_OpenDoor_Cell.png";
    private static final String IMG_MONSTER_CELL = "MonsterCell_Grey.png";
    private static final String IMG_CONVEYOR     = "Conveyor_belt_cell.png";
    private static final String IMG_SOCK         = "contamination_sock_cell.png";
    private static final String IMG_CARD_CELL    = "CardCell.png";
    private static final String IMG_BACKGROUND   = "background_Game.png";
    private static final String IMG_CONTROL      = "ControlPanel.png";
    private static final String IMG_BOARD_HOLDER = "Board_Holder.png";
    private static final String IMG_BOARD        = "images.png";
    private static final String IMG_POWERUP_BTN  = "Power_Up_Button.png";
    private static final String IMG_ROLL_BTN     = "Roll_Button.png";
    private static final String IMG_DECK_FULL    = "CardsDeck_Full.png";
    private static final String IMG_DECK_MID     = "CardsDeck_Mid.png";
    private static final String IMG_DECK_LEAST   = "CardsDeck_Least.png";
    private static final String IMG_TURN_OFF     = "turn_off.png";
    private static final String IMG_TURN_ON      = "turn_on.png";
    private static final String IMG_CONF_OFF     = "confusion_off.png";
    private static final String IMG_CONF_ON      = "confusion_on.png";
    private static final String IMG_FREEZE_OFF   = "freezed_off.png";
    private static final String IMG_FREEZE_ON    = "freezed_on.png";
    private static final String IMG_SHIELD_OFF   = "shield_off.png";
    private static final String IMG_SHIELD_ON    = "shield_on.png";
    private static final String IMG_POWER_OFF    = "powerup_off.png";
    private static final String IMG_POWER_ON     = "powerup_on.png";
    private static final String IMG_PROFILE      = "monster_profile.png";
    private static final String IMG_ACTION_LOG   = "Action_Log.png";
    private static final String IMG_CARD_BACK    = "card_back_design.jpg";

    // =========================================================
    //  IMAGE LOADING
    // =========================================================
    private Image img(String filename) {
        return ImageLoader.getInstance().loadImage(filename);
    }

    // =========================================================
    //  GRID ARRAYS
    // =========================================================
    private ImageView[][] bgViews;
    private ImageView[][] monsterViews;
    private Label[][]     energyLabels;

    // Cell size property for responsive label scaling
    private final javafx.beans.property.DoubleProperty cellSize =
        new javafx.beans.property.SimpleDoubleProperty(40);

    // =========================================================
    //  IMAGES
    // =========================================================
    private Image normalImage, doorSC, doorLC, doorSO, doorLO;
    private Image monsterCellImage, conveyorImage, sockImage, cardCellImage;
    private Image[] diceImages = new Image[6];
    private Image deckFull, deckMid, deckLeast;
    private Image mi_celia, mi_fungus, mi_henry, mi_james;
    private Image mi_mike, mi_randall, mi_roz, mi_yeti;
    private Image si_celia, si_fungus, si_henry, si_james;
    private Image si_mike, si_randall, si_roz, si_yeti;
    private Image en0, en25, en50, en75, en100;
    private Image cardBack, c2319, cContam, cMegaDrain, cMind;
    private Image cSwap, cSmall, cSneaky, cShield, cConfuse;

    // =========================================================
    //  PLAYER PANEL WIDGETS
    // =========================================================
    private final ImageView playerPortrait  = new ImageView();
    private final ImageView playerEnergyBar = new ImageView();
    private StackPane playerEnergyWrapper;
    private Label playerNameLbl, playerTypeLbl, playerRoleLbl;
    private Label playerPosLbl, playerEnergyLbl, playerStatusLbl, playerTurnLbl;

    private ImageView pTurnOff, pTurnOn;
    private ImageView pConfOff, pConfOn;
    private ImageView pFrzOff,  pFrzOn;
    private ImageView pShldOff, pShldOn;
    private ImageView pPwrOff,  pPwrOn;

    private boolean pTurnState, pConfState, pFrzState, pShldState, pPwrState;
    private boolean oTurnState, oConfState, oFrzState, oShldState, oPwrState;

    // =========================================================
    //  OPPONENT PANEL WIDGETS
    // =========================================================
    private final ImageView opponentPortrait  = new ImageView();
    private final ImageView opponentEnergyBar = new ImageView();
    private StackPane opponentEnergyWrapper;
    private Label opponentNameLbl, opponentTypeLbl, opponentRoleLbl;
    private Label opponentPosLbl, opponentEnergyLbl, opponentStatusLbl;

    private ImageView oTurnOff, oTurnOn;
    private ImageView oConfOff, oConfOn;
    private ImageView oFrzOff,  oFrzOn;
    private ImageView oShldOff, oShldOn;
    private ImageView oPwrOff,  oPwrOn;

    private ImageView playerProfileBg, opponentProfileBg;
    private ImageView actionLogBg;
    private Label actionLine1, actionLine2, actionLine3;

    // =========================================================
    //  CARD OVERLAY
    // =========================================================
    private VBox      cardOverlay;
    private ImageView cardOverlayBack, cardOverlayFace;
    private Label     cardOverlayName, cardOverlayDesc, cardOverlayEffect;
    private final BoxBlur worldBlur = new BoxBlur(0, 0, 2);

    // In-scene message overlay
    private StackPane messageOverlay;
    private VBox      messageBox;
    private Label     messageTitle;
    private Label     messageBody;
    private HBox      messageButtons;

    // =========================================================
    //  STATE
    // =========================================================
    private Game     game;
    private boolean  isAnimating           = false;
    private boolean  monsterOverlayVisible = false;
    private Timeline diceTimeline;

    private int playerPowerTurnsLeft   = 0;
    private int opponentPowerTurnsLeft = 0;

    private double targetX = 0, targetY = 0;
    private AnimationTimer parallaxTimer;

    private Image playerEnergyBarCurrentImage   = null;
    private Image opponentEnergyBarCurrentImage = null;

    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {
        try {
            javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream(FONT_PATH), 14);

            loadAllImages();

            backgroundView.setImage(img(IMG_BACKGROUND));
            backgroundView.fitWidthProperty().bind(
                backgroundRoot.widthProperty().multiply(BG_OVERSIZE));
            backgroundView.fitHeightProperty().bind(
                backgroundRoot.heightProperty().multiply(BG_OVERSIZE));
            backgroundView.setEffect(new BoxBlur(3, 3, 2));

            controlPanelView.setImage(img(IMG_CONTROL));
            controlPanelView.fitWidthProperty().bind(backgroundRoot.widthProperty());
            controlPanelView.fitHeightProperty().bind(backgroundRoot.heightProperty());

            setupParallax();

            javafx.beans.binding.NumberBinding boardSize =
                backgroundRoot.heightProperty().multiply(BOARD_SIZE_MULT);
            boardHolderView.setImage(img(IMG_BOARD_HOLDER));
            boardHolderView.fitWidthProperty().bind(boardSize.multiply(1.10));
            boardHolderView.fitHeightProperty().bind(boardSize.multiply(1.10));
            boardImageView.setImage(img(IMG_BOARD));
            boardImageView.fitWidthProperty().bind(boardSize);
            boardImageView.fitHeightProperty().bind(boardSize);
            grid.maxWidthProperty().bind(boardSize);
            grid.maxHeightProperty().bind(boardSize);
            grid.minWidthProperty().bind(boardSize);
            grid.minHeightProperty().bind(boardSize);

            buildGrid();
            buildPlayerPanel();
            buildOpponentPanel();
            buildActionLog();
            buildCardOverlay();
            buildMessageOverlay();

            if (powerUpImageBtn != null) {
                powerUpImageBtn.setImage(img(IMG_POWERUP_BTN));
                addButtonHover(powerUpImageBtn);
            }
            if (rollImageBtn != null) {
                rollImageBtn.setImage(img(IMG_ROLL_BTN));
                addButtonHover(rollImageBtn);
            }
            if (cardDeckView != null) cardDeckView.setImage(deckFull);
            if (diceView     != null) diceView.setImage(diceImages[0]);

            setupResponsiveLayout();
            setupCheatCodes();

            backgroundRoot.sceneProperty().addListener((obs, old, scene) -> {
                if (scene != null) {
                    scene.widthProperty().addListener((o, ov, nv) -> {
                        if (nv.doubleValue() > 0) Platform.runLater(this::applyAllLayout);
                    });
                    Platform.runLater(this::applyAllLayout);
                }
            });

            System.out.println("DEBUG: GameController.initialize() done");
        } catch (Exception e) {
            System.err.println("Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    //  LOAD IMAGES
    // =========================================================
    private void loadAllImages() {
        normalImage      = img(IMG_NORMAL);
        doorSC           = img(IMG_DOOR_SC);
        doorLC           = img(IMG_DOOR_LC);
        doorSO           = img(IMG_DOOR_SO);
        doorLO           = img(IMG_DOOR_LO);
        monsterCellImage = img(IMG_MONSTER_CELL);
        conveyorImage    = img(IMG_CONVEYOR);
        sockImage        = img(IMG_SOCK);
        cardCellImage    = img(IMG_CARD_CELL);
        for (int i = 1; i <= 6; i++) diceImages[i-1] = img("Dice_on_" + i + ".png");
        deckFull  = img(IMG_DECK_FULL);
        deckMid   = img(IMG_DECK_MID);
        deckLeast = img(IMG_DECK_LEAST);
        mi_celia   = img("celia mae.png");
        mi_fungus  = img("Fungus.png");
        mi_henry   = img("Henry_J._Waternoose_III.png");
        mi_james   = img("James sullivan.png");
        mi_mike    = img("Mike_Wazowski.png");
        mi_randall = img("Randall.png");
        mi_roz     = img("Roz.png");
        mi_yeti    = img("Yeti.png");
        si_celia   = img("Celia_Mae_Screen.png");
        si_fungus  = img("FungusScreen.png");
        si_henry   = img("Henry_Screen.png");
        si_james   = img("James_Screen.png");
        si_mike    = img("Mike_Screen.png");
        si_randall = img("Randal_Screen.png");
        si_roz     = img("Rose_Screen.png");
        si_yeti    = img("Yeti_Screen.png");
        en0   = img("0_Energy_Player.png");
        en25  = img("25_Energy_Player.png");
        en50  = img("50_Energy_Player.png");
        en75  = img("75_Energy_Player.png");
        en100 = img("100_Energy_Player.png");
        cardBack   = img(IMG_CARD_BACK);
        c2319      = img("2319_alert.png");
        cContam    = img("contamination_code.png");
        cMegaDrain = img("mega_drain.png");
        cMind      = img("mind_scramble.png");
        cSwap      = img("position_swap.png");
        cSmall     = img("small_snatcher.png");
        cSneaky    = img("sneaky_theif.png");
        cShield    = img("super_shield.png");
        cConfuse   = img("total_confusion.png");
    }

    // =========================================================
    //  BUILD GRID  (replaced with new version)
    // =========================================================
    private void buildGrid() {
        // Bind the shared cellSize property so label styles can react to resize
        cellSize.bind(grid.heightProperty().divide(Constants.BOARD_ROWS));

        bgViews      = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        monsterViews = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        energyLabels = new Label[Constants.BOARD_ROWS][Constants.BOARD_COLS];

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

                // Background image view
                ImageView bgView = new ImageView();
                bgView.setPreserveRatio(false);
                bgView.fitWidthProperty().bind(
                    grid.widthProperty().divide(Constants.BOARD_COLS));
                bgView.fitHeightProperty().bind(
                    grid.heightProperty().divide(Constants.BOARD_ROWS));

                // Monster sprite view
                ImageView mView = new ImageView();
                mView.setPreserveRatio(true);
                double mScale = isMonsterSlot ? 1.0 : 0.80;
                mView.fitWidthProperty().bind(
                    grid.widthProperty().divide(Constants.BOARD_COLS).multiply(mScale));
                mView.fitHeightProperty().bind(
                    grid.heightProperty().divide(Constants.BOARD_ROWS).multiply(mScale));

                // Cell index label — scales with cell size, has subtle background badge
                Label indexLabel = new Label(String.valueOf(boardIndex));
                indexLabel.setMouseTransparent(true);
                cellSize.addListener((obs, old, val) -> indexLabel.setStyle(
                    "-fx-font-family: '" + FONT + "';" +
                    "-fx-font-size: " + Math.max(TXT_CELL_INDEX,
                        val.doubleValue() * 0.18) + "px;" +
                    "-fx-text-fill: rgba(255,255,255,0.85);" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-color: rgba(0,0,0,0.35);" +
                    "-fx-background-radius: 5;" +
                    "-fx-padding: 1 4 1 4;"));
                // Apply initial style immediately
                indexLabel.setStyle(
                    "-fx-font-family: '" + FONT + "';" +
                    "-fx-font-size: " + TXT_CELL_INDEX + "px;" +
                    "-fx-text-fill: rgba(255,255,255,0.85);" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-color: rgba(0,0,0,0.35);" +
                    "-fx-background-radius: 5;" +
                    "-fx-padding: 1 4 1 4;");
                StackPane.setAlignment(indexLabel, Pos.TOP_RIGHT);

                // Energy label — styled dynamically based on cell type via userData
                Label energyLabel = new Label("");
                cellSize.addListener((obs, old, val) -> {
                    Object kind = energyLabel.getUserData();
                    if ("door".equals(kind)) {
                        applyDoorEnergyLabelStyle(energyLabel, val.doubleValue());
                    } else if ("monster".equals(kind)) {
                        applyMonsterEnergyLabelStyle(energyLabel, val.doubleValue());
                    }
                });
                energyLabel.setVisible(false);
                StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER);

                bgViews[row][col]      = bgView;
                monsterViews[row][col] = mView;
                energyLabels[row][col] = energyLabel;

                // Monster-slot cells get a hand cursor and click handler
                if (isMonsterSlot) {
                    final int monsterBoardIndex = boardIndex;
                    cellStack.setCursor(Cursor.HAND);
                    cellStack.setOnMouseClicked(e ->
                        handleMonsterCellClick(monsterBoardIndex));
                }

                cellStack.getChildren().addAll(bgView, mView, indexLabel, energyLabel);
                grid.add(cellStack, col, row);
            }
        }
    }

    // ── Label style helpers called from the cellSize listener ──────────────

    /** Door energy label: blue-tinted, centred at bottom */
    private void applyDoorEnergyLabelStyle(Label lbl, double cellPx) {
        lbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + Math.max(TXT_DOOR_ENERGY, cellPx * 0.15) + "px;" +
            "-fx-text-fill: rgba(175,228,255,0.95);" +
            "-fx-font-weight: bold;");
    }

    /** Monster energy label: green-tinted, centred at bottom */
    private void applyMonsterEnergyLabelStyle(Label lbl, double cellPx) {
        lbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + Math.max(TXT_DOOR_ENERGY, cellPx * 0.15) + "px;" +
            "-fx-text-fill: rgba(100,255,160,0.95);" +
            "-fx-font-weight: bold;");
    }

    // =========================================================
    //  MONSTER CELL CLICK HANDLER  (replaces showMonsterPopup)
    //  Clicking a monster cell now triggers the floating energy
    //  popup animation on every stationed-monster cell whose role
    //  matches the current player, mirroring the post-turn effect.
    // =========================================================

    /**
     * Called when the user clicks a monster-slot cell on the board.
     * Instead of a popup dialog it fires the floating ⚡ animation on
     * every MonsterCell whose stationed monster shares a role with the
     * current active player, so the player can see at a glance which
     * cells are "theirs" and what energy they hold.
     *
     * @param boardIndex the flat board index (0-99) of the clicked cell
     */
    private void handleMonsterCellClick(int boardIndex) {
        if (game == null) return;

        Monster clickedMonster = getMonsterAtCell(boardIndex);
        if (clickedMonster == null) return;

        // Show the info overlay for the specific monster that was clicked
        showMonsterInfoOverlay(clickedMonster);
    }

    /**
     * Displays a styled in-scene info card for the given monster.
     * Uses the existing message-overlay infrastructure so it stays
     * inside the fullscreen window and is never clipped by the OS.
     */
    /**
     * Shows a full card-style overlay for a monster — blurs the world,
     * displays the monster's screen portrait, then reveals a stats panel,
     * mirroring the card-draw presentation.
     */
    private void showMonsterInfoOverlay(Monster m) {
        monsterOverlayVisible = true;

        // ── Determine role-based accent colour ──────────────────────────
        boolean isScarer   = m.getRole() == Role.SCARER;
        String  accentHex  = isScarer ? "#ff6a00" : "#00ccff";
        Color   accentColor = isScarer ? Color.web("#ff6a00") : Color.web("#00ccff");

        // ── Portrait image (screen portrait, same as side panels) ────────
        ImageView portrait = new ImageView(screenPortrait(m.getName()));
        portrait.setPreserveRatio(true);
        portrait.setFitWidth(220);
        DropShadow portraitGlow = new DropShadow(24, accentColor);
        portraitGlow.setSpread(0.15);
        portrait.setEffect(portraitGlow);

        // ── Name label ───────────────────────────────────────────────────
        Label nameLbl = new Label(m.getName().toUpperCase());
        nameLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + accentHex + ";");
        nameLbl.setAlignment(Pos.CENTER);
        DropShadow nameGlow = new DropShadow(18, accentColor);
        nameGlow.setSpread(0.3);
        nameLbl.setEffect(nameGlow);

        // ── Thin divider bar ─────────────────────────────────────────────
        javafx.scene.shape.Rectangle divider = new javafx.scene.shape.Rectangle(200, 2);
        divider.setFill(accentColor);
        divider.setOpacity(0.6);
        divider.setArcWidth(2); divider.setArcHeight(2);

        // ── Stat rows helper ─────────────────────────────────────────────
        // Each row: dim label on left, bright value on right
        java.util.function.BiFunction<String, String, HBox> statRow = (key, val) -> {
            Label k = new Label(key);
            k.setStyle(
                "-fx-font-family: '" + FONT + "';" +
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #7ab8cc;");
            Label v = new Label(val);
            v.setStyle(
                "-fx-font-family: '" + FONT + "';" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #e8f9ff;");
            HBox row = new HBox(k, v);
            row.setSpacing(6);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(k, javafx.scene.layout.Priority.NEVER);
            // push value to right side
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().add(1, spacer); // insert spacer between key and val
            return row;
        };

        // Build status string
        boolean confused = m.isConfused();
        StringBuilder statusSb = new StringBuilder();
        if (!m.isShielded() && !m.isFrozen() && !confused) statusSb.append("NORMAL");
        if (m.isShielded()) statusSb.append("SHIELD ");
        if (m.isFrozen())   statusSb.append("FROZEN ");
        if (confused)       statusSb.append("CONFUSED(").append(m.getConfusionTurns()).append("T)");

        // Role value — magenta if confused
        String roleVal = m.getRole().toString();
        Label roleLbl = new Label(roleVal);
        roleLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + (confused ? "#ff00ff" : "#e8f9ff") + ";");

        VBox statsBox = new VBox(6);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setMaxWidth(220);
        statsBox.getChildren().addAll(
            statRow.apply("TYPE",      m.getClass().getSimpleName()),
            statRow.apply("ROLE",      roleVal),       // will be tinted below if confused
            statRow.apply("ORIG ROLE", m.getOriginalRole().toString()),
            statRow.apply("ENERGY",    String.valueOf(m.getEnergy())),
            statRow.apply("POSITION",  String.valueOf(m.getPosition())),
            statRow.apply("STATUS",    statusSb.toString())
        );

        // If confused, override the role row's value colour
        if (confused) {
            HBox roleRow = (HBox) statsBox.getChildren().get(1);
            // last child of the HBox is the value label
            Node valNode = roleRow.getChildren().get(roleRow.getChildren().size() - 1);
            if (valNode instanceof Label)
                ((Label) valNode).setStyle(
                    "-fx-font-family: '" + FONT + "';" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #ff00ff;");
        }

        // ── Description label ────────────────────────────────────────────
        Label descLbl = new Label(m.getDescription());
        descLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: 10px;" +
            "-fx-text-fill: #7ab8cc;" +
            "-fx-font-style: italic;");
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(220);
        descLbl.setAlignment(Pos.CENTER);

        // ── Buttons ──────────────────────────────────────────────────────
        Button auraBtn = new Button("SHOW ENERGY AURA");
        auraBtn.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: rgba(0,60,30,0.85);" +
            "-fx-text-fill: #00ff88;" +
            "-fx-border-color: #00ff88;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 6 16;");

        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: rgba(10,30,50,0.90);" +
            "-fx-text-fill: " + accentHex + ";" +
            "-fx-border-color: " + accentHex + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 6 28;");

        HBox btnRow = new HBox(12, auraBtn, closeBtn);
        btnRow.setAlignment(Pos.CENTER);

        // ── Hint ─────────────────────────────────────────────────────────
        Label hint = new Label("tap outside to dismiss");
        hint.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: 9px;" +
            "-fx-text-fill: #445566;" +
            "-fx-font-style: italic;");

        // ── Card body ────────────────────────────────────────────────────
        VBox card = new VBox(10,
            portrait,
            nameLbl,
            divider,
            statsBox,
            descLbl,
            btnRow,
            hint);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20, 24, 18, 24));
        card.setMaxWidth(270);

        // Glassy dark panel with role-coloured border
        card.setStyle(
            "-fx-background-color: " +
                "linear-gradient(to bottom, rgba(5,18,35,0.97), rgba(2,10,22,0.99));" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + accentHex + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 16;");

        DropShadow cardShadow = new DropShadow(40, accentColor);
        cardShadow.setSpread(0.08);
        card.setEffect(cardShadow);

        // ── Full-screen dim layer (lives on backgroundRoot, above masterLayout) ──
        StackPane dimLayer = new StackPane(card);
        dimLayer.setStyle("-fx-background-color: rgba(0,0,0,0);"); // colour animated below
        dimLayer.setPickOnBounds(true);
        dimLayer.setOpacity(0);

        // Close on clicking the dim background
        dimLayer.setOnMouseClicked(e -> {
            if (e.getTarget() == dimLayer) dismissMonsterOverlay(dimLayer);
        });
        card.setOnMouseClicked(javafx.event.Event::consume);

        closeBtn.setOnAction(e -> dismissMonsterOverlay(dimLayer));

        auraBtn.setOnAction(e -> {
            int energy  = m.getEnergy();
            boolean pos = energy > 0;
            animateStationedMonsterPopups(pos, Math.max(1, Math.abs(energy)), m.getRole());
        });

        // Hover glow on buttons
        for (Button btn : new Button[]{closeBtn, auraBtn}) {
            btn.setOnMouseEntered(ev -> btn.setOpacity(0.80));
            btn.setOnMouseExited(ev  -> btn.setOpacity(1.00));
        }

        // ── Add to scene on top of everything ────────────────────────────
        backgroundRoot.getChildren().add(dimLayer);
        dimLayer.toFront();

        // ── Blur the game world (same BoxBlur used for cards) ────────────
        masterLayout.setEffect(worldBlur);
        Timeline blurIn = new Timeline(
            new KeyFrame(Duration.ZERO,
                ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0); }),
            new KeyFrame(Duration.millis(380),
                ev -> { worldBlur.setWidth(18); worldBlur.setHeight(18); }));

        // Dim the background simultaneously
        Timeline dimIn = new Timeline(
            new KeyFrame(Duration.ZERO,
                ev -> dimLayer.setStyle("-fx-background-color: rgba(0,0,0,0);")),
            new KeyFrame(Duration.millis(380),
                ev -> dimLayer.setStyle("-fx-background-color: rgba(0,0,0,0.55);")));

        // Card + dim layer fade in
        FadeTransition panelFade = new FadeTransition(Duration.millis(320), dimLayer);
        panelFade.setFromValue(0); panelFade.setToValue(1);

        // Card scales in from 0.85 → 1.0
        card.setScaleX(0.85); card.setScaleY(0.85);
        ScaleTransition cardScale = new ScaleTransition(Duration.millis(320), card);
        cardScale.setFromX(0.85); cardScale.setFromY(0.85);
        cardScale.setToX(1.0);   cardScale.setToY(1.0);
        cardScale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        new ParallelTransition(blurIn, dimIn, panelFade, cardScale).play();
    }

    /** Reverses the blur + fade for the monster info overlay. */
    private void dismissMonsterOverlay(StackPane dimLayer) {
        Timeline blurOut = new Timeline(
            new KeyFrame(Duration.ZERO,
                ev -> { worldBlur.setWidth(18); worldBlur.setHeight(18); }),
            new KeyFrame(Duration.millis(280),
                ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0);
                        masterLayout.setEffect(null); }));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(240), dimLayer);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(ev -> {
            backgroundRoot.getChildren().remove(dimLayer);
            monsterOverlayVisible = false;
        });

        new ParallelTransition(blurOut, fadeOut).play();
    }
    // =========================================================

    /**
     * Fires the floating ⚡ popup on every MonsterCell whose stationed
     * monster matches {@code targetRole}.  Mirrors the logic from the
     * original {@code animateStationedMonsterPopups} method.
     *
     * @param isIncrease true  → green "+N⚡", false → red "-N⚡"
     * @param amount     absolute energy amount to display
     * @param targetRole only cells whose monster has this role are affected
     */
    private void animateStationedMonsterPopups(boolean isIncrease, int amount,
                                               Role targetRole) {
        if (game == null) return;
        Cell[][] boardCells = game.getBoard().getBoardCells();

        for (int index = 0; index < 100; index++) {
            int bRow = index / Constants.BOARD_COLS;
            int bCol = index % Constants.BOARD_COLS;
            if (bRow % 2 == 1) bCol = Constants.BOARD_COLS - 1 - bCol;

            Cell cell = boardCells[bRow][bCol];
            if (!(cell instanceof MonsterCell)) continue;

            Monster cellMonster = ((MonsterCell) cell).getCellMonster();
            if (cellMonster == null || cellMonster.getRole() != targetRole) continue;

            int[] visualRC  = toRowCol(index);
            int   visualRow = visualRC[0];
            int   visualCol = visualRC[1];

            // Locate the StackPane for this grid coordinate
            for (Node child : grid.getChildren()) {
                Integer cIdx = GridPane.getColumnIndex(child);
                Integer rIdx = GridPane.getRowIndex(child);
                int colCoord = (cIdx == null) ? 0 : cIdx;
                int rowCoord = (rIdx == null) ? 0 : rIdx;
                if (colCoord == visualCol && rowCoord == visualRow
                        && child instanceof StackPane) {
                    createFloatingPopup((StackPane) child, isIncrease, amount);
                    break;
                }
            }
        }
    }

    /**
     * Creates, styles, positions and animates a single floating energy label
     * inside the given cell's StackPane, then removes it when done.
     *
     * @param cellPane  the StackPane that represents the board cell
     * @param isIncrease true → green gain label, false → red loss label
     * @param amount    absolute value to show next to the ⚡ symbol
     */
    private void createFloatingPopup(StackPane cellPane, boolean isIncrease, int amount) {
        // amount == 0 means a "warp flash" — show a neutral star indicator
        String text  = (amount == 0) ? "★ WARP"
                     : isIncrease   ? "+" + amount + "⚡"
                                    : "-" + amount + "⚡";
        String color = (amount == 0) ? "#ffcc00"
                     : isIncrease   ? "#00FF00"
                                    : "#FF3333";

        Label popupLabel = new Label(text);
        popupLabel.setStyle(
            "-fx-font-family: 'Arial';" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 3, 0, 0, 0);" +
            "-fx-padding: 2 0 0 4;");
        popupLabel.setMouseTransparent(true);
        StackPane.setAlignment(popupLabel, Pos.TOP_LEFT);

        Platform.runLater(() -> {
            cellPane.getChildren().add(popupLabel);

            TranslateTransition moveUp = new TranslateTransition(Duration.millis(1200), popupLabel);
            moveUp.setFromY(0);
            moveUp.setToY(-15);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(1200), popupLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ParallelTransition sequence = new ParallelTransition(moveUp, fadeOut);
            sequence.setOnFinished(ev -> cellPane.getChildren().remove(popupLabel));
            sequence.play();
        });
    }

    // =========================================================
    //  BUILD PLAYER PANEL
    // =========================================================
    private void buildPlayerPanel() {
        playerPanelContainer.setAlignment(Pos.TOP_CENTER);
        playerPanelContainer.setSpacing(4);

        pTurnOff = makeLight(IMG_TURN_OFF);   pTurnOn = makeLight(IMG_TURN_ON);
        pConfOff = makeLight(IMG_CONF_OFF);   pConfOn = makeLight(IMG_CONF_ON);
        pFrzOff  = makeLight(IMG_FREEZE_OFF); pFrzOn  = makeLight(IMG_FREEZE_ON);
        pShldOff = makeLight(IMG_SHIELD_OFF); pShldOn = makeLight(IMG_SHIELD_ON);
        pPwrOff  = makeLight(IMG_POWER_OFF);  pPwrOn  = makeLight(IMG_POWER_ON);

        HBox lights = makeLightRow(
            pTurnOff, pTurnOn, pConfOff, pConfOn,
            pFrzOff, pFrzOn, pShldOff, pShldOn, pPwrOff, pPwrOn);

        playerPortrait.setPreserveRatio(true);
        addDropShadow(playerPortrait, 12, Color.BLACK);

        playerPosLbl = makeLbl("0", "#00ff88", TXT_PLAYER_POS, true);
        StackPane portraitPane = new StackPane(playerPortrait, playerPosLbl);
        StackPane.setAlignment(playerPosLbl, Pos.BOTTOM_LEFT);
        StackPane.setMargin(playerPosLbl, new Insets(0, 0, 6, 6));

        playerProfileBg = new ImageView(img(IMG_PROFILE));
        playerProfileBg.setPreserveRatio(true);

        playerNameLbl = makeLbl("-", "white", TXT_PLAYER_NAME, true);
        playerNameLbl.setTranslateX(20);
        playerNameLbl.setTranslateY(100);

        playerTypeLbl = makeLbl("Type: -", "#aaaaaa", TXT_PLAYER_TYPE, false);
        playerTypeLbl.setTranslateX(150);
        playerTypeLbl.setTranslateY(113);

        playerRoleLbl = makeLbl("Role: -", "white", TXT_PLAYER_ROLE, false);
        playerRoleLbl.setTranslateX(150);
        playerRoleLbl.setTranslateY(122);

        VBox profileText = new VBox(2, playerNameLbl, playerTypeLbl, playerRoleLbl);
        profileText.setPadding(new Insets(10, 6, 6, 10));
        profileText.setAlignment(Pos.TOP_LEFT);
        StackPane profilePane = new StackPane(playerProfileBg, profileText);
        StackPane.setAlignment(profileText, Pos.TOP_LEFT);

        playerEnergyLbl = makeLbl("-", "#00ff88", TXT_PLAYER_ENERGY, true);
        addGlow(playerEnergyLbl, Color.web("#00ff88"), 16, 0.6);
        playerEnergyLbl.setTranslateX(10);
        playerEnergyLbl.setTranslateY(0);

        playerEnergyBar.setPreserveRatio(true);
        playerEnergyBar.setImage(en100);
        playerEnergyBarCurrentImage = en100;
        playerEnergyWrapper = new StackPane(playerEnergyBar);
        playerEnergyWrapper.setAlignment(Pos.CENTER);
        playerEnergyWrapper.setTranslateX(10);
        playerEnergyWrapper.setTranslateY(0);

        playerStatusLbl = makeLbl("NORMAL", "#aaaaaa", TXT_PLAYER_STATUS, false);
        playerStatusLbl.setTranslateX(30);
        playerStatusLbl.setTranslateY(-320);

        playerTurnLbl = makeLbl("", "#ffcc00", TXT_PLAYER_TURN, true);
        playerTurnLbl.setTranslateX(10);
        playerTurnLbl.setTranslateY(0);

        playerPanelContainer.getChildren().addAll(
            lights, portraitPane, profilePane,
            playerEnergyLbl, playerEnergyWrapper,
            playerStatusLbl, playerTurnLbl);
    }

    // =========================================================
    //  BUILD OPPONENT PANEL
    // =========================================================
    private void buildOpponentPanel() {
        opponentPanelContainer.setAlignment(Pos.TOP_CENTER);
        opponentPanelContainer.setSpacing(4);

        oTurnOff = makeLight(IMG_TURN_OFF);   oTurnOn = makeLight(IMG_TURN_ON);
        oConfOff = makeLight(IMG_CONF_OFF);   oConfOn = makeLight(IMG_CONF_ON);
        oFrzOff  = makeLight(IMG_FREEZE_OFF); oFrzOn  = makeLight(IMG_FREEZE_ON);
        oShldOff = makeLight(IMG_SHIELD_OFF); oShldOn = makeLight(IMG_SHIELD_ON);
        oPwrOff  = makeLight(IMG_POWER_OFF);  oPwrOn  = makeLight(IMG_POWER_ON);

        HBox lights = makeLightRow(
            oTurnOff, oTurnOn, oConfOff, oConfOn,
            oFrzOff, oFrzOn, oShldOff, oShldOn, oPwrOff, oPwrOn);

        opponentPortrait.setPreserveRatio(true);
        addDropShadow(opponentPortrait, 12, Color.BLACK);

        opponentPosLbl = makeLbl("0", "#ff6666", TXT_PLAYER_POS, true);
        StackPane oppPortraitPane = new StackPane(opponentPortrait, opponentPosLbl);
        StackPane.setAlignment(opponentPosLbl, Pos.BOTTOM_LEFT);
        StackPane.setMargin(opponentPosLbl, new Insets(0, 0, 6, 6));

        opponentProfileBg = new ImageView(img(IMG_PROFILE));
        opponentProfileBg.setPreserveRatio(true);

        opponentNameLbl = makeLbl("-", "white", TXT_PLAYER_NAME, true);
        opponentNameLbl.setTranslateX(30);
        opponentNameLbl.setTranslateY(100);

        opponentTypeLbl = makeLbl("Type: -", "#aaaaaa", TXT_PLAYER_TYPE, false);
        opponentTypeLbl.setTranslateX(160);
        opponentTypeLbl.setTranslateY(113);

        opponentRoleLbl = makeLbl("Role: -", "white", TXT_PLAYER_ROLE, false);
        opponentRoleLbl.setTranslateX(160);
        opponentRoleLbl.setTranslateY(122);

        VBox oppProfileText = new VBox(2, opponentNameLbl, opponentTypeLbl, opponentRoleLbl);
        oppProfileText.setPadding(new Insets(10, 6, 6, 10));
        oppProfileText.setAlignment(Pos.TOP_LEFT);
        StackPane oppProfilePane = new StackPane(opponentProfileBg, oppProfileText);
        StackPane.setAlignment(oppProfileText, Pos.TOP_LEFT);

        opponentEnergyLbl = makeLbl("-", "#ff6666", TXT_PLAYER_ENERGY, true);
        addGlow(opponentEnergyLbl, Color.web("#ff6666"), 16, 0.6);
        opponentEnergyLbl.setTranslateX(10);
        opponentEnergyLbl.setTranslateY(0);

        opponentEnergyBar.setPreserveRatio(true);
        opponentEnergyBar.setImage(en100);
        opponentEnergyBarCurrentImage = en100;
        opponentEnergyWrapper = new StackPane(opponentEnergyBar);
        opponentEnergyWrapper.setAlignment(Pos.CENTER);
        opponentEnergyWrapper.setTranslateX(0);
        opponentEnergyWrapper.setTranslateY(-30);

        opponentStatusLbl = makeLbl("NORMAL", "#aaaaaa", TXT_PLAYER_STATUS, false);
        opponentStatusLbl.setTranslateX(30);
        opponentStatusLbl.setTranslateY(-320);

        opponentPanelContainer.getChildren().addAll(
            lights, oppPortraitPane, oppProfilePane,
            opponentEnergyLbl, opponentEnergyWrapper,
            opponentStatusLbl);
    }

    // =========================================================
    //  BUILD ACTION LOG
    // =========================================================
    private void buildActionLog() {
        actionLogBg = new ImageView(img(IMG_ACTION_LOG));
        actionLogBg.setPreserveRatio(true);

        actionLine1 = makeLbl("", "white",   TXT_ACTION_LOG, false);
        actionLine2 = makeLbl("", "#aaffaa", TXT_ACTION_LOG, false);
        actionLine3 = makeLbl("", "#aaaaff", TXT_ACTION_LOG, false);
        for (Label l : new Label[]{actionLine1, actionLine2, actionLine3})
            l.setWrapText(true);

        VBox logText = new VBox(2, actionLine1, actionLine2, actionLine3);
        logText.setAlignment(Pos.TOP_LEFT);
        logText.setPadding(new Insets(26, 8, 6, 10));

        StackPane logPane = new StackPane(actionLogBg, logText);
        StackPane.setAlignment(logText, Pos.TOP_LEFT);

        actionLogContainer.getChildren().add(logPane);
        actionLogContainer.setStyle("-fx-padding: 4;");
    }

    // =========================================================
    //  BUILD CARD OVERLAY
    // =========================================================
    private void buildCardOverlay() {
        cardOverlay = new VBox(14);
        cardOverlay.setAlignment(Pos.CENTER);
        cardOverlay.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        cardOverlay.setVisible(false);
        cardOverlay.setOpacity(0);

        cardOverlayBack = new ImageView(cardBack);
        cardOverlayBack.setPreserveRatio(true);
        addDropShadow(cardOverlayBack, 22, Color.BLACK);

        cardOverlayFace = new ImageView();
        cardOverlayFace.setPreserveRatio(true);
        cardOverlayFace.setOpacity(0);
        addDropShadow(cardOverlayFace, 22, Color.BLACK);

        StackPane cardStack = new StackPane(cardOverlayBack, cardOverlayFace);

        cardOverlayName   = makeLbl("", "#ffcc00", TXT_CARD_NAME, true);
        cardOverlayDesc   = makeLbl("", "white",   TXT_CARD_BODY, false);
        cardOverlayEffect = makeLbl("", "#00ffff", TXT_CARD_BODY, true);
        for (Label l : new Label[]{cardOverlayName, cardOverlayDesc, cardOverlayEffect}) {
            l.setWrapText(true);
            l.setAlignment(Pos.CENTER);
        }

        Label hint = makeLbl("tap to continue", "#888888", 10, false);
        hint.setStyle(hint.getStyle() + "-fx-font-style: italic;");

        cardOverlay.getChildren().addAll(
            cardStack, cardOverlayName, cardOverlayDesc, cardOverlayEffect, hint);
        cardOverlay.setOnMouseClicked(e -> dismissCardOverlay());

        backgroundRoot.getChildren().add(cardOverlay);
        StackPane.setAlignment(cardOverlay, Pos.CENTER);
    }

    // =========================================================
    //  BUILD MESSAGE OVERLAY
    // =========================================================
    private void buildMessageOverlay() {
        messageOverlay = new StackPane();
        messageOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        messageOverlay.setVisible(false);
        messageOverlay.setOpacity(0);
        messageOverlay.setPickOnBounds(true);

        messageBox = new VBox(14);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(22, 28, 22, 28));
        messageBox.setMaxWidth(440);
        messageBox.setStyle(
            "-fx-background-color: rgba(18,18,32,0.97);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #ff5555;" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 2;");

        messageTitle = makeLbl("", "#ff6666", 15, true);
        messageBody  = makeLbl("", "white", 12, false);
        messageBody.setWrapText(true);
        messageBody.setMaxWidth(380);
        messageBody.setAlignment(Pos.CENTER);

        messageButtons = new HBox(14);
        messageButtons.setAlignment(Pos.CENTER);

        messageBox.getChildren().addAll(messageTitle, messageBody, messageButtons);
        messageOverlay.getChildren().add(messageBox);
        StackPane.setAlignment(messageBox, Pos.CENTER);

        backgroundRoot.getChildren().add(messageOverlay);
        messageOverlay.toFront();
    }

    private void showMessageOverlay(String title, String body,
                                    Runnable onConfirm, boolean confirm) {
        if (messageOverlay == null) return;

        messageTitle.setText(title != null ? title : "");
        messageBody.setText(body != null ? body : "");
        messageButtons.getChildren().clear();

        if (confirm) {
            messageBox.setStyle(
                "-fx-background-color: rgba(18,18,32,0.97);" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #00ccff;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 2;");
            messageTitle.setStyle(
                "-fx-font-family: " + FONT + ";" +
                "-fx-text-fill: #00ccff;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;");

            Button ok = new Button("OK");
            styleOverlayButton(ok, "#00cc88");
            ok.setOnAction(e -> {
                hideMessageOverlay();
                if (onConfirm != null) onConfirm.run();
            });

            Button cancel = new Button("CANCEL");
            styleOverlayButton(cancel, "#666666");
            cancel.setOnAction(e -> hideMessageOverlay());

            messageButtons.getChildren().addAll(ok, cancel);
        } else {
            messageBox.setStyle(
                "-fx-background-color: rgba(18,18,32,0.97);" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #ff5555;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 2;");
            messageTitle.setStyle(
                "-fx-font-family: " + FONT + ";" +
                "-fx-text-fill: #ff6666;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;");

            Button ok = new Button("OK");
            styleOverlayButton(ok, "#ff5555");
            ok.setOnAction(e -> hideMessageOverlay());
            messageButtons.getChildren().add(ok);
        }

        messageOverlay.setVisible(true);
        messageOverlay.toFront();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), messageOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void styleOverlayButton(Button btn, String color) {
        btn.setStyle(
            "-fx-font-family: " + FONT + ";" +
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 20;");
    }

    private void hideMessageOverlay() {
        if (messageOverlay == null) return;
        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), messageOverlay);
        fadeOut.setFromValue(messageOverlay.getOpacity());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            messageOverlay.setVisible(false);
            messageButtons.getChildren().clear();
        });
        fadeOut.play();
    }

    // =========================================================
    //  RESPONSIVE LAYOUT
    // =========================================================
    private void setupResponsiveLayout() {
        masterLayout.prefWidthProperty().bind(backgroundRoot.widthProperty());
        masterLayout.prefHeightProperty().bind(backgroundRoot.heightProperty());
        masterLayout.maxWidthProperty().bind(backgroundRoot.widthProperty());
        masterLayout.maxHeightProperty().bind(backgroundRoot.heightProperty());
        controlBar.prefWidthProperty().bind(backgroundRoot.widthProperty());

        javafx.beans.value.ChangeListener<Number> onResize =
            (obs, old, val) -> applyAllLayout();
        backgroundRoot.widthProperty().addListener(onResize);
        backgroundRoot.heightProperty().addListener(onResize);
    }

    private void applyAllLayout() {
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W == 0 || H == 0) return;

        double barH = H * CONTROL_BAR_H;
        controlBar.setPrefHeight(barH);
        BorderPane.setMargin(boardContainer, new Insets(6, 6, barH + 6, 6));

        double panelW = W * SIDE_PANEL_W;
        playerPanelContainer.setPrefWidth(panelW);
        if (masterLayout.getRight() != null)
            ((VBox) masterLayout.getRight()).setPrefWidth(panelW);

        double pad = H * PANEL_TOP_PAD;
        playerPanelContainer.setStyle("-fx-padding: " + (pad + 170) + " 4 4 60;");
        opponentPanelContainer.setStyle("-fx-padding: " + (pad + 170) + " 4 4 4;");

        double portraitW = panelW * PORTRAIT_W_MULT;
        playerPortrait.setFitWidth(portraitW);
        opponentPortrait.setFitWidth(portraitW);

        double energyW = panelW * ENERGY_BAR_W_MULT;
        playerEnergyBar.setFitWidth(energyW);
        opponentEnergyBar.setFitWidth(energyW);

        double profileW = panelW * PROFILE_W_MULT;
        if (playerProfileBg   != null) playerProfileBg.setFitWidth(profileW);
        if (opponentProfileBg != null) opponentProfileBg.setFitWidth(profileW);

        double logW = panelW * ACTION_LOG_W_MULT;
        if (actionLogBg != null) actionLogBg.setFitWidth(logW);
        for (Label l : new Label[]{actionLine1, actionLine2, actionLine3})
            if (l != null) l.setMaxWidth(logW - 18);

        double lightSz = panelW * LIGHT_SIZE_MULT;
        for (ImageView iv : new ImageView[]{
                pTurnOff, pTurnOn, pConfOff, pConfOn,
                pFrzOff,  pFrzOn,  pShldOff, pShldOn, pPwrOff, pPwrOn,
                oTurnOff, oTurnOn, oConfOff, oConfOn,
                oFrzOff,  oFrzOn,  oShldOff, oShldOn, oPwrOff, oPwrOn}) {
            if (iv != null) iv.setFitWidth(lightSz);
        }

        double centerW   = Math.max(1, W - panelW * 2);
        double boardSide = Math.min(H * BOARD_SIZE_MULT, centerW * 0.92);
        double cardW     = boardSide * CARD_W_MULT;
        if (cardOverlayBack != null) cardOverlayBack.setFitWidth(cardW);
        if (cardOverlayFace != null) cardOverlayFace.setFitWidth(cardW);
        if (cardOverlay     != null) cardOverlay.setMaxWidth(cardW + 60);

        myLabel.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + Math.max(TXT_TOP_LABEL, H * 0.018) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 0 3 0;" +
            "-fx-text-fill: #00ff88;");

        cardDeckView.setFitWidth(W * DECK_W);
        cardDeckView.setFitHeight(H * DECK_H);
        AnchorPane.setLeftAnchor(cardDeckView,   W * DECK_LEFT);
        AnchorPane.setTopAnchor(cardDeckView,    barH * DECK_TOP_FRAC);
        AnchorPane.setRightAnchor(cardDeckView,  null);
        AnchorPane.setBottomAnchor(cardDeckView, null);

        double diceSize = H * DICE_SIZE;
        diceView.setFitWidth(diceSize);
        diceView.setFitHeight(diceSize);
        AnchorPane.setLeftAnchor(diceView,   (W / 2) - (diceSize / 2) + 6);
        AnchorPane.setTopAnchor(diceView,    barH * DICE_TOP_FRAC);
        AnchorPane.setRightAnchor(diceView,  null);
        AnchorPane.setBottomAnchor(diceView, null);

        double labelX = (W / 2) - Math.max(40, W * 0.04);
        AnchorPane.setLeftAnchor(diceResultLabel,   labelX);
        AnchorPane.setBottomAnchor(diceResultLabel, barH * 0.04);
        AnchorPane.setRightAnchor(diceResultLabel,  null);
        AnchorPane.setTopAnchor(diceResultLabel,    null);
        diceResultLabel.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + Math.max(TXT_DICE_RESULT, H * 0.02) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #00ff88;");

        double btnW = W * BTN_W;
        double btnH = H * BTN_H;
        powerUpImageBtn.setFitWidth(btnW);
        powerUpImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(powerUpImageBtn,  W * POWERUP_RIGHT);
        AnchorPane.setTopAnchor(powerUpImageBtn,    barH * BTN_TOP_FRAC);
        AnchorPane.setLeftAnchor(powerUpImageBtn,   null);
        AnchorPane.setBottomAnchor(powerUpImageBtn, null);

        rollImageBtn.setFitWidth(btnW);
        rollImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(rollImageBtn,  W * ROLL_RIGHT);
        AnchorPane.setTopAnchor(rollImageBtn,    barH * BTN_TOP_FRAC);
        AnchorPane.setLeftAnchor(rollImageBtn,   null);
        AnchorPane.setBottomAnchor(rollImageBtn, null);

        if (actionLogContainer != null && actionLogBg != null) {
            actionLogContainer.setTranslateX(20);
            actionLogContainer.setTranslateY(H - 200);
        }
    }

    // =========================================================
    //  PARALLAX
    // =========================================================
    private void setupParallax() {
        parallaxTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double cx = backgroundView.getTranslateX();
                double cy = backgroundView.getTranslateY();
                backgroundView.setTranslateX(cx + (targetX - cx) * 0.08);
                backgroundView.setTranslateY(cy + (targetY - cy) * 0.08);
            }
        };
        parallaxTimer.start();

        backgroundRoot.setOnMouseMoved(e -> {
            double w = backgroundRoot.getWidth();
            double h = backgroundRoot.getHeight();
            targetX = ((e.getX() / w) - 0.5) * -PARALLAX_X;
            targetY = ((e.getY() / h) - 0.5) * -PARALLAX_Y;
        });
    }

    public void stopParallax() {
        if (parallaxTimer != null) parallaxTimer.stop();
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

    private void refreshBoard() {
        if (game == null) return;
        Cell[][] cells = game.getBoard().getBoardCells();

        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setImage(null);
                energyLabels[r][c].setVisible(false);
                energyLabels[r][c].setUserData(null);
            }

        for (int index = 0; index < 100; index++) {
            int bRow = index / Constants.BOARD_COLS;
            int bCol = index % Constants.BOARD_COLS;
            if (bRow % 2 == 1) bCol = Constants.BOARD_COLS - 1 - bCol;
            Cell cell = cells[bRow][bCol];
            int[] rc  = toRowCol(index);
            if (cell != null) {
                setCellImage(rc[0], rc[1], cell);
                if (cell instanceof DoorCell) {
                    DoorCell door = (DoorCell) cell;
                    if (!door.isActivated()) {
                        Label lbl = energyLabels[rc[0]][rc[1]];
                        lbl.setUserData("door");
                        lbl.setText("!" + door.getEnergy());
                        applyDoorEnergyLabelStyle(lbl, cellSize.get());
                        lbl.setVisible(true);
                    }
                } else if (cell instanceof MonsterCell) {
                    MonsterCell mc = (MonsterCell) cell;
                    if (mc.getCellMonster() != null) {
                        Label lbl = energyLabels[rc[0]][rc[1]];
                        lbl.setUserData("monster");
                        lbl.setText(String.valueOf(mc.getCellMonster().getEnergy()));
                        applyMonsterEnergyLabelStyle(lbl, cellSize.get());
                        lbl.setVisible(true);
                    }
                }
            } else {
                bgViews[rc[0]][rc[1]].setImage(normalImage);
            }
        }

        for (Monster m : Board.getStationedMonsters()) {
            int[] rc = toRowCol(m.getPosition());
            monsterViews[rc[0]][rc[1]].setImage(monsterSprite(m.getName()));
        }
        drawMonster(game.getPlayer());
        drawMonster(game.getOpponent());
        updateDeck();
    }

    private void drawMonster(Monster m) {
        if (m == null) return;
        int[] rc = toRowCol(m.getPosition());
        monsterViews[rc[0]][rc[1]].setImage(monsterSprite(m.getName()));
    }

    private int[] toRowCol(int index) {
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        row = Constants.BOARD_ROWS - 1 - row;
        return new int[]{row, col};
    }

    private void setCellImage(int row, int col, Cell cell) {
        if (cell instanceof MonsterCell)
            bgViews[row][col].setImage(monsterCellImage);
        else if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            bgViews[row][col].setImage(d.isActivated()
                ? (d.getRole() == Role.SCARER ? doorSO : doorLO)
                : (d.getRole() == Role.SCARER ? doorSC : doorLC));
        } else if (cell instanceof ConveyorBelt)
            bgViews[row][col].setImage(conveyorImage);
        else if (cell instanceof ContaminationSock)
            bgViews[row][col].setImage(sockImage);
        else if (cell instanceof CardCell)
            bgViews[row][col].setImage(cardCellImage);
        else
            bgViews[row][col].setImage(normalImage);
    }

    private void updateDeck() {
        if (cardDeckView == null) return;
        int remaining = Board.cards.size();
        int total     = Board.getOriginalCards().size();
        if (total == 0) return;
        double ratio = (double) remaining / total;
        cardDeckView.setImage(ratio > 0.60 ? deckFull
            : ratio > 0.25 ? deckMid : deckLeast);
    }

    // =========================================================
    //  UPDATE UI
    // =========================================================
    private void updateUI() {
        if (game == null) return;
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        Monster current  = game.getCurrent();

        playerPortrait.setImage(screenPortrait(player.getName()));
        playerNameLbl.setText(player.getName());
        playerTypeLbl.setText(player.getClass().getSimpleName());
        playerPosLbl.setText(String.valueOf(player.getPosition()));
        playerEnergyLbl.setText(String.valueOf(player.getEnergy()));
        boolean pConf = !player.getOriginalRole().equals(player.getRole());
        playerRoleLbl.setText(player.getRole().toString());
        playerRoleLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + TXT_PLAYER_ROLE + "px;" +
            "-fx-text-fill: " + (pConf ? "#ff00ff" : "white") + ";" +
            (pConf ? "-fx-font-weight: bold;" : ""));
        playerStatusLbl.setText(statusString(player));
        playerTurnLbl.setText(current == player ? "YOUR TURN" : "");
        animateEnergyBar(playerEnergyBar, playerEnergyWrapper,
            player.getEnergy(), true);

        opponentPortrait.setImage(screenPortrait(opponent.getName()));
        opponentNameLbl.setText(opponent.getName());
        opponentTypeLbl.setText(opponent.getClass().getSimpleName());
        opponentPosLbl.setText(String.valueOf(opponent.getPosition()));
        opponentEnergyLbl.setText(String.valueOf(opponent.getEnergy()));
        boolean oConf = !opponent.getOriginalRole().equals(opponent.getRole());
        opponentRoleLbl.setText(opponent.getRole().toString());
        opponentRoleLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + TXT_PLAYER_ROLE + "px;" +
            "-fx-text-fill: " + (oConf ? "#ff00ff" : "white") + ";" +
            (oConf ? "-fx-font-weight: bold;" : ""));
        opponentStatusLbl.setText(statusString(opponent));
        animateEnergyBar(opponentEnergyBar, opponentEnergyWrapper,
            opponent.getEnergy(), false);

        myLabel.setText(current == player
            ? "YOUR TURN - PRESS ROLL!" : "OPPONENT TURN - PRESS ROLL!");
        myLabel.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + TXT_TOP_LABEL + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 0 3 0;" +
            "-fx-text-fill: " + (current == player ? "#00ff88" : "#ff6666") + ";");

        if (playerPowerTurnsLeft   > 0) playerPowerTurnsLeft--;
        if (opponentPowerTurnsLeft > 0) opponentPowerTurnsLeft--;

        setLight(pTurnOff, pTurnOn, current == player,         pTurnState, v -> pTurnState = v);
        setLight(pConfOff, pConfOn, player.isConfused(),        pConfState, v -> pConfState = v);
        setLight(pFrzOff,  pFrzOn,  player.isFrozen(),          pFrzState,  v -> pFrzState  = v);
        setLight(pShldOff, pShldOn, player.isShielded(),        pShldState, v -> pShldState = v);
        setLight(pPwrOff,  pPwrOn,  playerPowerTurnsLeft > 0,  pPwrState,  v -> pPwrState  = v);

        setLight(oTurnOff, oTurnOn, current == opponent,        oTurnState, v -> oTurnState = v);
        setLight(oConfOff, oConfOn, opponent.isConfused(),       oConfState, v -> oConfState = v);
        setLight(oFrzOff,  oFrzOn,  opponent.isFrozen(),         oFrzState,  v -> oFrzState  = v);
        setLight(oShldOff, oShldOn, opponent.isShielded(),       oShldState, v -> oShldState = v);
        setLight(oPwrOff,  oPwrOn,  opponentPowerTurnsLeft > 0, oPwrState,  v -> oPwrState  = v);
    }

    // =========================================================
    //  ENERGY BAR ANIMATION
    // =========================================================
    private void animateEnergyBar(ImageView bar, StackPane wrapper,
                                   int energy, boolean isPlayer) {
        int pct = (int) Math.min(100, Math.max(0, (energy / 1000.0) * 100));
        Image target;
        if      (pct >= 75) target = en100;
        else if (pct >= 50) target = en75;
        else if (pct >= 25) target = en50;
        else if (pct > 0)   target = en25;
        else                target = en0;

        Image cur = isPlayer ? playerEnergyBarCurrentImage : opponentEnergyBarCurrentImage;
        if (cur == target) return;

        if (isPlayer) playerEnergyBarCurrentImage   = target;
        else          opponentEnergyBarCurrentImage = target;

        bar.setVisible(true);
        bar.setOpacity(1);

        ImageView overlay = new ImageView(target);
        overlay.setFitWidth(bar.getFitWidth());
        overlay.setPreserveRatio(true);
        overlay.setOpacity(0);
        wrapper.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            bar.setImage(target);
            bar.setOpacity(1);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), overlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> wrapper.getChildren().remove(overlay));
            fadeOut.play();
        });
        fadeIn.play();
    }

    // =========================================================
    //  INDICATOR LIGHT CROSS-FADE
    // =========================================================
    private void setLight(ImageView offView, ImageView onView,
                           boolean active, boolean currentState,
                           java.util.function.Consumer<Boolean> stateSetter) {
        if (active == currentState) return;
        stateSetter.accept(active);

        if (active) {
            onView.setVisible(true);
            onView.setOpacity(0);
            FadeTransition in = new FadeTransition(Duration.millis(300), onView);
            in.setFromValue(0); in.setToValue(1);
            in.setOnFinished(e -> {
                FadeTransition out = new FadeTransition(Duration.millis(200), offView);
                out.setFromValue(offView.getOpacity()); out.setToValue(0);
                out.setOnFinished(ev -> offView.setVisible(false));
                out.play();
            });
            in.play();
        } else {
            offView.setVisible(true);
            offView.setOpacity(0);
            FadeTransition in = new FadeTransition(Duration.millis(300), offView);
            in.setFromValue(0); in.setToValue(1);
            in.setOnFinished(e -> {
                FadeTransition out = new FadeTransition(Duration.millis(200), onView);
                out.setFromValue(onView.getOpacity()); out.setToValue(0);
                out.setOnFinished(ev -> onView.setVisible(false));
                out.play();
            });
            in.play();
        }
    }

    // =========================================================
    //  CARD OVERLAY
    // =========================================================
    private void showCardOverlay(Card card) {
        cardOverlayName.setText(card.getName());
        cardOverlayDesc.setText(card.getDescription());
        cardOverlayEffect.setText(cardEffect(card.getName()));
        cardOverlayFace.setImage(cardFace(card.getName()));

        cardOverlayBack.setOpacity(1);
        cardOverlayBack.setVisible(true);
        cardOverlayFace.setOpacity(0);
        cardOverlay.setVisible(true);

        masterLayout.setEffect(worldBlur);
        Timeline blurIn = new Timeline(
            new KeyFrame(Duration.millis(0),
                e -> { worldBlur.setWidth(0); worldBlur.setHeight(0); }),
            new KeyFrame(Duration.millis(400),
                e -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }));

        FadeTransition cardIn = new FadeTransition(Duration.millis(350), cardOverlay);
        cardIn.setFromValue(0); cardIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e -> {
            FadeTransition faceIn = new FadeTransition(Duration.millis(400), cardOverlayFace);
            faceIn.setFromValue(0); faceIn.setToValue(1);
            faceIn.setOnFinished(ev -> {
                FadeTransition backOut = new FadeTransition(Duration.millis(300), cardOverlayBack);
                backOut.setFromValue(1); backOut.setToValue(0);
                backOut.setOnFinished(bev -> cardOverlayBack.setVisible(false));
                backOut.play();
            });
            faceIn.play();
        });

        new SequentialTransition(blurIn, cardIn, pause).play();
    }

    private void dismissCardOverlay() {
        Timeline blurOut = new Timeline(
            new KeyFrame(Duration.millis(0),
                e -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }),
            new KeyFrame(Duration.millis(350),
                e -> { worldBlur.setWidth(0); worldBlur.setHeight(0);
                       masterLayout.setEffect(null); }));

        FadeTransition cardOut = new FadeTransition(Duration.millis(280), cardOverlay);
        cardOut.setFromValue(1); cardOut.setToValue(0);
        cardOut.setOnFinished(e -> {
            cardOverlay.setVisible(false);
            refreshBoard();
            updateUI();
        });

        new ParallelTransition(blurOut, cardOut).play();
    }

    // =========================================================
    //  ROLL HANDLER
    // =========================================================
    @FXML
    private void handleRollDice() {
        if (game == null || cardOverlay.isVisible() || isAnimating || monsterOverlayVisible) return;
        try {
            isAnimating = true;
            Monster current  = game.getCurrent();
            Monster opponent = current == game.getPlayer()
                ? game.getOpponent() : game.getPlayer();

            int  oldPos    = current.getPosition();
            int  oldEnergy = current.getEnergy();
            int  oldOppEng = opponent.getEnergy();
            boolean wasShld = current.isShielded();

            Card topCard = Board.cards.isEmpty() ? null : Board.cards.get(0);
            if (Board.cards.isEmpty()) Board.reloadCards();

            if (current.isFrozen()) {
                game.playTurn();
                actionLine1.setText(current.getName() + " FROZEN - SKIPPED!");
                actionLine2.setText(""); actionLine3.setText("");
                refreshBoard(); updateUI();
                isAnimating = false;
                return;
            }

            // Snapshot door states BEFORE the turn
            boolean[][] doorWasActivated = new boolean[10][10];
            Cell[][] cells = game.getBoard().getBoardCells();
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    if (cells[r][c] instanceof DoorCell)
                        doorWasActivated[r][c] = ((DoorCell) cells[r][c]).isActivated();

            // Snapshot stationed monsters' energies BEFORE the turn
            java.util.Map<String, Integer> stationedEnergyBefore = new java.util.HashMap<>();
            for (Monster m : Board.getStationedMonsters())
                stationedEnergyBefore.put(m.getName(), m.getEnergy());

            game.playTurn();

            int newPos    = current.getPosition();
            int newEnergy = current.getEnergy();
            int newOppEng = opponent.getEnergy();
            boolean drawn = topCard != null &&
                (Board.cards.isEmpty() || Board.cards.get(0) != topCard);

            int moved    = newPos - oldPos;
            if (moved < 0) moved += 100;
            int diceFace = Math.max(1, Math.min(6, moved));

            actionLine1.setText(current.getName() + " -> POS " + newPos + " (+" + moved + ")");
            actionLine2.setText(drawn && topCard != null
                ? topCard.getName() + ": " + cardEffect(topCard.getName()) : "");

            if (wasShld && !current.isShielded())
                actionLine3.setText("SHIELD BLOCKED LOSS!");
            else if (newEnergy != oldEnergy) {
                int diff = newEnergy - oldEnergy;
                actionLine3.setText(current.getName()
                    + (diff > 0 ? " +" : " ") + diff + " -> " + newEnergy);
            } else if (newOppEng != oldOppEng) {
                int diff = newOppEng - oldOppEng;
                actionLine3.setText(opponent.getName()
                    + (diff > 0 ? " +" : " ") + diff + " -> " + newOppEng);
            } else {
                actionLine3.setText("");
            }

            final Card    fc = topCard;
            final boolean fd = drawn;
            final Monster fm = current;
            final Monster fo = opponent;
            final int     fp = oldPos;
            final int     fn = newPos;
            final boolean[][] finalDoorWasActivated = doorWasActivated;
            final Cell[][] finalCells = cells;
            final java.util.Map<String, Integer> finalStationedBefore = stationedEnergyBefore;

            animateDice(diceFace, () ->
                animateMove(fm, fo, fp, fn, () -> {
                    refreshBoard(); updateUI();

                    // Door sound — only if door was just opened by landing on it
                    int[] rowCol = toRowCol(fn);
                    Cell landedCell = finalCells[rowCol[0]][rowCol[1]];
                    if (landedCell instanceof DoorCell) {
                        if (!finalDoorWasActivated[rowCol[0]][rowCol[1]]
                                && ((DoorCell) landedCell).isActivated()) {
                            SoundManager.getInstance().playDoorOpening();
                        }
                    }

                    // Stationed monster energy popups after landing
                    for (Monster stationed : Board.getStationedMonsters()) {
                        Integer before = finalStationedBefore.get(stationed.getName());
                        if (before != null && stationed.getEnergy() != before) {
                            int diff = stationed.getEnergy() - before;
                            int[] dst = toRowCol(stationed.getPosition());
                            // Locate the cell StackPane and fire the floating popup
                            for (Node child : grid.getChildren()) {
                                Integer cIdx = GridPane.getColumnIndex(child);
                                Integer rIdx = GridPane.getRowIndex(child);
                                int colCoord = (cIdx == null) ? 0 : cIdx;
                                int rowCoord = (rIdx == null) ? 0 : rIdx;
                                if (colCoord == dst[1] && rowCoord == dst[0]
                                        && child instanceof StackPane) {
                                    createFloatingPopup(
                                        (StackPane) child, diff >= 0, Math.abs(diff));
                                    break;
                                }
                            }
                        }
                    }

                    if (fd && fc != null) {
                        SoundManager.getInstance().playCardDraw();
                        showCardOverlay(fc);
                    }
                    checkWinner();
                    isAnimating = false;
                }));

        } catch (game.engine.exceptions.InvalidMoveException ex) {
            actionLine1.setText("INVALID: " + ex.getMessage());
            actionLine2.setText("ROLL AGAIN!"); actionLine3.setText("");
            refreshBoard(); updateUI();
            isAnimating = false;
            showErrorAlert("Invalid Move", ex.getMessage());
        } catch (Exception ex) {
            actionLine1.setText("ERROR: " + ex.getMessage());
            ex.printStackTrace();
            isAnimating = false;
        }
    }

    private Monster getMonsterAtCell(int cellIndex) {
        if (game == null) return null;
        if (game.getPlayer().getPosition() == cellIndex)   return game.getPlayer();
        if (game.getOpponent().getPosition() == cellIndex) return game.getOpponent();
        for (Monster m : Board.getStationedMonsters())
            if (m.getPosition() == cellIndex) return m;
        return null;
    }

    private void checkWinner() {
        if (game.getWinner() == null) return;
        Monster winner   = game.getWinner();
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        if (parallaxTimer != null) parallaxTimer.stop();
        myLabel.setText(winner.getName() + " WINS!");
        SceneManager.getInstance().switchToGameOverScreen(
            winner.getName(), winner.getRole().toString(), player.getRole(),
            player.getName(), player.getRole().toString(), player.getEnergy(),
            opponent.getName(), opponent.getRole().toString(), opponent.getEnergy());
    }

    // =========================================================
    //  DICE ANIMATION
    // =========================================================
    private void animateDice(int finalFace, Runnable onFinished) {
        if (diceTimeline != null) diceTimeline.stop();
        diceTimeline = new Timeline();
        java.util.Random rand = new java.util.Random();
        int[] delays = {60, 80, 100, 130, 160, 200, 250, 320, 400};
        int elapsed = 0;
        for (int d : delays) {
            elapsed += d;
            final int f = rand.nextInt(6);
            diceTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(elapsed),
                e -> diceView.setImage(diceImages[f])));
        }
        elapsed += 300;
        final int total = elapsed;
        diceTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(total), e -> {
            diceView.setImage(diceImages[finalFace - 1]);
            diceResultLabel.setText(String.valueOf(finalFace));
            if (onFinished != null) onFinished.run();
        }));
        diceTimeline.play();
    }

    // =========================================================
    //  MONSTER MOVEMENT
    // =========================================================
    private void animateMove(Monster current, Monster opponent,
                              int oldPos, int newPos, Runnable onFinished) {
        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setTranslateX(0);
                monsterViews[r][c].setTranslateY(0);
                monsterViews[r][c].setImage(null);
            }
        for (Monster m : Board.getStationedMonsters()) {
            int[] rc = toRowCol(m.getPosition());
            monsterViews[rc[0]][rc[1]].setImage(monsterSprite(m.getName()));
        }
        drawMonster(opponent);
        int[] startRC = toRowCol(oldPos);
        monsterViews[startRC[0]][startRC[1]].setImage(monsterSprite(current.getName()));

        SequentialTransition seq = new SequentialTransition();
        int dist = Math.abs(newPos - oldPos);
        if (dist > 12 || oldPos == newPos)
            seq.getChildren().add(makeHop(current, opponent, oldPos, newPos, 600));
        else {
            int step = newPos > oldPos ? 1 : -1;
            for (int pos = oldPos; pos != newPos; pos += step)
                seq.getChildren().add(makeHop(current, opponent, pos, pos + step, 250));
        }
        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }

    private Animation makeHop(Monster moving, Monster stationary,
                               int from, int to, int ms) {
        int[] fRC = toRowCol(from);
        int[] tRC = toRowCol(to);
        double cellW = grid.getWidth()  / Constants.BOARD_COLS;
        double cellH = grid.getHeight() / Constants.BOARD_ROWS;
        double dx = (tRC[1] - fRC[1]) * cellW;
        double dy = (tRC[0] - fRC[0]) * cellH;

        ImageView mv = monsterViews[fRC[0]][fRC[1]];
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), mv);
        tt.setByX(dx); tt.setByY(dy);
        tt.setOnFinished(e -> {
            mv.setTranslateX(0); mv.setTranslateY(0); mv.setImage(null);
            for (Monster s : Board.getStationedMonsters())
                if (s.getPosition() == from) {
                    mv.setImage(monsterSprite(s.getName())); break;
                }
            if (stationary.getPosition() == from && mv.getImage() == null)
                mv.setImage(monsterSprite(stationary.getName()));
            monsterViews[tRC[0]][tRC[1]].setImage(monsterSprite(moving.getName()));
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
        if (game == null || cardOverlay.isVisible() || monsterOverlayVisible) return;
        if (messageOverlay != null && messageOverlay.isVisible()) return;
        showConfirmDialog("Use Powerup", "Activate Powerup?",
            "Costs " + Constants.POWERUP_COST + " energy. Proceed?",
            () -> {
                try {
                    Monster current  = game.getCurrent();
                    boolean isPlayer = (current == game.getPlayer());
                    String name = current.getName();
                    game.usePowerup();
                    actionLine1.setText(name + " POWERUP ACTIVATED!");
                    actionLine2.setText("");
                    actionLine3.setText("");
                    int POWERUP_DURATION = 3;
                    if (isPlayer) playerPowerTurnsLeft   = POWERUP_DURATION;
                    else          opponentPowerTurnsLeft = POWERUP_DURATION;
                    refreshBoard();
                    updateUI();
                } catch (Exception ex) {
                    showErrorAlert("Powerup Failed", ex.getMessage());
                }
            });
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
            actionLine1.setText("CHEAT: WARPED!");
            actionLine2.setText(""); actionLine3.setText("");
            refreshBoard(); updateUI(); checkWinner();
            // Flash all stationed monster cells to show the board state
            animateStationedMonsterPopups(true, 0, current.getRole());
        } else if (e.getCode() == KeyCode.E) {
            int gained = 50;
            current.setEnergy(current.getEnergy() + gained);
            actionLine1.setText("CHEAT: +50 ENERGY!");
            actionLine2.setText(""); actionLine3.setText("");
            refreshBoard(); updateUI();
            // Show +50 floating popup on every MonsterCell matching the current role
            animateStationedMonsterPopups(true, gained, current.getRole());
        }
    }

    // =========================================================
    //  DIALOGS
    // =========================================================
    private void showConfirmDialog(String title, String header,
                                   String content, Runnable onConfirm) {
        String body = header;
        if (content != null && !content.isEmpty()) {
            body = (header != null && !header.isEmpty())
                ? header + "\n" + content : content;
        }
        showMessageOverlay(title, body, onConfirm, true);
    }

    private void showErrorAlert(String title, String msg) {
        showMessageOverlay(title, msg != null ? msg : "Unknown error", null, false);
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private Image monsterSprite(String name) {
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":               return mi_celia;
            case "fungus":                  return mi_fungus;
            case "henry j. waternoose":
            case "henry j. waternoose iii": return mi_henry;
            case "james p. sullivan":
            case "james sullivan":          return mi_james;
            case "mike wazowski":           return mi_mike;
            case "randall boggs":
            case "randall":                 return mi_randall;
            case "roz":                     return mi_roz;
            case "yeti":                    return mi_yeti;
            default: return null;
        }
    }

    private Image screenPortrait(String name) {
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":               return si_celia;
            case "fungus":                  return si_fungus;
            case "henry j. waternoose":
            case "henry j. waternoose iii": return si_henry;
            case "james p. sullivan":
            case "james sullivan":          return si_james;
            case "mike wazowski":           return si_mike;
            case "randall boggs":
            case "randall":                 return si_randall;
            case "roz":                     return si_roz;
            case "yeti":                    return si_yeti;
            default: return monsterSprite(name);
        }
    }

    private Image cardFace(String name) {
        switch (name) {
            case "2319 Alert":         return c2319;
            case "Contamination Code": return cContam;
            case "Mega Drain":         return cMegaDrain;
            case "Mind Scramble":      return cMind;
            case "Position Swap":      return cSwap;
            case "Small Snatcher":     return cSmall;
            case "Sneaky Thief":       return cSneaky;
            case "Super Shield":       return cShield;
            case "Total Confusion":    return cConfuse;
            default:                   return cardBack;
        }
    }

    private String cardEffect(String name) {
        switch (name) {
            case "Position Swap":      return "SWAP POSITIONS";
            case "2319 Alert":         return "OPPONENT -> START";
            case "Contamination Code": return "PLAYER -> START";
            case "Small Snatcher":     return "STEAL 50 ENERGY";
            case "Sneaky Thief":       return "STEAL 100 ENERGY";
            case "Mega Drain":         return "STEAL 150 ENERGY";
            case "Super Shield":       return "SHIELD ACTIVE";
            case "Mind Scramble":      return "CONFUSION 2 TURNS";
            case "Total Confusion":    return "CONFUSION 3 TURNS";
            default:                   return "SPECIAL EFFECT";
        }
    }

    private String statusString(Monster m) {
        StringBuilder sb = new StringBuilder();
        if (m.isShielded()) sb.append("[SHIELD] ");
        if (m.isConfused()) sb.append("[CONF:").append(m.getConfusionTurns()).append("T] ");
        if (m.isFrozen())   sb.append("[FROZEN] ");
        if (m instanceof game.engine.monsters.Dasher) {
            int mt = ((game.engine.monsters.Dasher) m).getMomentumTurns();
            if (mt > 0) sb.append("[RUSH:").append(mt).append("T] ");
        }
        if (m instanceof game.engine.monsters.MultiTasker) {
            int ft = ((game.engine.monsters.MultiTasker) m).getNormalSpeedTurns();
            if (ft > 0) sb.append("[FOCUS:").append(ft).append("T] ");
        }
        return sb.length() == 0 ? "NORMAL" : sb.toString().trim();
    }

    private Label makeLbl(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + size + "px;" +
            "-fx-text-fill: " + color + ";" +
            (bold ? "-fx-font-weight: bold;" : ""));
        l.setWrapText(true);
        l.setMaxWidth(9999);
        return l;
    }

    private ImageView makeLight(String filename) {
        ImageView iv = new ImageView(img(filename));
        iv.setPreserveRatio(true);
        iv.setFitWidth(28);
        addDropShadow(iv, 6, Color.BLACK);
        return iv;
    }

    private HBox makeLightRow(
            ImageView tOff, ImageView tOn,
            ImageView cOff, ImageView cOn,
            ImageView fOff, ImageView fOn,
            ImageView sOff, ImageView sOn,
            ImageView pOff, ImageView pOn) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
            makeStackedLight(tOff, tOn),
            makeStackedLight(cOff, cOn),
            makeStackedLight(fOff, fOn),
            makeStackedLight(sOff, sOn),
            makeStackedLight(pOff, pOn));
        return row;
    }

    private StackPane makeStackedLight(ImageView off, ImageView on) {
        off.setVisible(true);  off.setOpacity(1);
        on.setVisible(false);  on.setOpacity(0);
        StackPane sp = new StackPane(off, on);
        sp.setMaxWidth(36); sp.setMaxHeight(36);
        return sp;
    }

    private void addDropShadow(Node node, double radius, Color color) {
        node.setEffect(new DropShadow(radius, color));
    }

    private void addGlow(Label label, Color color, double radius, double spread) {
        DropShadow glow = new DropShadow();
        glow.setColor(color); glow.setRadius(radius); glow.setSpread(spread);
        label.setEffect(glow);
    }

    private void addButtonHover(ImageView btn) {
        btn.setOnMouseEntered(e -> btn.setOpacity(0.80));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
    }
}