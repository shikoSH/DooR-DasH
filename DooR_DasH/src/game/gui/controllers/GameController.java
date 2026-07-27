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
import javafx.scene.effect.ColorAdjust;
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
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;

import static game.gui.controllers.GameUIConstants.*;
import static game.gui.controllers.GameUIHelper.*;

/**
 * GameController  (refactored)
 * ----------------------------
 * Thin orchestrator: owns FXML wiring, game-loop entry points, and delegates
 * every functional concern to a dedicated helper class:
 *
 *   • {@link GameUIConstants}    – all magic numbers / image paths
 *   • {@link GameUIHelper}       – label / light / shadow factories
 *   • {@link GamePanelBuilder}   – side-panel and action-log construction
 *   • {@link GameBoardRenderer}  – grid building and board refresh
 *   • {@link GameAnimationHelper}– dice, move, energy-bar, popup, conveyor
 *                                  pointer, and card animations
 *
 * Owner: Integration / GameController team member
 */
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
    @FXML private ImageView  cardDeckView;
    @FXML private ImageView  diceView;
    @FXML private ImageView  powerUpImageBtn;
    @FXML private ImageView  rollImageBtn;
    @FXML private AnchorPane controlBar;
    @FXML private AnchorPane masterLayout;
    @FXML private Label menuBtn;

    // =========================================================
    //  HELPERS / SUB-CONTROLLERS
    // =========================================================
    private GameBoardRenderer boardRenderer;

    // =========================================================
    //  PANEL REFERENCES  (returned by GamePanelBuilder)
    // =========================================================
    private GamePanelBuilder.PlayerPanelRefs   player;
    private GamePanelBuilder.OpponentPanelRefs opponent;

    // Action-log labels
    private Label actionLine1, actionLine2, actionLine3;
    private ImageView actionLogBg;   // stored for resize-binding in applyAllLayout
    private VBox actionLogTextBox;   // NEW

    
    // =========================================================
    //  CARD OVERLAY
    // =========================================================
    private VBox      cardOverlay;
    private ImageView cardOverlayBack, cardOverlayFace;
    private Label     cardOverlayName, cardOverlayDesc, cardOverlayEffect;
    private final BoxBlur worldBlur = new BoxBlur(0, 0, 2);

    // =========================================================
    //  CARD DECK SPREAD OVERLAY
    // =========================================================
    private StackPane cardDeckDimLayer;
    private boolean   cardDeckOverlayVisible = false;

    // =========================================================
    //  MESSAGE OVERLAY  (centered modal: errors + power-up confirm)
    // =========================================================
    private StackPane messageOverlay;
    private VBox      messageBox;
    private Label     messageIcon, messageTitle, messageBody;
    private HBox      messageButtons;
    private javafx.scene.shape.Rectangle messageSep;
    private Animation messageOverlayTransition;
    private int       messageOverlayToken = 0;

    // =========================================================
    //  IMAGES
    // =========================================================
    private Image[] diceImages = new Image[6];
    private Image[] glowingDiceImages = new Image[6];
    private Image deckFull, deckMid, deckLeast;
    // Energy-bar tier images: [en0, en25, en50, en75, en100]
    private Image[] energyTiers = new Image[5];

    // Monster sprite / screen-portrait images
    private Image mi_celia, mi_fungus, mi_henry, mi_james;
    private Image mi_mike, mi_randall, mi_roz, mi_yeti;
    private Image si_celia, si_fungus, si_henry, si_james;
    private Image si_mike, si_randall, si_roz, si_yeti;

    // Card face images
    private Image cardBack, c2319, cContam, cMegaDrain, cMind;
    private Image cSwap, cSmall, cSneaky, cShield, cConfuse;

    // =========================================================
    //  STATE
    // =========================================================
    private Game    game;
    private boolean isAnimating           = false;
    private boolean monsterOverlayVisible = false;
    private Timeline diceTimeline;

    private int playerPowerTurnsLeft   = 0;
    private int opponentPowerTurnsLeft = 0;

    private double targetX = 0, targetY = 0;
    private AnimationTimer parallaxTimer;

    // Shared cellSize property for responsive label scaling
    private final javafx.beans.property.DoubleProperty cellSize =
        new javafx.beans.property.SimpleDoubleProperty(40);
    private boolean   optionsMenuVisible = false;
    private StackPane optionsDimLayer;
    private VBox      optionsMenuCard;
    private Label     optionsTitleLbl, optionsMusicLbl, optionsSfxLbl;
    private javafx.scene.shape.Rectangle optionsSep;
    private Button    optionsResumeBtn, optionsRestartBtn, optionsMainMenuBtn;
    private Slider    optionsMusicSlider, optionsSfxSlider;
    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {
        try {
            javafx.scene.text.Font.loadFont(
                getClass().getResourceAsStream(FONT_PATH), 14);

            loadAllImages();

            backgroundView.setImage(loadImage(IMG_BACKGROUND));
            backgroundView.fitWidthProperty().bind(
                backgroundRoot.widthProperty().multiply(BG_OVERSIZE));
            backgroundView.fitHeightProperty().bind(
                backgroundRoot.heightProperty().multiply(BG_OVERSIZE));
            backgroundView.setEffect(new BoxBlur(3, 3, 2));

            controlPanelView.setImage(loadImage(IMG_CONTROL));
            // Sized manually in applyAllLayout() using ONE uniform scale
            // factor for both width and height — see GameUIConstants
            // "UNIFORM-SCALE LAYOUT" for why. No property binding here;
            // a bound property can't also be set directly every resize.

            setupParallax();

            boardHolderView.setImage(loadImage(IMG_BOARD_HOLDER));
            boardImageView.setImage(loadImage(IMG_BOARD));
            // Board / holder / grid sizes are also set manually in
            // applyAllLayout() for the same reason.

            // ── Delegate to helpers ──────────────────────────────────────
            boardRenderer = new GameBoardRenderer(grid, cellSize,
                this::handleMonsterCellClick, this::handleConveyorCellClick,
                this::handleSockCellClick, this::handleCardCellClick);
            boardRenderer.buildGrid();

            player   = GamePanelBuilder.buildPlayerPanel(playerPanelContainer, energyTiers[4]);
            opponent = GamePanelBuilder.buildOpponentPanel(opponentPanelContainer, energyTiers[4]);

            GamePanelBuilder.ActionLogRefs logRefs = GamePanelBuilder.buildActionLog(actionLogContainer);
            actionLine1  = logRefs.line1;
            actionLine2  = logRefs.line2;
            actionLine3  = logRefs.line3;
            actionLogBg  = logRefs.background;
            actionLogTextBox = logRefs.textBox;   // NEW

            buildCardOverlay();
            buildMessageOverlay();

            if (powerUpImageBtn != null) {
                powerUpImageBtn.setImage(loadImage(IMG_POWERUP_BTN));
                setupPressStates(powerUpImageBtn, IMG_POWERUP_BTN, IMG_POWERUP_BTN_HALF, IMG_POWERUP_BTN_PRESSED, "#ffdd55");
            }
            if (rollImageBtn != null) {
                rollImageBtn.setImage(loadImage(IMG_ROLL_BTN));
                setupPressStates(rollImageBtn, IMG_ROLL_BTN, IMG_ROLL_BTN_HALF, IMG_ROLL_BTN_PRESSED, "#ff4444");
            }
            if (cardDeckView != null) {
                cardDeckView.setImage(deckFull);
                cardDeckView.setCursor(Cursor.HAND);
                cardDeckView.setOnMouseClicked(e -> showCardDeckOverlay());
            }
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
        for (int i = 1; i <= 6; i++) {
            diceImages[i-1] = loadImage("Dice_on_" + i + ".png");
            glowingDiceImages[i-1] = loadImage("Glowing_dice_on_" + i + ".png");
        }
        deckFull  = loadImage(IMG_DECK_FULL);
        deckMid   = loadImage(IMG_DECK_MID);
        deckLeast = loadImage(IMG_DECK_LEAST);

        energyTiers[0] = loadImage("0_Energy_Player.png");
        energyTiers[1] = loadImage("25_Energy_Player.png");
        energyTiers[2] = loadImage("50_Energy_Player.png");
        energyTiers[3] = loadImage("75_Energy_Player.png");
        energyTiers[4] = loadImage("100_Energy_Player.png");

        mi_celia   = loadImage("celia mae.png");
        mi_fungus  = loadImage("Fungus.png");
        mi_henry   = loadImage("Henry_J._Waternoose_III.png");
        mi_james   = loadImage("James sullivan.png");
        mi_mike    = loadImage("Mike_Wazowski.png");
        mi_randall = loadImage("Randall.png");
        mi_roz     = loadImage("Roz.png");
        mi_yeti    = loadImage("Yeti.png");
        si_celia   = loadImage("Celia_Mae_Screen.png");
        si_fungus  = loadImage("FungusScreen.png");
        si_henry   = loadImage("Henry_Screen.png");
        si_james   = loadImage("James_Screen.png");
        si_mike    = loadImage("Mike_Screen.png");
        si_randall = loadImage("Randal_Screen.png");
        si_roz     = loadImage("Rose_Screen.png");
        si_yeti    = loadImage("Yeti_Screen.png");

        cardBack   = loadImage(IMG_CARD_BACK);
        c2319      = loadImage("2319_alert.png");
        cContam    = loadImage("contamination_code.png");
        cMegaDrain = loadImage("mega_drain.png");
        cMind      = loadImage("mind_scramble.png");
        cSwap      = loadImage("position_swap.png");
        cSmall     = loadImage("small_snatcher.png");
        cSneaky    = loadImage("sneaky_theif.png");
        cShield    = loadImage("super_shield.png");
        cConfuse   = loadImage("total_confusion.png");
    }

    // =========================================================
    //  GAME START  (called from StartController / SceneManager)
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
    //  BOARD REFRESH  (delegates to GameBoardRenderer)
    // =========================================================
    private void refreshBoard() {
        boardRenderer.refreshBoard(game, deckFull, deckMid, deckLeast, cardDeckView);
    }

    // =========================================================
    //  UI UPDATE
    // =========================================================
    private void updateUI() {
        if (game == null) return;
        Monster p  = game.getPlayer();
        Monster o  = game.getOpponent();
        Monster cur = game.getCurrent();

        // ── Player panel ─────────────────────────────────────
        player.portrait.setImage(screenPortrait(p.getName()));
        player.nameLbl.setText(p.getName());
        player.typeLbl.setText(p.getClass().getSimpleName());
        player.posLbl.setText(String.valueOf(p.getPosition()));
        player.energyLbl.setText(String.valueOf(p.getEnergy()));
        boolean pConf = !p.getOriginalRole().equals(p.getRole());
        player.roleLbl.setText(p.getRole().toString());
        player.roleLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + TXT_PLAYER_ROLE + "px;" +
            "-fx-text-fill: " + (pConf ? "#ff00ff" : "white") + ";" +
            (pConf ? "-fx-font-weight: bold;" : ""));
        player.statusLbl.setText(statusString(p));
        player.turnLbl.setText(cur == p ? "YOUR TURN" : "");
        player.energyBarCurrentImage = GameAnimationHelper.animateEnergyBar(
            player.energyBar, player.energyWrapper,
            p.getEnergy(), player.energyBarCurrentImage, energyTiers);

        // ── Opponent panel ────────────────────────────────────
        opponent.portrait.setImage(screenPortrait(o.getName()));
        opponent.nameLbl.setText(o.getName());
        opponent.typeLbl.setText(o.getClass().getSimpleName());
        opponent.posLbl.setText(String.valueOf(o.getPosition()));
        opponent.energyLbl.setText(String.valueOf(o.getEnergy()));
        boolean oConf = !o.getOriginalRole().equals(o.getRole());
        opponent.roleLbl.setText(o.getRole().toString());
        opponent.roleLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + TXT_PLAYER_ROLE + "px;" +
            "-fx-text-fill: " + (oConf ? "#ff00ff" : "white") + ";" +
            (oConf ? "-fx-font-weight: bold;" : ""));
        opponent.statusLbl.setText(statusString(o));
        opponent.energyBarCurrentImage = GameAnimationHelper.animateEnergyBar(
            opponent.energyBar, opponent.energyWrapper,
            o.getEnergy(), opponent.energyBarCurrentImage, energyTiers);

        // ── Top label ─────────────────────────────────────────
        myLabel.setText(cur == p ? "YOUR TURN - PRESS ROLL!" : "OPPONENT TURN - PRESS ROLL!");
        myLabel.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + TXT_TOP_LABEL + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 0 3 0;" +
            "-fx-text-fill: " + (cur == p ? "#00ff88" : "#ff6666") + ";");

        // ── Power-up turn counters ────────────────────────────
        if (playerPowerTurnsLeft   > 0) playerPowerTurnsLeft--;
        if (opponentPowerTurnsLeft > 0) opponentPowerTurnsLeft--;

        // ── Status lights ─────────────────────────────────────
        setLight(player.turnOff,   player.turnOn,   cur == p,                   player.turnState,  v -> player.turnState  = v);
        setLight(player.confOff,   player.confOn,   p.isConfused(),              player.confState,  v -> player.confState  = v);
        setLight(player.frzOff,    player.frzOn,    p.isFrozen(),                player.frzState,   v -> player.frzState   = v);
        setLight(player.shldOff,   player.shldOn,   p.isShielded(),              player.shldState,  v -> player.shldState  = v);
        setLight(player.pwrOff,    player.pwrOn,    playerPowerTurnsLeft > 0,   player.pwrState,   v -> player.pwrState   = v);

        setLight(opponent.turnOff, opponent.turnOn, cur == o,                    opponent.turnState, v -> opponent.turnState = v);
        setLight(opponent.confOff, opponent.confOn, o.isConfused(),              opponent.confState, v -> opponent.confState = v);
        setLight(opponent.frzOff,  opponent.frzOn,  o.isFrozen(),                opponent.frzState,  v -> opponent.frzState  = v);
        setLight(opponent.shldOff, opponent.shldOn, o.isShielded(),              opponent.shldState, v -> opponent.shldState  = v);
        setLight(opponent.pwrOff,  opponent.pwrOn,  opponentPowerTurnsLeft > 0, opponent.pwrState,  v -> opponent.pwrState  = v);
    }

    // =========================================================
    //  ROLL HANDLER
    // =========================================================
    @FXML
    private void handleRollDice() {
        if (game == null || cardOverlay.isVisible() || isAnimating || monsterOverlayVisible || cardDeckOverlayVisible || optionsMenuVisible) return;
        try {
            isAnimating = true;
            Monster current  = game.getCurrent();
            Monster opp      = current == game.getPlayer()
                ? game.getOpponent() : game.getPlayer();

            int  oldPos    = current.getPosition();
            int  oldEnergy = current.getEnergy();
            int  oldOppEng = opp.getEnergy();
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

            // Snapshot door activation states before the turn
            boolean[][] doorWasActivated = new boolean[10][10];
            Cell[][] cells = game.getBoard().getBoardCells();
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    if (cells[r][c] instanceof DoorCell)
                        doorWasActivated[r][c] = ((DoorCell) cells[r][c]).isActivated();

            // Snapshot stationed monster energies before the turn
            java.util.Map<String, Integer> stationedBefore = new java.util.HashMap<>();
            for (Monster m : Board.getStationedMonsters())
                stationedBefore.put(m.getName(), m.getEnergy());

            game.playTurn();

            int diceRoll  = game.getLastRoll();
            int rawLandingPos = game.getBoard().getLastRawLandingPos();
            int newPos    = current.getPosition();          // final resting position, AFTER any transport
            int newEnergy = current.getEnergy();
            int newOppEng = opp.getEnergy();
            boolean drawn = topCard != null &&
                (Board.cards.isEmpty() || Board.cards.get(0) != topCard);

            int diceFace = Math.max(1, Math.min(6, diceRoll));   // FIXED: use the real roll, not (newPos - oldPos),
                                                                   // which was wrong whenever a transport cell fired

            actionLine1.setText(current.getName() + " -> POS " + newPos + " (+" + diceRoll + ")");
            actionLine2.setText(drawn && topCard != null
                ? topCard.getName() + ": " + cardEffect(topCard.getName()) : "");
            if (wasShld && !current.isShielded())
                actionLine3.setText("SHIELD BLOCKED LOSS!");
            else if (newEnergy != oldEnergy)
                actionLine3.setText(current.getName()
                    + (newEnergy - oldEnergy > 0 ? " +" : " ")
                    + (newEnergy - oldEnergy) + " -> " + newEnergy);
            else if (newOppEng != oldOppEng)
                actionLine3.setText(opp.getName()
                    + (newOppEng - oldOppEng > 0 ? " +" : " ")
                    + (newOppEng - oldOppEng) + " -> " + newOppEng);
            else
                actionLine3.setText("");

            final Card    fc   = topCard;
            final boolean fd   = drawn;
            final Monster fm   = current;
            final Monster fo   = opp;
            final int     fp   = oldPos;
            final int     frl  = rawLandingPos;         // where the dice roll actually lands
            final int     fn   = newPos;                // where the monster ends up after any transport
            final Cell    rawLandingCell = game.getBoard().getCell(rawLandingPos);
            final boolean[][] finalDoorSnap = doorWasActivated;
            final java.util.Map<String, Integer> finalStationedBefore = stationedBefore;

            if (diceTimeline != null) diceTimeline.stop();
            SoundManager.getInstance().playDiceRoll();
            diceTimeline = GameAnimationHelper.animateDice(
                diceView, diceImages, glowingDiceImages, null, diceFace,
                () -> {
                    SoundManager.getInstance().playMovement();
                    // LEG 1: walk from the old position to the raw dice-landing cell
                    GameAnimationHelper.animateMove(
                        fm, fo, fp, frl,
                        boardRenderer.getMonsterViews(),
                        boardRenderer.getSpotlightViews(),
                        grid,
                        () -> {
                            Runnable finishTurn = () -> {
                                refreshBoard(); updateUI();

                                // Door-opening sound
                                Cell landed = game.getBoard().getCell(fn);
                                int[] rc = game.getBoard().indexToRowCol(fn);
                                if (landed instanceof DoorCell
                                        && !finalDoorSnap[rc[0]][rc[1]]
                                        && ((DoorCell) landed).isActivated()) {
                                    SoundManager.getInstance().playDoorOpening();
                                }

                                // Stationed monster energy popups
                                for (Monster stationed : Board.getStationedMonsters()) {
                                    Integer before = finalStationedBefore.get(stationed.getName());
                                    if (before != null && stationed.getEnergy() != before) {
                                        int diff = stationed.getEnergy() - before;
                                        int[] dst = GameAnimationHelper.toRowCol(stationed.getPosition());
                                        for (Node child : grid.getChildren()) {
                                            Integer cIdx = GridPane.getColumnIndex(child);
                                            Integer rIdx = GridPane.getRowIndex(child);
                                            int col = (cIdx == null) ? 0 : cIdx;
                                            int row = (rIdx == null) ? 0 : rIdx;
                                            if (col == dst[1] && row == dst[0] && child instanceof StackPane) {
                                                GameAnimationHelper.createFloatingPopup(
                                                    (StackPane) child, diff >= 0, Math.abs(diff));
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (fd && fc != null) showCardOverlay(fc);
                                checkWinner();
                                isAnimating = false;
                            };

                            // LEG 2: only runs if the raw landing cell actually transported the monster
                            if (frl != fn) {
                                if (rawLandingCell instanceof ConveyorBelt) {
                                    Runnable doTransportHop = () -> {
                                        SoundManager.getInstance().playConveyorBelt();
                                        GameAnimationHelper.animateMove(
                                            fm, fo, frl, fn,
                                            boardRenderer.getMonsterViews(),
                                            boardRenderer.getSpotlightViews(),
                                            grid,
                                            finishTurn,
                                            true);
                                    };
                                    // Show the destination pointer first, then hop (with sound) once it finishes.
                                    GameAnimationHelper.animateConveyorPointer(
                                        frl, fn, grid, true, doTransportHop);
                                } else if (rawLandingCell instanceof ContaminationSock) {
                                    Runnable doTransportHop = () -> {
                                        SoundManager.getInstance().playContaminationSock();
                                        boolean movingIsPlayer = fm == game.getPlayer();
                                        GameAnimationHelper.animateEnergyLossFlash(
                                            backgroundRoot, movingIsPlayer ? player.portrait : opponent.portrait);
                                        GameAnimationHelper.animateMove(
                                            fm, fo, frl, fn,
                                            boardRenderer.getMonsterViews(),
                                            boardRenderer.getSpotlightViews(),
                                            grid,
                                            finishTurn,
                                            true);
                                    };
                                    // Show the destination pointer first, then hop (with sound) once it finishes.
                                    GameAnimationHelper.animateConveyorPointer(
                                        frl, fn, grid, false, doTransportHop);
                                } else {
                                    GameAnimationHelper.animateMove(
                                        fm, fo, frl, fn,
                                        boardRenderer.getMonsterViews(),
                                        boardRenderer.getSpotlightViews(),
                                        grid,
                                        finishTurn,
                                        true);
                                }
                            } else {
                                finishTurn.run();
                            }
                        });
                });  
        } catch (game.engine.exceptions.InvalidMoveException ex) {
            actionLine1.setText("INVALID: " + ex.getMessage());
            actionLine2.setText("ROLL AGAIN!"); actionLine3.setText("");
            refreshBoard(); updateUI();
            isAnimating = false;
            SoundManager.getInstance().playInvalidMove();
            showErrorAlert("Invalid Move", ex.getMessage());
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            actionLine1.setText("ERROR: " + msg);
            actionLine2.setText(""); actionLine3.setText("");
            isAnimating = false;
            showErrorAlert("Error", msg);
            ex.printStackTrace();
        }
    }

    // =========================================================
    //  POWER-UP HANDLER
    // =========================================================
    @FXML
    private void handlePowerUp() {
        if (game == null || cardOverlay.isVisible() || monsterOverlayVisible || cardDeckOverlayVisible || optionsMenuVisible) return;
        if (messageOverlay != null && messageOverlay.isVisible()) return;

        if (powerUpImageBtn != null) {
            ScaleTransition press = new ScaleTransition(Duration.millis(90), powerUpImageBtn);
            press.setFromX(1.0); press.setFromY(1.0);
            press.setToX(0.90);  press.setToY(0.90);
            press.setCycleCount(2);
            press.setAutoReverse(true);
            press.play();
        }

        showConfirmDialog("Use Powerup", "Activate Powerup?",
            "Costs " + Constants.POWERUP_COST + " energy. Proceed?",
            () -> {
                try {
                    Monster current  = game.getCurrent();
                    boolean isPlayer = (current == game.getPlayer());
                    String name = current.getName();
                    game.usePowerup();

                    actionLine1.setText(name + " POWERUP ACTIVATED!");
                    actionLine2.setText(""); actionLine3.setText("");
                    int POWERUP_DURATION = 3;
                    if (isPlayer) playerPowerTurnsLeft   = POWERUP_DURATION;
                    else          opponentPowerTurnsLeft = POWERUP_DURATION;
                    refreshBoard(); updateUI();

                    SoundManager.getInstance().playPowerUp();
                    GameAnimationHelper.animatePowerUpActivation(
                        backgroundRoot, isPlayer ? player.portrait : opponent.portrait);
                } catch (game.engine.exceptions.OutOfEnergyException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage()
                        : "Not enough energy! Need " + Constants.POWERUP_COST + " energy to activate.";
                    SoundManager.getInstance().playInvalidMove();
                    showErrorAlert("Not Enough Energy", msg);
                } catch (Exception ex) {
                    showErrorAlert("Powerup Failed",
                        ex.getMessage() != null ? ex.getMessage() : "Could not activate powerup.");
                }
            });
    }
    
    @FXML
    private void handleMenuButton() {
        showOptionsMenuOverlay();
    }

    // =========================================================
    //  GAME OPTIONS MENU  (same visual pattern as showMonsterInfoOverlay)
    // =========================================================
    private void showOptionsMenuOverlay() {
        if (game == null || cardOverlay.isVisible() || monsterOverlayVisible
                || cardDeckOverlayVisible || optionsMenuVisible) return;
        optionsMenuVisible = true;

        double curScale = overlayScale();
        double curBoardSide = REF_BOARD_SIZE * curScale;
        // Noticeably larger than the monster-card fractions — this menu is
        // button-heavy and needs to read clearly, not fit dense stat rows.
        double titleFontPx = Math.max(16, curBoardSide * TXT_CARD_NAME_FRAC * 1.5);
        double bodyFontPx  = Math.max(12, curBoardSide * TXT_CARD_BODY_FRAC * 1.35);
        double btnFontPx   = Math.max(13, curBoardSide * TXT_CARD_BODY_FRAC * 1.5);

        optionsTitleLbl = makeLbl("GAME MENU", "#ffdd55", (int) titleFontPx, true);
        optionsTitleLbl.setAlignment(Pos.CENTER);

        optionsSep = new javafx.scene.shape.Rectangle(curBoardSide * 0.36, 2);
        optionsSep.setFill(Color.web("#c9a227"));

        optionsResumeBtn = overlayActionButton("RESUME", "#dddddd", "#1a1a1a", "#888888");
        optionsResumeBtn.setMaxWidth(Double.MAX_VALUE);
        optionsResumeBtn.setOnAction(e -> dismissOptionsMenu());

        optionsRestartBtn = overlayActionButton("RESTART  MATCH", "#ffdd55", "#1a1a1a", "#c9a227");
        optionsRestartBtn.setMaxWidth(Double.MAX_VALUE);
        optionsRestartBtn.setOnAction(e -> handleRestartMatch());

        optionsMusicLbl = makeLbl("MUSIC VOLUME", "#aaaaaa", (int) bodyFontPx, false);
        optionsMusicSlider = new Slider(0, 1, SceneManager.getInstance().getMusicVolume());
        optionsMusicSlider.setStyle("-fx-accent:#c9a227;");
        optionsMusicSlider.valueProperty().addListener((obs, o, n) ->
            SceneManager.getInstance().setMusicVolume(n.doubleValue()));

        optionsSfxLbl = makeLbl("SOUND EFFECTS VOLUME", "#aaaaaa", (int) bodyFontPx, false);
        optionsSfxSlider = new Slider(0, 1, SoundManager.getSfxVolume());
        optionsSfxSlider.setStyle("-fx-accent:#c9a227;");
        optionsSfxSlider.valueProperty().addListener((obs, o, n) -> SoundManager.setSfxVolume(n.doubleValue()));

        optionsMainMenuBtn = overlayActionButton("MAIN MENU", "#ff8888", "#1a1a1a", "#c9463f");
        optionsMainMenuBtn.setMaxWidth(Double.MAX_VALUE);
        optionsMainMenuBtn.setOnAction(e -> handleGoToMainMenu());

        optionsMenuCard = new VBox(16,
            optionsTitleLbl, optionsSep, optionsResumeBtn, optionsRestartBtn,
            optionsMusicLbl, optionsMusicSlider, optionsSfxLbl, optionsSfxSlider,
            optionsMainMenuBtn);
        optionsMenuCard.setAlignment(Pos.TOP_CENTER);
        optionsMenuCard.getStyleClass().add("game-modal-panel");
        applyModalPanelStyle(optionsMenuCard, "#c9a227",
            "linear-gradient(to bottom,rgba(16,16,24,0.97),rgba(6,6,12,0.99))");
        addDropShadow(optionsMenuCard, 36, Color.BLACK);
        layoutOptionsMenuPanel();

        optionsDimLayer = new StackPane(optionsMenuCard);
        optionsDimLayer.setStyle("-fx-background-color:rgba(0,0,0,0.62);");
        optionsDimLayer.setPickOnBounds(true);
        optionsDimLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        optionsDimLayer.setOpacity(0);
        StackPane.setAlignment(optionsMenuCard, Pos.CENTER);
        optionsDimLayer.setOnMouseClicked(e -> {
            if (e.getTarget() == optionsDimLayer) dismissOptionsMenu();
        });
        optionsMenuCard.setOnMouseClicked(javafx.event.Event::consume);

        backgroundRoot.getChildren().add(optionsDimLayer);
        optionsDimLayer.toFront();

        masterLayout.setEffect(worldBlur);
        Timeline blurIn = new Timeline(
            new KeyFrame(Duration.ZERO,       ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0); }),
            new KeyFrame(Duration.millis(350), ev -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }));
        FadeTransition dimFade = new FadeTransition(Duration.millis(300), optionsDimLayer);
        dimFade.setFromValue(0); dimFade.setToValue(1);
        optionsMenuCard.setScaleX(0.90); optionsMenuCard.setScaleY(0.90);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(280), optionsMenuCard);
        scaleIn.setFromX(0.90); scaleIn.setFromY(0.90);
        scaleIn.setToX(1.0);   scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(blurIn, dimFade, scaleIn).play();
    }

    /** Recomputes the menu panel size from the live window — mirrors layoutMonsterOverlayPanel(). */
    private void layoutOptionsMenuPanel() {
        if (optionsMenuCard == null) return;
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W <= 0 || H <= 0) return;

        double scale = overlayScale();
        double curBoardSide = REF_BOARD_SIZE * scale;
        double panelW = clampModalSize(
            Math.min(Math.min(W * 0.42, H * 0.58), curBoardSide * 0.58),
            320 * scale, 460 * scale);
        double pad = Math.max(18, panelW * 0.07);

        double titleFontPx = Math.max(16, panelW * 0.085);
        double bodyFontPx  = Math.max(12, panelW * 0.052);
        double btnFontPx   = Math.max(13, panelW * 0.058);

        optionsMenuCard.setMinWidth(panelW * 0.92);
        optionsMenuCard.setMaxWidth(panelW);
        optionsMenuCard.setPrefWidth(panelW);
        optionsMenuCard.setMaxHeight(Region.USE_PREF_SIZE);        // CHANGED — hug content instead of stretching to a fixed panelH
        optionsMenuCard.setMaxHeight(Region.USE_PREF_SIZE);
        optionsMenuCard.setPadding(new Insets(pad * 1.1, pad * 1.15, pad, pad * 1.15));

        if (optionsSep != null) optionsSep.setWidth(panelW * 0.72);
        if (optionsTitleLbl != null) setFontSize(optionsTitleLbl, titleFontPx);
        if (optionsMusicLbl != null) setFontSize(optionsMusicLbl, bodyFontPx);
        if (optionsSfxLbl   != null) setFontSize(optionsSfxLbl,   bodyFontPx);
        if (optionsResumeBtn   != null) restyleOverlayButtonFont(optionsResumeBtn,   btnFontPx);
        if (optionsRestartBtn  != null) restyleOverlayButtonFont(optionsRestartBtn,  btnFontPx);
        if (optionsMainMenuBtn != null) restyleOverlayButtonFont(optionsMainMenuBtn, btnFontPx);
        if (optionsMusicSlider != null) optionsMusicSlider.setPrefWidth(panelW * 0.8);
        if (optionsSfxSlider   != null) optionsSfxSlider.setPrefWidth(panelW * 0.8);
    }

    private void dismissOptionsMenuImmediate() {
        if (optionsDimLayer != null) {
            backgroundRoot.getChildren().remove(optionsDimLayer);
            optionsDimLayer = null;
        }
        optionsMenuVisible = false;
        masterLayout.setEffect(null);
        worldBlur.setWidth(0);
        worldBlur.setHeight(0);
    }

    private void dismissOptionsMenu() {
        if (optionsDimLayer == null) return;
        Timeline blurOut = new Timeline(
            new KeyFrame(Duration.ZERO,       ev -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }),
            new KeyFrame(Duration.millis(280), ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0);
                                                        masterLayout.setEffect(null); }));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(240), optionsDimLayer);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        final StackPane layer = optionsDimLayer;
        fadeOut.setOnFinished(ev -> {
            backgroundRoot.getChildren().remove(layer);
            optionsMenuVisible = false;
            optionsDimLayer = null;
            optionsMenuCard = null;
            optionsTitleLbl = null; optionsMusicLbl = null; optionsSfxLbl = null;   // ADD
            optionsSep = null;
            optionsResumeBtn = null; optionsRestartBtn = null; optionsMainMenuBtn = null;   // ADD
            optionsMusicSlider = null; optionsSfxSlider = null;                     // ADD
        });
        new ParallelTransition(blurOut, fadeOut).play();
    }

    /** Restarts the match with the same original role, resetting all in-progress turn state. */
    private void handleRestartMatch() {
        if (game == null) return;
        Role originalRole = game.getPlayer().getOriginalRole();
        dismissOptionsMenu();
        if (diceTimeline != null) diceTimeline.stop();
        isAnimating = false;
        playerPowerTurnsLeft = 0;
        opponentPowerTurnsLeft = 0;
        startGame(originalRole);
    }

    private void handleGoToMainMenu() {
        dismissOptionsMenu();
        SceneManager.getInstance().switchToStartScreen();
    }
    
    
    
    // =========================================================
    //  MONSTER CELL CLICK
    // =========================================================
    private void handleMonsterCellClick(int boardIndex) {
        if (game == null) return;
        Monster clicked = getMonsterAtCell(boardIndex);
        if (clicked == null) return;
        showMonsterInfoOverlay(clicked);
    }

    private Monster getMonsterAtCell(int cellIndex) {
        if (game == null) return null;
        if (game.getPlayer().getPosition()   == cellIndex) return game.getPlayer();
        if (game.getOpponent().getPosition() == cellIndex) return game.getOpponent();
        for (Monster m : Board.getStationedMonsters())
            if (m.getPosition() == cellIndex) return m;
        return null;
    }

    // =========================================================
    //  CONVEYOR CELL CLICK
    // =========================================================

    /**
     * Shows an animated pointer from the tapped conveyor-belt cell to the
     * cell it will transport a monster to. Purely visual: reads the
     * destination from the engine's ConveyorBelt cell but never mutates
     * game state or moves any monster.
     */
    private void handleConveyorCellClick(int boardIndex) {
        if (game == null || isAnimating) return;

        Cell[][] cells = game.getBoard().getBoardCells();
        int row = boardIndex / Constants.BOARD_COLS;
        int col = boardIndex % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        if (row < 0 || row >= cells.length || col < 0 || col >= cells[0].length) return;

        Cell cell = cells[row][col];
        if (!(cell instanceof ConveyorBelt)) return;

        int destIndex = boardIndex + ((ConveyorBelt) cell).getEffect();
        destIndex = Math.max(0, Math.min(Constants.BOARD_SIZE - 1, destIndex));

        GameAnimationHelper.animateConveyorPointer(boardIndex, destIndex, grid, true);
    }

    // =========================================================
    //  CONTAMINATION SOCK CELL CLICK
    // =========================================================

    /**
     * Shows an animated pointer from the tapped contamination-sock cell to
     * the cell it will transport a monster to. Purely visual: reads the
     * destination from the engine's ContaminationSock cell but never
     * mutates game state or moves any monster.
     */
    private void handleSockCellClick(int boardIndex) {
        if (game == null || isAnimating) return;

        Cell[][] cells = game.getBoard().getBoardCells();
        int row = boardIndex / Constants.BOARD_COLS;
        int col = boardIndex % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        if (row < 0 || row >= cells.length || col < 0 || col >= cells[0].length) return;

        Cell cell = cells[row][col];
        if (!(cell instanceof ContaminationSock)) return;

        int destIndex = boardIndex + ((ContaminationSock) cell).getEffect();
        destIndex = Math.max(0, Math.min(Constants.BOARD_SIZE - 1, destIndex));

        GameAnimationHelper.animateConveyorPointer(boardIndex, destIndex, grid, false);
    }

    // =========================================================
    //  CARD CELL CLICK
    // =========================================================

    /**
     * Shows the full-deck spread overlay when a Card cell is tapped.
     * Purely visual: does not draw a card or touch engine state.
     */
    private void handleCardCellClick(int boardIndex) {
        if (game == null || isAnimating || cardDeckOverlayVisible) return;

        Cell[][] cells = game.getBoard().getBoardCells();
        int row = boardIndex / Constants.BOARD_COLS;
        int col = boardIndex % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        if (row < 0 || row >= cells.length || col < 0 || col >= cells[0].length) return;

        Cell cell = cells[row][col];
        if (!(cell instanceof CardCell)) return;

        showCardDeckOverlay();
    }

    // =========================================================
    //  MONSTER INFO OVERLAY  (centered modal panel + CLOSE)
    // =========================================================

    private ImageView monsterOverlayFace;
    private VBox      monsterOverlayCard;
    private StackPane monsterDimLayer;
    private Label     monsterNameLbl, monsterDescLbl;
    private VBox      monsterStatsBox;
    private javafx.scene.shape.Rectangle monsterSep;
    private Button    monsterCloseBtn;

    /**
     * Centered dimmed modal with monster portrait, stats, and a CLOSE button.
     * Panel width / fonts are recomputed from the current window size so the
     * popup stays centered and readable across resizes (StackPane centering,
     * no absolute layout coords).
     */
    private void showMonsterInfoOverlay(Monster m) {
        if (m == null) return;
        if (monsterDimLayer != null) dismissMonsterOverlayImmediate();
        monsterOverlayVisible = true;

        double curScale = overlayScale();
        double curBoardSide = REF_BOARD_SIZE * curScale;
        final double statFontPx = Math.max(9, curBoardSide * TXT_CARD_BODY_FRAC);
        double nameFontPx = Math.max(11, curBoardSide * TXT_CARD_NAME_FRAC);
        double bodyFontPx = Math.max(9,  curBoardSide * TXT_CARD_BODY_FRAC * 0.9);

        monsterOverlayFace = new ImageView(screenPortrait(m.getName()));
        monsterOverlayFace.setPreserveRatio(true);
        monsterOverlayFace.setFitWidth(curBoardSide * 0.34);
        addDropShadow(monsterOverlayFace, 22, Color.BLACK);

        java.util.function.BiFunction<String, String, HBox> statRow = (key, val) -> {
            Label k = new Label(key);
            k.setStyle("-fx-font-family:'" + FONT + "';-fx-font-size:" + statFontPx + "px;-fx-text-fill:#888888;");
            Label v = new Label(val);
            v.setStyle("-fx-font-family:'" + FONT + "';-fx-font-size:" + statFontPx + "px;-fx-font-weight:bold;-fx-text-fill:#e8e8e8;");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            HBox row = new HBox(k, sp, v);
            row.setSpacing(6);
            row.setAlignment(Pos.CENTER_LEFT);
            return row;
        };

        boolean confused = m.isConfused();
        StringBuilder statusSb = new StringBuilder();
        if (!m.isShielded() && !m.isFrozen() && !confused) statusSb.append("NORMAL");
        if (m.isShielded()) statusSb.append("SHIELD ");
        if (m.isFrozen())   statusSb.append("FROZEN ");
        if (confused)       statusSb.append("CONFUSED(").append(m.getConfusionTurns()).append("T)");

        monsterSep = new javafx.scene.shape.Rectangle(curBoardSide * 0.32, 1.5);
        monsterSep.setFill(Color.web("#c9a227"));

        monsterNameLbl = makeLbl(m.getName().toUpperCase(), "#ffdd55", (int) nameFontPx, true);
        monsterNameLbl.setAlignment(Pos.CENTER);

        monsterStatsBox = new VBox(6);
        monsterStatsBox.setAlignment(Pos.CENTER_LEFT);
        monsterStatsBox.setMaxWidth(curBoardSide * 0.34);
        monsterStatsBox.getChildren().addAll(
            statRow.apply("TYPE",     m.getClass().getSimpleName()),
            statRow.apply("ROLE",     m.getRole().toString()),
            statRow.apply("ENERGY",   String.valueOf(m.getEnergy())),
            statRow.apply("POSITION", String.valueOf(m.getPosition())),
            statRow.apply("STATUS",   statusSb.toString().trim())
        );

        monsterDescLbl = makeLbl(m.getDescription(), "#9a9a9a", (int) bodyFontPx, false);
        monsterDescLbl.setWrapText(true);
        monsterDescLbl.setMaxWidth(curBoardSide * 0.34);
        monsterDescLbl.setAlignment(Pos.CENTER);
        monsterDescLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        monsterCloseBtn = overlayActionButton("CLOSE", "#dddddd", "#1a1a1a", "#888888");
        monsterCloseBtn.setOnAction(e -> dismissMonsterOverlay());

        monsterOverlayCard = new VBox(12,
            monsterOverlayFace, monsterNameLbl, monsterSep, monsterStatsBox,
            monsterDescLbl, monsterCloseBtn);
        monsterOverlayCard.setAlignment(Pos.TOP_CENTER);
        monsterOverlayCard.getStyleClass().add("game-modal-panel");
        applyModalPanelStyle(monsterOverlayCard, "#c9a227",
            "linear-gradient(to bottom,rgba(16,16,24,0.97),rgba(6,6,12,0.99))");
        addDropShadow(monsterOverlayCard, 36, Color.BLACK);
        layoutMonsterOverlayPanel();

        monsterDimLayer = new StackPane(monsterOverlayCard);
        monsterDimLayer.setStyle("-fx-background-color:rgba(0,0,0,0.62);");
        monsterDimLayer.setPickOnBounds(true);
        monsterDimLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        monsterDimLayer.setOpacity(0);
        StackPane.setAlignment(monsterOverlayCard, Pos.CENTER);
        monsterDimLayer.setOnMouseClicked(e -> {
            if (e.getTarget() == monsterDimLayer) dismissMonsterOverlay();
        });
        monsterOverlayCard.setOnMouseClicked(javafx.event.Event::consume);

        backgroundRoot.getChildren().add(monsterDimLayer);
        monsterDimLayer.toFront();

        masterLayout.setEffect(worldBlur);
        Timeline blurIn = new Timeline(
            new KeyFrame(Duration.ZERO,       ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0); }),
            new KeyFrame(Duration.millis(350), ev -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }));
        FadeTransition dimFade = new FadeTransition(Duration.millis(300), monsterDimLayer);
        dimFade.setFromValue(0); dimFade.setToValue(1);
        monsterOverlayCard.setScaleX(0.90); monsterOverlayCard.setScaleY(0.90);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(280), monsterOverlayCard);
        scaleIn.setFromX(0.90); scaleIn.setFromY(0.90);
        scaleIn.setToX(1.0);   scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(blurIn, dimFade, scaleIn).play();
    }

    /** Recomputes monster modal sizes from the live window — called on open and resize. */
    private void layoutMonsterOverlayPanel() {
        if (monsterOverlayCard == null) return;
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W <= 0 || H <= 0) return;

        double scale = overlayScale();
        double curBoardSide = REF_BOARD_SIZE * scale;
        // Compact floating window: ~square-ish card, clearly inset from edges
        double panelW = clampModalSize(
            Math.min(Math.min(W * 0.30, H * 0.42), curBoardSide * 0.40),
            220 * scale, 300 * scale);
        double panelH = clampModalSize(
            Math.min(H * 0.70, curBoardSide * 0.92),
            280 * scale, 520 * scale);
        double pad = Math.max(14, panelW * 0.06);
        double nameFontPx = Math.max(11, panelW * 0.065);
        double bodyFontPx = Math.max(9,  panelW * 0.048);
        double statFontPx = Math.max(9,  panelW * 0.050);
        double btnFontPx  = Math.max(10, panelW * 0.048);

        monsterOverlayCard.setMinWidth(panelW * 0.92);
        monsterOverlayCard.setMaxWidth(panelW);
        monsterOverlayCard.setPrefWidth(panelW);
        monsterOverlayCard.setMaxHeight(panelH);
        monsterOverlayCard.setPadding(new Insets(pad * 1.1, pad * 1.15, pad, pad * 1.15));
        if (monsterOverlayFace != null) monsterOverlayFace.setFitWidth(panelW * 0.62);
        if (monsterSep != null) monsterSep.setWidth(panelW * 0.68);
        if (monsterStatsBox != null) monsterStatsBox.setMaxWidth(panelW * 0.84);
        if (monsterNameLbl != null) setFontSize(monsterNameLbl, nameFontPx);
        if (monsterDescLbl != null) {
            monsterDescLbl.setMaxWidth(panelW * 0.84);
            setFontSize(monsterDescLbl, bodyFontPx);
        }
        if (monsterCloseBtn != null) restyleOverlayButtonFont(monsterCloseBtn, btnFontPx);
        if (monsterStatsBox != null) {
            for (Node row : monsterStatsBox.getChildren()) {
                if (!(row instanceof HBox)) continue;
                for (Node child : ((HBox) row).getChildren()) {
                    if (child instanceof Label) setFontSize((Label) child, statFontPx);
                }
            }
        }
    }

    private void dismissMonsterOverlayImmediate() {
        if (monsterDimLayer != null) {
            backgroundRoot.getChildren().remove(monsterDimLayer);
            monsterDimLayer = null;
        }
        monsterOverlayVisible = false;
        masterLayout.setEffect(null);
        worldBlur.setWidth(0);
        worldBlur.setHeight(0);
    }

    private void dismissMonsterOverlay() {
        if (monsterDimLayer == null) return;
        Timeline blurOut = new Timeline(
            new KeyFrame(Duration.ZERO,       ev -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }),
            new KeyFrame(Duration.millis(280), ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0);
                                                        masterLayout.setEffect(null); }));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(240), monsterDimLayer);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        final StackPane layer = monsterDimLayer;
        fadeOut.setOnFinished(ev -> {
            backgroundRoot.getChildren().remove(layer);
            monsterOverlayVisible = false;
            monsterDimLayer = null;
            monsterOverlayCard = null;
        });
        new ParallelTransition(blurOut, fadeOut).play();
    }

    // =========================================================
    //  CARD DECK SPREAD OVERLAY
    // =========================================================

    /**
     * Blurs the background and fans out every card type in the deck, each
     * with an "xN" label showing how many of that card remain. Purely
     * visual — reads Board's card lists but never mutates them.
     */
    private void showCardDeckOverlay() {
        if (cardOverlay.isVisible() || monsterOverlayVisible || cardDeckOverlayVisible) return;
        cardDeckOverlayVisible = true;

        // ── Tally remaining counts per card type, keeping the deck's
        //    original first-seen order so the spread reads consistently
        //    across turns. ──────────────────────────────────────────────
        java.util.LinkedHashMap<String, Integer> counts = new java.util.LinkedHashMap<>();
        for (Card c : Board.getOriginalCards()) counts.putIfAbsent(c.getName(), 0);
        for (Card c : Board.cards) counts.merge(c.getName(), 1, Integer::sum);

        // ── Current proportional sizing (window may have resized) ────────
        double curW = backgroundRoot.getWidth();
        double curH = backgroundRoot.getHeight();
        double curScale = Math.min(curW / REF_W, curH / REF_H);
        double curBoardSide = REF_BOARD_SIZE * curScale;

        int n = Math.max(1, counts.size());
        // Sized off the full screen width (curW) rather than the board alone,
        // and with a much smaller overlap below, so the fanned deck reads as
        // big cards spread across the whole screen instead of a small cluster.
        double cardW = Math.min(curW * 0.17, (curW * 0.96) / n);
        double countFontPx = Math.max(10, cardW * 0.12);

        HBox row = new HBox(-cardW * 0.08);
        row.setAlignment(Pos.BOTTOM_CENTER);
        row.setPadding(new Insets(10, 6, 4, 6));

        java.util.List<Node>   entries       = new java.util.ArrayList<>();
        java.util.List<Double> entryAngles   = new java.util.ArrayList<>();
        java.util.List<Double> entryLifts    = new java.util.ArrayList<>();
        double mid = (n - 1) / 2.0;
        int idx = 0;
        for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) {
            String name      = e.getKey();
            int    remaining = e.getValue();

            ImageView face = new ImageView(cardFace(name));
            face.setPreserveRatio(true);
            face.setFitWidth(cardW);
            addDropShadow(face, 12, Color.BLACK);
            if (remaining == 0) face.setOpacity(0.35);

            Label countLbl = makeLbl("x" + remaining,
                remaining > 0 ? "#ffdd33" : "#555555", (int) countFontPx, true);
            countLbl.setAlignment(Pos.CENTER);
            countLbl.setMaxWidth(cardW);

            VBox entry = new VBox(4, face, countLbl);
            entry.setAlignment(Pos.TOP_CENTER);

            double offset = idx - mid;
            double angle  = offset * 10.0;
            double lift   = Math.abs(offset) * (cardW * 0.16);
            entryAngles.add(angle);
            entryLifts.add(lift);

            // Start collapsed toward the centre, unrotated and tiny —
            // the entrance animation below "spreads" them into their
            // fanned-out resting position.
            entry.setRotate(0);
            entry.setTranslateX(-offset * (cardW * 0.9));
            entry.setTranslateY(0);
            entry.setOpacity(0);
            entry.setScaleX(0.5); entry.setScaleY(0.5);

            entries.add(entry);
            row.getChildren().add(entry);
            idx++;
        }

        Label title = makeLbl("THE DECK", "#ffcc00",
            (int) Math.max(13, curBoardSide * TXT_CARD_NAME_FRAC), true);
        title.setAlignment(Pos.CENTER);

        Button closeBtn = styledOverlayButton("CLOSE", "rgba(30,30,30,0.90)", "#aaaaaa");
        closeBtn.setOnAction(e -> dismissCardDeckOverlay());

        Label hint = makeLbl("tap outside to dismiss", "#444444", 9, false);
        hint.setStyle(hint.getStyle() + "-fx-font-style:italic;");

        VBox panel = new VBox(16, title, row, closeBtn, hint);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(24, 30, 20, 30));
        panel.setMaxWidth(curW * 0.95);
        panel.setStyle(
            "-fx-background-color:linear-gradient(to bottom,rgba(12,12,18,0.96),rgba(4,4,10,0.98));" +
            "-fx-background-radius:18;" +
            "-fx-border-color:#2a2a2a;" +
            "-fx-border-width:1.5;" +
            "-fx-border-radius:18;");
        addDropShadow(panel, 40, Color.BLACK);
        panel.setOnMouseClicked(javafx.event.Event::consume);

        cardDeckDimLayer = new StackPane(panel);
        cardDeckDimLayer.setStyle("-fx-background-color:rgba(0,0,0,0.55);");
        cardDeckDimLayer.setPickOnBounds(true);
        cardDeckDimLayer.setOpacity(0);
        cardDeckDimLayer.setOnMouseClicked(e -> {
            if (e.getTarget() == cardDeckDimLayer) dismissCardDeckOverlay();
        });

        backgroundRoot.getChildren().add(cardDeckDimLayer);
        cardDeckDimLayer.toFront();

        // ── Blur + dim fade in, cards spread out from centre in a
        //    staggered cascade so the deck visibly "fans open". ─────────
        masterLayout.setEffect(worldBlur);
        Timeline blurIn = new Timeline(
            new KeyFrame(Duration.ZERO,       ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0); }),
            new KeyFrame(Duration.millis(350), ev -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }));
        FadeTransition dimFade = new FadeTransition(Duration.millis(300), cardDeckDimLayer);
        dimFade.setFromValue(0); dimFade.setToValue(1);

        ParallelTransition cardsSpread = new ParallelTransition();
        for (int i = 0; i < entries.size(); i++) {
            Node entry = entries.get(i);
            double angle = entryAngles.get(i);
            double lift  = entryLifts.get(i);

            PauseTransition delay = new PauseTransition(Duration.millis(i * 45));

            TranslateTransition moveX = new TranslateTransition(Duration.millis(380), entry);
            moveX.setToX(0);
            moveX.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition moveY = new TranslateTransition(Duration.millis(380), entry);
            moveY.setToY(lift);
            moveY.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition rot = new RotateTransition(Duration.millis(380), entry);
            rot.setToAngle(angle);
            rot.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), entry);
            fadeIn.setToValue(1);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(380), entry);
            scaleUp.setToX(1); scaleUp.setToY(1);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);

            cardsSpread.getChildren().add(new SequentialTransition(
                delay, new ParallelTransition(moveX, moveY, rot, fadeIn, scaleUp)));
        }

        new ParallelTransition(blurIn, dimFade, cardsSpread).play();
    }

    private void dismissCardDeckOverlay() {
        if (cardDeckDimLayer == null) return;
        Timeline blurOut = new Timeline(
            new KeyFrame(Duration.ZERO,       ev -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }),
            new KeyFrame(Duration.millis(280), ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0);
                                                        masterLayout.setEffect(null); }));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(240), cardDeckDimLayer);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        final StackPane layer = cardDeckDimLayer;
        fadeOut.setOnFinished(ev -> {
            backgroundRoot.getChildren().remove(layer);
            cardDeckOverlayVisible = false;
            cardDeckDimLayer = null;
        });
        new ParallelTransition(blurOut, fadeOut).play();
    }

    private Button styledOverlayButton(String text, String bgColor, String fgColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-font-size:11px;" +
            "-fx-font-weight:bold;" +
            "-fx-background-color:" + bgColor + ";" +
            "-fx-text-fill:" + fgColor + ";" +
            "-fx-border-color:" + fgColor + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:6;" +
            "-fx-background-radius:6;" +
            "-fx-cursor:hand;" +
            "-fx-padding:6 16;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.80));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
        return btn;
    }

    // =========================================================
    //  CARD OVERLAY
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
            l.setWrapText(true); l.setAlignment(Pos.CENTER);
        }

        Label hint = makeLbl("tap to continue", "#888888", 10, false);
        hint.setStyle(hint.getStyle() + "-fx-font-style: italic;");

        cardOverlay.getChildren().addAll(cardStack, cardOverlayName, cardOverlayDesc, cardOverlayEffect, hint);
        cardOverlay.setOnMouseClicked(e -> dismissCardOverlay());
        backgroundRoot.getChildren().add(cardOverlay);
        StackPane.setAlignment(cardOverlay, Pos.CENTER);
    }

    private void showCardOverlay(Card card) {
    	SoundManager.getInstance().playCardDraw();
        cardOverlayName.setText(card.getName());
        cardOverlayDesc.setText(card.getDescription());
        cardOverlayEffect.setText(cardEffect(card.getName()));
        cardOverlayFace.setImage(cardFace(card.getName()));
        cardOverlayBack.setOpacity(1); cardOverlayBack.setVisible(true);
        cardOverlayBack.setRotate(0);
        cardOverlayFace.setOpacity(0); cardOverlayFace.setVisible(false);
        cardOverlayFace.setRotate(0);

        // cardOverlay itself stays hidden until the flight animation lands —
        // the flying copy is what the user sees travelling from the deck.
        cardOverlay.setOpacity(0);
        cardOverlay.setVisible(false);

        playCardDrawFlight(() -> revealCardOverlay());
    }

    /**
     * Spawns a temporary card-back ImageView at the deck's current on-screen
     * position and animates it flying to the centre of the board, growing to
     * roughly the overlay's target size along the way, with a gentle rotation
     * for flair. Purely a "travel" animation — does not touch cardOverlay or
     * any of its children. Calls {@code onArrived} once the flight finishes,
     * which is where the blur/flip reveal choreography takes over.
     */
    private void playCardDrawFlight(Runnable onArrived) {
        if (cardDeckView == null || cardBack == null) {
            onArrived.run();
            return;
        }

        // Deck's current on-screen bounds, converted into backgroundRoot's
        // local coordinate space (backgroundRoot is the common StackPane
        // ancestor for both the deck and the overlay).
        javafx.geometry.Bounds deckBoundsInScene = cardDeckView.localToScene(cardDeckView.getBoundsInLocal());
        javafx.geometry.Point2D deckCenterLocal = backgroundRoot.sceneToLocal(
            (deckBoundsInScene.getMinX() + deckBoundsInScene.getMaxX()) / 2,
            (deckBoundsInScene.getMinY() + deckBoundsInScene.getMaxY()) / 2);

        double rootW = backgroundRoot.getWidth();
        double rootH = backgroundRoot.getHeight();
        double centerX = rootW / 2.0;
        double centerY = rootH / 2.0;

        // Target size roughly matches the card overlay's current back-image size,
        // falling back to a sane default if layout hasn't run yet.
        double targetW = cardOverlayBack.getFitWidth() > 0 ? cardOverlayBack.getFitWidth() : 160;

        ImageView flying = new ImageView(cardBack);
        flying.setPreserveRatio(true);
        flying.setFitWidth(Math.max(40, deckBoundsInScene.getWidth() * 0.9));
        addDropShadow(flying, 18, Color.BLACK);

        // Position it at the deck's centre using translate offsets from the
        // StackPane's own centre (StackPane children default to centred).
        flying.setTranslateX(deckCenterLocal.getX() - centerX);
        flying.setTranslateY(deckCenterLocal.getY() - centerY);
        flying.setRotate(-8);
        flying.setOpacity(0);

        backgroundRoot.getChildren().add(flying);
        flying.toFront();

        FadeTransition popIn = new FadeTransition(Duration.millis(120), flying);
        popIn.setFromValue(0); popIn.setToValue(1);

        TranslateTransition fly = new TranslateTransition(Duration.millis(480), flying);
        fly.setToX(0); fly.setToY(0);
        fly.setInterpolator(Interpolator.EASE_BOTH);

        double scaleFactor = targetW / flying.getFitWidth();
        ScaleTransition grow = new ScaleTransition(Duration.millis(480), flying);
        grow.setToX(scaleFactor); grow.setToY(scaleFactor);
        grow.setInterpolator(Interpolator.EASE_BOTH);

        RotateTransition straighten = new RotateTransition(Duration.millis(480), flying);
        straighten.setToAngle(0);
        straighten.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition travel = new ParallelTransition(fly, grow, straighten);

        SequentialTransition flight = new SequentialTransition(popIn, travel);
        flight.setOnFinished(e -> {
            backgroundRoot.getChildren().remove(flying);
            onArrived.run();
        });
        flight.play();
    }

    /**
     * Runs the blur-in + overlay fade-in, then performs a real Y-axis card
     * flip: the back rotates out of view, swaps to the front at the 90°
     * midpoint, then the front rotates in. Timing matches the original
     * cross-fade duration; only the back→front transition mechanic changed.
     */
    /**
     * Runs the blur-in + overlay fade-in, then performs a real Y-axis card
     * flip immediately after — no dead pause between arrival and flip, so
     * the flight and the flip read as one continuous motion.
     */
    private void revealCardOverlay() {
        cardOverlay.setVisible(true);

        masterLayout.setEffect(worldBlur);
        Timeline blurIn = new Timeline(
            new KeyFrame(Duration.millis(0),   e -> { worldBlur.setWidth(0);  worldBlur.setHeight(0); }),
            new KeyFrame(Duration.millis(250), e -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }));
        FadeTransition cardIn = new FadeTransition(Duration.millis(200), cardOverlay);
        cardIn.setFromValue(0); cardIn.setToValue(1);

        ParallelTransition arrive = new ParallelTransition(blurIn, cardIn);
        arrive.setOnFinished(e -> {
            // Real card-flip: rotate the back out on the Y axis, swap to the
            // front at the 90° midpoint, then rotate the front in. Starts
            // immediately — no pause — so it flows straight from the flight.
            cardOverlayFace.setOpacity(1);
            cardOverlayFace.setVisible(false);
            cardOverlayBack.setVisible(true);
            cardOverlayBack.setOpacity(1);

            RotateTransition flipOutBack = new RotateTransition(Duration.millis(220), cardOverlayBack);
            flipOutBack.setAxis(javafx.scene.transform.Rotate.Y_AXIS);
            flipOutBack.setFromAngle(0);
            flipOutBack.setToAngle(90);
            flipOutBack.setInterpolator(Interpolator.EASE_IN);
            flipOutBack.setOnFinished(ev -> {
                cardOverlayBack.setVisible(false);
                cardOverlayFace.setVisible(true);
                cardOverlayFace.setRotate(-90);

                RotateTransition flipInFace = new RotateTransition(Duration.millis(220), cardOverlayFace);
                flipInFace.setAxis(javafx.scene.transform.Rotate.Y_AXIS);
                flipInFace.setFromAngle(-90);
                flipInFace.setToAngle(0);
                flipInFace.setInterpolator(Interpolator.EASE_OUT);
                flipInFace.play();
            });
            flipOutBack.play();
        });
        arrive.play();
    }

    private void dismissCardOverlay() {
        Timeline blurOut = new Timeline(
            new KeyFrame(Duration.millis(0),   e -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }),
            new KeyFrame(Duration.millis(350), e -> { worldBlur.setWidth(0);  worldBlur.setHeight(0);
                                                       masterLayout.setEffect(null); }));
        FadeTransition cardOut = new FadeTransition(Duration.millis(280), cardOverlay);
        cardOut.setFromValue(1); cardOut.setToValue(0);
        cardOut.setOnFinished(e -> {
            cardOverlay.setVisible(false);
            // Reset flip rotation so the next card draw starts clean.
            cardOverlayBack.setRotate(0);
            cardOverlayFace.setRotate(0);
            refreshBoard(); updateUI();
        });
        new ParallelTransition(blurOut, cardOut).play();
    }
    // =========================================================
    //  MESSAGE / EXCEPTION OVERLAY  (confirm dialog + error alert)
    // =========================================================
    private void buildMessageOverlay() {
        messageOverlay = new StackPane();
        messageOverlay.setStyle("-fx-background-color:rgba(0,0,0,0.62);");
        messageOverlay.setVisible(false);
        messageOverlay.setOpacity(0);
        messageOverlay.setPickOnBounds(true);
        // Fill the full window (letterbox included). Do NOT apply the HUD
        // uniform scale — that was shrinking the dim layer away from the edges.
        messageOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        messageBox = new VBox(14);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.getStyleClass().add("game-modal-panel");

        messageIcon  = makeLbl("", "#ffffff", 22, true);
        messageIcon.setAlignment(Pos.CENTER);
        messageTitle = makeLbl("", "#ffffff", 16, true);
        messageTitle.setAlignment(Pos.CENTER);
        messageTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageBody  = makeLbl("", "#cccccc", 12, false);
        messageBody.setWrapText(true);
        messageBody.setAlignment(Pos.CENTER);
        messageBody.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageSep = new javafx.scene.shape.Rectangle(200, 1.5);
        messageSep.setFill(Color.web("#555555"));
        messageButtons = new HBox(14);
        messageButtons.setAlignment(Pos.CENTER);

        messageBox.getChildren().addAll(
            messageIcon, messageTitle, messageSep, messageBody, messageButtons);
        messageOverlay.getChildren().add(messageBox);
        StackPane.setAlignment(messageBox, Pos.CENTER);

        messageOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() != messageOverlay) return;
            // Backdrop click: cancel confirm / close alert
            hideMessageOverlay();
        });
        messageBox.setOnMouseClicked(javafx.event.Event::consume);

        backgroundRoot.getChildren().add(messageOverlay);
        messageOverlay.toFront();
    }

    /**
     * Shows a centered arcade modal for errors (invalid move, out of energy, …)
     * or confirm dialogs (power-up). Panel size/fonts track the live window
     * via {@link #layoutMessageOverlay()}.
     *
     * @param title     headline text
     * @param body      detail text
     * @param onConfirm callback for CONFIRM; null for error/alert mode
     * @param confirm   true = CONFIRM + CLOSE, false = CLOSE only
     */
    private void showMessageOverlay(String title, String body,
            Runnable onConfirm, boolean confirm) {
        if (messageOverlay == null) return;

        // Cancel any overlay transition in flight (e.g. a confirm dialog still
        // fading out) so it can't sneak in later and hide content we're about
        // to show — this is what was killing the insufficient-energy alert.
        final int myToken = ++messageOverlayToken;
        if (messageOverlayTransition != null) messageOverlayTransition.stop();

        String iconText, titleColor, borderColor, boxBg;
        if (confirm) {
            iconText    = ">>";
            titleColor  = "#ffdd55";
            borderColor = "#c9a227";
            boxBg       = "linear-gradient(to bottom,rgba(18,18,28,0.98),rgba(8,8,14,0.99))";
        } else {
            iconText    = "!";
            titleColor  = "#ff6b6b";
            borderColor = "#aa4444";
            boxBg       = "linear-gradient(to bottom,rgba(28,12,12,0.98),rgba(14,4,4,0.99))";
        }

        applyModalPanelStyle(messageBox, borderColor, boxBg);
        addDropShadow(messageBox, 44, Color.BLACK);

        messageIcon.setText(iconText);
        messageIcon.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-font-size:22px;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:" + titleColor + ";" +
            "-fx-alignment:center;");

        messageTitle.setText(title != null ? title.toUpperCase() : "");
        messageTitle.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-font-size:16px;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:" + titleColor + ";" +
            "-fx-alignment:center;");

        messageSep.setFill(Color.web(borderColor));

        messageBody.setText(body != null ? body : "");
        messageBody.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-font-size:12px;" +
            "-fx-text-fill:#bbbbbb;" +
            "-fx-alignment:center;");

        messageButtons.getChildren().clear();
        if (confirm) {
            Button ok = overlayActionButton("CONFIRM", "#1a1a12", "#ffdd55", "#c9a227");
            ok.setOnAction(e -> {
                hideMessageOverlay();
                if (onConfirm != null) onConfirm.run();
            });
            Button close = overlayActionButton("CLOSE", "#dddddd", "#1a1a1a", "#666666");
            close.setOnAction(e -> hideMessageOverlay());
            messageButtons.getChildren().addAll(ok, close);
        } else {
            Button close = overlayActionButton("CLOSE", "#dddddd", "#1a1a1a", "#888888");
            close.setOnAction(e -> hideMessageOverlay());
            messageButtons.getChildren().add(close);
        }

        layoutMessageOverlay();

        messageOverlay.setVisible(true);
        messageOverlay.toFront();
        messageOverlay.setOpacity(0);
        messageBox.setTranslateX(0);
        messageBox.setScaleX(0.90); messageBox.setScaleY(0.90);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), messageOverlay);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(220), messageBox);
        scaleIn.setFromX(0.90); scaleIn.setFromY(0.90);
        scaleIn.setToX(1.0);   scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition arrive = new ParallelTransition(fadeIn, scaleIn);
        arrive.setOnFinished(e -> {
            if (myToken != messageOverlayToken) return;
            if (!confirm) GameAnimationHelper.shakeNode(messageBox);
        });
        messageOverlayTransition = arrive;
        arrive.play();
    }

    /** Live proportional layout for the message modal (open + window resize). */
    private void layoutMessageOverlay() {
        if (messageOverlay == null || messageBox == null) return;
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W <= 0 || H <= 0) return;

        double scale = overlayScale();
        double boardSide = REF_BOARD_SIZE * scale;
        // Compact centered dialogue — modest rectangle, never edge-hugging
        double panelW = clampModalSize(
            Math.min(Math.min(W * 0.28, H * 0.50), boardSide * 0.48),
            220 * scale, 340 * scale);
        double panelH = clampModalSize(
            Math.min(H * 0.48, boardSide * 0.55),
            160 * scale, 320 * scale);
        double titlePx = Math.max(13, panelW * 0.055);
        double bodyPx  = Math.max(11, panelW * 0.042);
        double iconPx  = Math.max(16, panelW * 0.070);
        double btnPx   = Math.max(10, panelW * 0.045);
        double pad     = Math.max(16, panelW * 0.07);

        messageBox.setMinWidth(panelW * 0.92);
        messageBox.setMaxWidth(panelW);
        messageBox.setPrefWidth(panelW);
        messageBox.setMaxHeight(panelH);
        messageBox.setPadding(new Insets(pad * 1.1, pad * 1.2, pad, pad * 1.2));
        messageBody.setMaxWidth(panelW * 0.86);
        messageSep.setWidth(panelW * 0.62);

        setFontSize(messageIcon, iconPx);
        setFontSize(messageTitle, titlePx);
        setFontSize(messageBody, bodyPx);
        for (Node n : messageButtons.getChildren()) {
            if (n instanceof Button) restyleOverlayButtonFont((Button) n, btnPx);
        }
    }

    /** Clamps a modal dimension so it scales with the window but stays inset. */
    private static double clampModalSize(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private double overlayScale() {
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W <= 0 || H <= 0) return 1.0;
        return Math.min(W / REF_W, H / REF_H);
    }

    private void applyModalPanelStyle(Region panel, String borderColor, String boxBg) {
        panel.setStyle(
            "-fx-background-color:" + boxBg + ";" +
            "-fx-background-radius:14;" +
            "-fx-border-color:" + borderColor + ";" +
            "-fx-border-radius:14;" +
            "-fx-border-width:2;");
    }

    /** Flat arcade button used by message + monster modals. */
    private Button overlayActionButton(String text, String fgColor, String bgColor, String borderColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-font-size:12px;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:" + fgColor + ";" +
            "-fx-background-color:" + bgColor + ";" +
            "-fx-border-color:" + borderColor + ";" +
            "-fx-border-width:1.5;" +
            "-fx-border-radius:8;" +
            "-fx-background-radius:8;" +
            "-fx-padding:9 26;" +
            "-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.78));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
        return btn;
    }

    private void restyleOverlayButtonFont(Button btn, double fontPx) {
        if (btn == null) return;
        String style = btn.getStyle() == null ? "" : btn.getStyle();
        if (style.matches("(?s).*-fx-font-size:\\s*[0-9.]+px;.*")) {
            style = style.replaceAll("-fx-font-size:\\s*[0-9.]+px;",
                "-fx-font-size: " + fontPx + "px;");
        } else {
            style = style + "-fx-font-size: " + fontPx + "px;";
        }
        double padV = Math.max(7, fontPx * 0.75);
        double padH = Math.max(18, fontPx * 2.1);
        if (style.matches("(?s).*-fx-padding:\\s*[0-9.]+\\s+[0-9.]+;.*")) {
            style = style.replaceAll("-fx-padding:\\s*[0-9.]+\\s+[0-9.]+;",
                "-fx-padding:" + padV + " " + padH + ";");
        }
        btn.setStyle(style);
    }

    private void hideMessageOverlay() {
        if (messageOverlay == null) return;
        final int myToken = ++messageOverlayToken;
        if (messageOverlayTransition != null) messageOverlayTransition.stop();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), messageOverlay);
        fadeOut.setFromValue(messageOverlay.getOpacity()); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            if (myToken != messageOverlayToken) return;
            messageOverlay.setVisible(false);
            messageButtons.getChildren().clear();
        });
        messageOverlayTransition = fadeOut;
        fadeOut.play();
    }

    private void showConfirmDialog(String title, String header, String content, Runnable onConfirm) {
        String body = (content != null && !content.isEmpty())
            ? ((header != null && !header.isEmpty()) ? header + "\n" + content : content)
            : header;
        showMessageOverlay(title, body, onConfirm, true);
    }

    private void showErrorAlert(String title, String msg) {
        showMessageOverlay(title, msg != null ? msg : "Unknown error", null, false);
    }

    // =========================================================
    //  RESPONSIVE LAYOUT
    // =========================================================
    private void setupResponsiveLayout() {
        // masterLayout and controlPanelView are no longer bound to fill
        // the entire window. They're sized to a fixed 1280x720-scaled
        // box every layout pass and centered by backgroundRoot (a
        // StackPane centers its children by default) — this is what
        // keeps every element scaling uniformly on both axes together
        // instead of independently distorting on odd aspect ratios.
        backgroundRoot.widthProperty().addListener((obs, old, val) -> applyAllLayout());
        backgroundRoot.heightProperty().addListener((obs, old, val) -> applyAllLayout());
    }

    /**
     * Wires up a real three-state button image: the normal image, a
     * "half-pressed" image while the mouse hovers (not clicked), and a
     * fully "pressed" image while the mouse button is actually held
     * down. Whichever image applies is recalculated from the button's
     * OWN current hover/press state, so it's correct no matter which
     * order the mouse events arrive in (e.g. releasing while still
     * hovering goes back to half-pressed, not all the way to normal).
     *
     * Also restores a hover glow (the visual feedback the old opacity-
     * dim hover effect used to give, before it was replaced by the
     * image swap) so hovering still reads clearly even on top of a
     * half-pressed image.
     *
     * If a real "pressed" image isn't available or fails to load —
     * e.g. an AI-generated asset that came out wrong — this falls back
     * to darkening + slightly shrinking whatever image IS showing via a
     * runtime ColorAdjust + scale, instead of silently doing nothing.
     * That reads as "pushed in" without needing pixel-perfect matching
     * art, and costs nothing to keep even after real pressed art is
     * ready (it just stops being used the moment pressedImg loads).
     */
    private void setupPressStates(ImageView btn, String normalPath, String halfPath, String pressedPath, String glowColor) {
        if (btn == null) return;
        Image normalImg   = loadImage(normalPath);
        Image halfImg     = loadImage(halfPath);
        Image pressedImg  = loadImage(pressedPath);

        DropShadow hoverGlow = new DropShadow();
        hoverGlow.setColor(Color.web(glowColor));
        hoverGlow.setRadius(18);
        hoverGlow.setSpread(0.45);

        ColorAdjust pressedDarken = new ColorAdjust();
        pressedDarken.setBrightness(-0.28);

        final boolean[] hovering = {false};
        final boolean[] pressed  = {false};

        Runnable refresh = () -> {
            if (pressed[0]) {
                if (pressedImg != null) {
                    btn.setImage(pressedImg);
                    btn.setEffect(null);
                } else {
                    // Fallback: no usable pressed art yet — darken whatever
                    // is currently showing instead of leaving press with no
                    // visible feedback at all.
                    btn.setImage(hovering[0] && halfImg != null ? halfImg : normalImg);
                    btn.setEffect(pressedDarken);
                }
                btn.setScaleX(0.95); btn.setScaleY(0.95);
                return;
            }
            btn.setScaleX(1.0); btn.setScaleY(1.0);
            if (hovering[0]) {
                btn.setImage(halfImg != null ? halfImg : normalImg);
                btn.setEffect(hoverGlow);
            } else {
                btn.setImage(normalImg);
                btn.setEffect(null);
            }
        };

        btn.setOnMouseEntered(e -> { hovering[0] = true;  refresh.run(); });
        btn.setOnMouseExited(e  -> { hovering[0] = false; refresh.run(); });
        btn.setOnMousePressed(e -> { pressed[0]  = true;  refresh.run(); });
        btn.setOnMouseReleased(e -> { pressed[0] = false; refresh.run(); });
    }

    private void offset(javafx.scene.Node n, double x, double y) {
        if (n == null) return;
        n.setTranslateX(x);
        n.setTranslateY(y);
    }

    private void applyAllLayout() {
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W == 0 || H == 0) return;

        // ── ONE uniform scale, applied as a REAL transform ──────────────
        // Everything below is laid out at the FIXED 1280x720 reference
        // size — the same numbers every single time, regardless of
        // window size — and then the whole composition is scaled as one
        // rigid unit via setScaleX/setScaleY at the bottom of this
        // method. This is what actually guarantees zero relative drift
        // between elements: it's not manual arithmetic doing the
        // resizing anymore (which had accumulated small inconsistencies
        // across several rounds of edits), it's JavaFX's own transform
        // math on a single subtree — which by definition can't shift
        // internal proportions. Enlarging really is now "just making
        // the picture bigger," exactly like scaling one flat image.
        double scale = Math.min(W / REF_W, H / REF_H);

        // Fixed reference size — literally never changes with window size.
        masterLayout.setMinWidth(REF_W);  masterLayout.setPrefWidth(REF_W);  masterLayout.setMaxWidth(REF_W);
        masterLayout.setMinHeight(REF_H); masterLayout.setPrefHeight(REF_H); masterLayout.setMaxHeight(REF_H);
        controlPanelView.setFitWidth(REF_W);
        controlPanelView.setFitHeight(REF_H);

        double barH       = REF_BAR_H;
        double topLabelH  = REF_TOP_LABEL_H;
        double panelW     = REF_PANEL_W;
        double inset      = 6;

        // ── Direct AnchorPane children of masterLayout ────────────────────
        AnchorPane.setLeftAnchor(myLabel, 0.0);
        AnchorPane.setRightAnchor(myLabel, 0.0);
        AnchorPane.setTopAnchor(myLabel, 0.0);
        offset(myLabel, OFFSET_X_TOP_LABEL, OFFSET_Y_TOP_LABEL);

        AnchorPane.setLeftAnchor(playerPanelContainer, 0.0);
        AnchorPane.setTopAnchor(playerPanelContainer, topLabelH);
        playerPanelContainer.setPrefWidth(panelW);
        playerPanelContainer.setMaxWidth(panelW);
        offset(playerPanelContainer, OFFSET_X_PLAYER_PANEL, OFFSET_Y_PLAYER_PANEL);

        AnchorPane.setRightAnchor(opponentPanelContainer, 0.0);
        AnchorPane.setTopAnchor(opponentPanelContainer, topLabelH);
        opponentPanelContainer.setPrefWidth(panelW);
        opponentPanelContainer.setMaxWidth(panelW);
        offset(opponentPanelContainer, OFFSET_X_OPPONENT_PANEL, OFFSET_Y_OPPONENT_PANEL);

        AnchorPane.setLeftAnchor(actionLogContainer, 0.0);
        AnchorPane.setBottomAnchor(actionLogContainer, 0.0);
        actionLogContainer.setPrefWidth(panelW);
        actionLogContainer.setMaxWidth(panelW);
        offset(actionLogContainer, OFFSET_X_ACTION_LOG, OFFSET_Y_ACTION_LOG);

        AnchorPane.setLeftAnchor(boardContainer,   panelW + inset);
        AnchorPane.setRightAnchor(boardContainer,  panelW + inset);
        AnchorPane.setTopAnchor(boardContainer,    topLabelH + inset);
        AnchorPane.setBottomAnchor(boardContainer, barH + inset);
        offset(boardContainer, OFFSET_X_BOARD, OFFSET_Y_BOARD);

        AnchorPane.setLeftAnchor(controlBar,   0.0);
        AnchorPane.setRightAnchor(controlBar,  0.0);
        AnchorPane.setBottomAnchor(controlBar, 0.0);
        controlBar.setPrefWidth(REF_W);
        controlBar.setPrefHeight(barH);
        offset(controlBar, OFFSET_X_CONTROL_BAR, OFFSET_Y_CONTROL_BAR);

        double pad       = REF_PANEL_TOP_PAD;
        double padRight  = 4;
        double padBottom = 4;
        double padLeftP  = 8;
        double padLeftO  = 4;
        playerPanelContainer.setStyle(
            "-fx-padding: " + pad + " " + padRight + " " + padBottom + " " + padLeftP + ";");
        opponentPanelContainer.setStyle(
            "-fx-padding: " + pad + " " + padRight + " " + padBottom + " " + padLeftO + ";");

        // ── Panel text — panelW is now a fixed reference constant, so
        // these evaluate to fixed reference sizes too; the scale
        // transform at the bottom handles the actual on-screen size.
        double nameFontPx   = Math.max(9,  panelW * TXT_PLAYER_NAME_FRAC);
        double typeFontPx   = Math.max(8,  panelW * TXT_PLAYER_TYPE_FRAC);
        double roleFontPx   = Math.max(8,  panelW * TXT_PLAYER_ROLE_FRAC);
        double posFontPx    = Math.max(9,  panelW * TXT_PLAYER_POS_FRAC);
        double energyFontPx = Math.max(9,  panelW * TXT_PLAYER_ENERGY_FRAC);
        double statusFontPx = Math.max(8,  panelW * TXT_PLAYER_STATUS_FRAC);
        double turnFontPx   = Math.max(8,  panelW * TXT_PLAYER_TURN_FRAC);

        setFontSize(player.nameLbl,   nameFontPx);
        setFontSize(player.typeLbl,   typeFontPx);
        setFontSize(player.roleLbl,   roleFontPx);
        setFontSize(player.posLbl,    posFontPx + 2);
        setFontSize(player.energyLbl, energyFontPx);
        setFontSize(player.statusLbl, statusFontPx);
        setFontSize(player.turnLbl,   turnFontPx);

        setFontSize(opponent.nameLbl,   nameFontPx);
        setFontSize(opponent.typeLbl,   typeFontPx);
        setFontSize(opponent.roleLbl,   roleFontPx);
        setFontSize(opponent.posLbl,    posFontPx + 2);
        setFontSize(opponent.energyLbl, energyFontPx);
        setFontSize(opponent.statusLbl, statusFontPx);

        player.portrait.setFitWidth(panelW * PORTRAIT_W_MULT);
        opponent.portrait.setFitWidth(panelW * PORTRAIT_W_MULT);
        player.energyBar.setFitWidth(panelW * ENERGY_BAR_W_MULT);
        opponent.energyBar.setFitWidth(panelW * ENERGY_BAR_W_MULT);
        if (player.profileBg   != null) player.profileBg.setFitWidth(panelW * PROFILE_W_MULT);
        if (opponent.profileBg != null) opponent.profileBg.setFitWidth(panelW * PROFILE_W_MULT);

        // ── Panel-internal element offsets — same pair applies to both
        // player and opponent so the two panels stay mirrored ───────────
        offset(player.portraitPane,   OFFSET_X_PORTRAIT,   OFFSET_Y_PORTRAIT);
        offset(opponent.portraitPane, OFFSET_X_PORTRAIT,   OFFSET_Y_PORTRAIT);
        offset(player.posLbl,         OFFSET_X_POS_LABEL_PLAYER,   OFFSET_Y_POS_LABEL_PLAYER);
        offset(opponent.posLbl,       OFFSET_X_POS_LABEL_OPPONENT, OFFSET_Y_POS_LABEL_OPPONENT);
        offset(player.profilePane,    OFFSET_X_PROFILE,    OFFSET_Y_PROFILE);
        offset(opponent.profilePane,  OFFSET_X_PROFILE,    OFFSET_Y_PROFILE);
        offset(player.nameLbl,        OFFSET_X_NAME_LBL,   OFFSET_Y_NAME_LBL);
        offset(opponent.nameLbl,      OFFSET_X_NAME_LBL,   OFFSET_Y_NAME_LBL);
        offset(player.typeLbl,        OFFSET_X_TYPE_LBL,   OFFSET_Y_TYPE_LBL);
        offset(opponent.typeLbl,      OFFSET_X_TYPE_LBL,   OFFSET_Y_TYPE_LBL);
        offset(player.roleLbl,        OFFSET_X_ROLE_LBL,   OFFSET_Y_ROLE_LBL);
        offset(opponent.roleLbl,      OFFSET_X_ROLE_LBL,   OFFSET_Y_ROLE_LBL);
        offset(player.energyRow,      OFFSET_X_ENERGY_NUM, OFFSET_Y_ENERGY_NUM);
        offset(opponent.energyRow,    OFFSET_X_ENERGY_NUM, OFFSET_Y_ENERGY_NUM);
        offset(player.energyWrapper,  OFFSET_X_ENERGY_BAR_PLAYER,   OFFSET_Y_ENERGY_BAR_PLAYER);
        offset(opponent.energyWrapper,OFFSET_X_ENERGY_BAR_OPPONENT, OFFSET_Y_ENERGY_BAR_OPPONENT);
        offset(player.statusLbl,      OFFSET_X_STATUS_LBL, OFFSET_Y_STATUS_LBL);
        offset(opponent.statusLbl,    OFFSET_X_STATUS_LBL, OFFSET_Y_STATUS_LBL);
        offset(player.turnRow,        OFFSET_X_TURN_LBL,   OFFSET_Y_TURN_LBL); // opponent has no turn row

        if (actionLogBg != null) {
            double logW = panelW * ACTION_LOG_W_MULT;
            actionLogBg.setFitWidth(logW);

            Image logImg = actionLogBg.getImage();
            double logH = (logImg != null && logImg.getWidth() > 0)
                ? logW * (logImg.getHeight() / logImg.getWidth())
                : logW * 0.6; // fallback ratio if the image hasn't finished loading yet

            double topPad   = logH * ACTION_LOG_TOP_PAD_FRAC;
            double leftPad  = logW * ACTION_LOG_LEFT_PAD_FRAC;
            double rightPad = logW * ACTION_LOG_RIGHT_PAD_FRAC;
            double botPad   = logH * ACTION_LOG_BOTTOM_PAD_FRAC;

            if (actionLogTextBox != null)
                actionLogTextBox.setPadding(new Insets(topPad, rightPad, botPad, leftPad));

            double lineFontPx   = Math.max(9, logH * ACTION_LOG_FONT_FRAC);
            double maxTextWidth = Math.max(10, logW - leftPad - rightPad);

            String[] colors = { ACTION_LOG_LINE1_COLOR, ACTION_LOG_LINE2_COLOR, ACTION_LOG_LINE3_COLOR };
            Label[]  lines  = { actionLine1, actionLine2, actionLine3 };
            for (int i = 0; i < lines.length; i++) {
                Label l = lines[i];
                if (l == null) continue;
                l.setMaxWidth(maxTextWidth);
                l.setStyle(
                    "-fx-font-family: '" + FONT + "';" +
                    "-fx-font-size: " + lineFontPx + "px;" +
                    "-fx-text-fill: " + colors[i] + ";");
            }
        }

        double lightSz = panelW * LIGHT_SIZE_MULT;
        for (ImageView iv : new ImageView[]{
                player.turnOff,   player.turnOn,   player.confOff,   player.confOn,
                player.frzOff,    player.frzOn,    player.shldOff,   player.shldOn,
                player.pwrOff,    player.pwrOn,
                opponent.turnOff, opponent.turnOn, opponent.confOff, opponent.confOn,
                opponent.frzOff,  opponent.frzOn,  opponent.shldOff, opponent.shldOn,
                opponent.pwrOff,  opponent.pwrOn})
            if (iv != null) iv.setFitWidth(lightSz);

        // ── Lights row position — moves ONLY the lights, nothing else ──────
        // translateX/Y is a pure visual offset; it doesn't affect the VBox's
        // layout of its other children, so nudging this can't push the
        // portrait/profile/energy bar below it around. Change
        // REF_LIGHTS_OFFSET_X/Y below to move the whole row.
        if (player.lightsRow != null) {
            player.lightsRow.setTranslateX(REF_LIGHTS_OFFSET_X);
            player.lightsRow.setTranslateY(REF_LIGHTS_OFFSET_Y);
        }
        if (opponent.lightsRow != null) {
            opponent.lightsRow.setTranslateX(REF_LIGHTS_OFFSET_X);
            opponent.lightsRow.setTranslateY(REF_LIGHTS_OFFSET_Y);
        }

        // ── Board — fixed reference size, scaled along with everything else ──
        double boardSide = REF_BOARD_SIZE;
        boardHolderView.setFitWidth(boardSide * 1.10);
        boardHolderView.setFitHeight(boardSide * 1.10);
        boardImageView.setFitWidth(boardSide);
        boardImageView.setFitHeight(boardSide);
        grid.setMaxWidth(boardSide);  grid.setMaxHeight(boardSide);
        grid.setMinWidth(boardSide);  grid.setMinHeight(boardSide);

        double cardW = boardSide * CARD_W_MULT;
        if (cardOverlayBack != null) cardOverlayBack.setFitWidth(cardW);
        if (cardOverlayFace != null) cardOverlayFace.setFitWidth(cardW);
        if (cardOverlay     != null) cardOverlay.setMaxWidth(cardW + 60);

        double cardNamePx = Math.max(11, boardSide * TXT_CARD_NAME_FRAC);
        double cardBodyPx = Math.max(9,  boardSide * TXT_CARD_BODY_FRAC);
        setFontSize(cardOverlayName,   cardNamePx);
        setFontSize(cardOverlayDesc,   cardBodyPx);
        setFontSize(cardOverlayEffect, cardBodyPx);

        myLabel.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + Math.max(TXT_TOP_LABEL, REF_H * 0.025) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 0 3 0;" +
            "-fx-text-fill: #00ff88;");

        // ── Card deck ───────────────────────────────────────────────────
        cardDeckView.setFitWidth(REF_DECK_W);
        cardDeckView.setFitHeight(REF_DECK_H);
        AnchorPane.setLeftAnchor(cardDeckView, REF_DECK_LEFT);
        AnchorPane.setTopAnchor(cardDeckView,  REF_DECK_TOP);
        AnchorPane.setRightAnchor(cardDeckView,  null);
        AnchorPane.setBottomAnchor(cardDeckView, null);
        offset(cardDeckView, OFFSET_X_CARD_DECK, OFFSET_Y_CARD_DECK);

        // ── Dice ────────────────────────────────────────────────────────
        double diceSize = REF_DICE_SIZE;
        diceView.setFitWidth(diceSize); diceView.setFitHeight(diceSize);
        AnchorPane.setLeftAnchor(diceView,   (REF_W / 2) - (diceSize / 2) + 6);
        AnchorPane.setTopAnchor(diceView,    REF_DICE_TOP);
        AnchorPane.setRightAnchor(diceView,  null);
        AnchorPane.setBottomAnchor(diceView, null);
        offset(diceView, OFFSET_X_DICE, OFFSET_Y_DICE);

        // ── Buttons ─────────────────────────────────────────────────────
        double btnW = REF_BTN_W;
        double btnH = REF_BTN_H;
        powerUpImageBtn.setFitWidth(btnW); powerUpImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(powerUpImageBtn, REF_POWERUP_RIGHT);
        AnchorPane.setTopAnchor(powerUpImageBtn,   REF_BTN_TOP);
        AnchorPane.setLeftAnchor(powerUpImageBtn,  null);
        AnchorPane.setBottomAnchor(powerUpImageBtn,null);
        offset(powerUpImageBtn, OFFSET_X_POWERUP_BTN, OFFSET_Y_POWERUP_BTN);

        rollImageBtn.setFitWidth(btnW); rollImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(rollImageBtn, REF_ROLL_RIGHT);
        AnchorPane.setTopAnchor(rollImageBtn,   REF_BTN_TOP);
        AnchorPane.setLeftAnchor(rollImageBtn,  null);
        AnchorPane.setBottomAnchor(rollImageBtn,null);
        offset(rollImageBtn, OFFSET_X_ROLL_BTN, OFFSET_Y_ROLL_BTN);

        // ── THE scale transform ────────────────────────────────────────
        // masterLayout and controlPanelView are separate StackPane
        // children of backgroundRoot, each built at the fixed 1280x720
        // reference size above — StackPane centers each independently,
        // so applying the identical scale to both keeps them perfectly
        // coincident. cardOverlay is also a top-level child (so blur on
        // masterLayout never touches it) and gets the same treatment.
        // messageOverlay / monsterDimLayer intentionally fill the FULL
        // window and are NOT scaled — they re-layout with percentage
        // widths so the dim backdrop always covers letterbox bars too.
        masterLayout.setScaleX(scale);
        masterLayout.setScaleY(scale);
        controlPanelView.setScaleX(scale);
        controlPanelView.setScaleY(scale);
        if (cardOverlay != null) {
            cardOverlay.setScaleX(scale);
            cardOverlay.setScaleY(scale);
        }
        if (messageOverlay != null && messageOverlay.isVisible()) {
            layoutMessageOverlay();
        }
        if (monsterDimLayer != null && monsterOverlayVisible) {
            layoutMonsterOverlayPanel();
        }
        if (optionsDimLayer != null && optionsMenuVisible) {   // ADD THIS
            layoutOptionsMenuPanel();
        }
    }

    // =========================================================
    //  PARALLAX
    // =========================================================
    private void setupParallax() {
        parallaxTimer = new AnimationTimer() {
            @Override public void handle(long now) {
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
    //  WINNER CHECK
    // =========================================================
    private void checkWinner() {
        if (game.getWinner() == null) return;
        Monster winner = game.getWinner();
        Monster p      = game.getPlayer();
        Monster o      = game.getOpponent();
        if (parallaxTimer != null) parallaxTimer.stop();
        myLabel.setText(winner.getName() + " WINS!");
        SceneManager.getInstance().switchToGameOverScreen(
            winner.getName(), winner.getRole().toString(), p.getRole(),
            p.getName(), p.getRole().toString(), p.getEnergy(),
            o.getName(), o.getRole().toString(), o.getEnergy());
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
        if (game == null || isAnimating || cardOverlay.isVisible() || cardDeckOverlayVisible) return;
        Monster current = game.getCurrent();
        if (e.getCode() == KeyCode.W) {
            current.setPosition(99);
            actionLine1.setText("CHEAT: WARPED!");
            actionLine2.setText(""); actionLine3.setText("");
            refreshBoard(); updateUI(); checkWinner();
            GameAnimationHelper.animateStationedMonsterPopups(
                true, 0, current.getRole(),
                game.getBoard().getBoardCells(), grid);
        } else if (e.getCode() == KeyCode.E) {
            int gained = 50;
            current.setEnergy(current.getEnergy() + gained);
            actionLine1.setText("CHEAT: +50 ENERGY!");
            actionLine2.setText(""); actionLine3.setText("");
            refreshBoard(); updateUI();
            GameAnimationHelper.animateStationedMonsterPopups(
                true, gained, current.getRole(),
                game.getBoard().getBoardCells(), grid);
        }
    }

    // =========================================================
    //  HELPER LOOKUPS
    // =========================================================

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
            default: return GameAnimationHelper.monsterSprite(name);
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
}