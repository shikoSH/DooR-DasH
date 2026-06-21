package game.gui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    @FXML private VBox       diceContainer;
    @FXML private ImageView  cardDeckView;
    @FXML private ImageView  diceView;
    @FXML private Label      diceResultLabel;
    @FXML private ImageView  powerUpImageBtn;
    @FXML private ImageView  rollImageBtn;
    @FXML private AnchorPane controlBar;
    @FXML private BorderPane masterLayout;

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
    //  MESSAGE OVERLAY
    // =========================================================
    private StackPane messageOverlay;
    private VBox      messageBox;
    private Label     messageTitle, messageBody;
    private HBox      messageButtons;
    private Animation messageOverlayTransition;   // NEW
    private int        messageOverlayToken = 0;   // NEW

    // =========================================================
    //  IMAGES
    // =========================================================
    private Image[] diceImages = new Image[6];
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
            controlPanelView.fitWidthProperty().bind(backgroundRoot.widthProperty());
            controlPanelView.fitHeightProperty().bind(backgroundRoot.heightProperty());

            setupParallax();

            javafx.beans.binding.NumberBinding boardSize =
                backgroundRoot.heightProperty().multiply(BOARD_SIZE_MULT);
            boardHolderView.setImage(loadImage(IMG_BOARD_HOLDER));
            boardHolderView.fitWidthProperty().bind(boardSize.multiply(1.10));
            boardHolderView.fitHeightProperty().bind(boardSize.multiply(1.10));
            boardImageView.setImage(loadImage(IMG_BOARD));
            boardImageView.fitWidthProperty().bind(boardSize);
            boardImageView.fitHeightProperty().bind(boardSize);
            grid.maxWidthProperty().bind(boardSize);
            grid.maxHeightProperty().bind(boardSize);
            grid.minWidthProperty().bind(boardSize);
            grid.minHeightProperty().bind(boardSize);

            // ── Delegate to helpers ──────────────────────────────────────
            boardRenderer = new GameBoardRenderer(grid, cellSize,
                this::handleMonsterCellClick, this::handleConveyorCellClick);
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
                addButtonHover(powerUpImageBtn);
            }
            if (rollImageBtn != null) {
                rollImageBtn.setImage(loadImage(IMG_ROLL_BTN));
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
        for (int i = 1; i <= 6; i++) diceImages[i-1] = loadImage("Dice_on_" + i + ".png");
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
        if (game == null || cardOverlay.isVisible() || isAnimating || monsterOverlayVisible) return;
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

            int newPos    = current.getPosition();
            int newEnergy = current.getEnergy();
            int newOppEng = opp.getEnergy();
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
            final int     fn   = newPos;
            final boolean[][] finalDoorSnap       = doorWasActivated;
            final Cell[][]    finalCells          = cells;
            final java.util.Map<String, Integer> finalStationedBefore = stationedBefore;

            if (diceTimeline != null) diceTimeline.stop();
            diceTimeline = GameAnimationHelper.animateDice(
                diceView, diceImages, diceResultLabel, diceFace,
                () -> GameAnimationHelper.animateMove(
                    fm, fo, fp, fn,
                    boardRenderer.getMonsterViews(),
                    boardRenderer.getSpotlightViews(),
                    grid,
                    () -> {
                        refreshBoard(); updateUI();

                        // Door-opening sound
                        int[] rc = GameAnimationHelper.toRowCol(fn);
                        Cell landed = finalCells[rc[0]][rc[1]];
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
                    }));

        } catch (game.engine.exceptions.InvalidMoveException ex) {
            actionLine1.setText("INVALID: " + ex.getMessage());
            actionLine2.setText("ROLL AGAIN!"); actionLine3.setText("");
            refreshBoard(); updateUI();
            isAnimating = false;
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
        if (game == null || cardOverlay.isVisible() || monsterOverlayVisible) return;
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
                    showErrorAlert("Not Enough Energy", msg);
                } catch (Exception ex) {
                    showErrorAlert("Powerup Failed",
                        ex.getMessage() != null ? ex.getMessage() : "Could not activate powerup.");
                }
            });
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
        if (row < 0 || row >= cells.length || col < 0 || col >= cells[0].length) return;

        Cell cell = cells[row][col];
        if (!(cell instanceof ConveyorBelt)) return;

        int destIndex = boardIndex + ((ConveyorBelt) cell).getEffect();
        destIndex = Math.max(0, Math.min(Constants.BOARD_SIZE - 1, destIndex));

        GameAnimationHelper.animateConveyorPointer(boardIndex, destIndex, grid);
    }

    // =========================================================
    //  MONSTER INFO OVERLAY  (card-style: back → flip reveal → stats)
    // =========================================================

    // Reusable card-back for the monster overlay (same as card deck back)
    private ImageView monsterOverlayBack;
    private ImageView monsterOverlayFace;
    private VBox      monsterOverlayCard;   // the stats panel revealed after flip
    private StackPane monsterDimLayer;

    /**
     * Shows the stationed-monster popup using the same blur+dim+card-flip
     * choreography as the card overlay.  Uses the screen portrait as the
     * "face" image, then reveals a clean dark stats panel.  No colored
     * highlights — neutral whites and greys only.
     */
    private void showMonsterInfoOverlay(Monster m) {
        monsterOverlayVisible = true;

        // ── Screen portrait — shown immediately, no card-back flip ────────
        monsterOverlayFace = new ImageView(screenPortrait(m.getName()));
        monsterOverlayFace.setPreserveRatio(true);
        monsterOverlayFace.setFitWidth(220);
        addDropShadow(monsterOverlayFace, 22, Color.BLACK);

        // ── Stats rows ────────────────────────────────────────────────────
        java.util.function.BiFunction<String, String, HBox> statRow = (key, val) -> {
            Label k = new Label(key);
            k.setStyle("-fx-font-family:'" + FONT + "';-fx-font-size:11px;-fx-text-fill:#999999;");
            Label v = new Label(val);
            v.setStyle("-fx-font-family:'" + FONT + "';-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#dddddd;");
            javafx.scene.layout.Region sp = new javafx.scene.layout.Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            HBox row = new HBox(k, sp, v);
            row.setSpacing(4);
            row.setAlignment(Pos.CENTER_LEFT);
            return row;
        };

        boolean confused = m.isConfused();
        StringBuilder statusSb = new StringBuilder();
        if (!m.isShielded() && !m.isFrozen() && !confused) statusSb.append("NORMAL");
        if (m.isShielded()) statusSb.append("SHIELD ");
        if (m.isFrozen())   statusSb.append("FROZEN ");
        if (confused)       statusSb.append("CONFUSED(").append(m.getConfusionTurns()).append("T)");

        javafx.scene.shape.Rectangle sep = new javafx.scene.shape.Rectangle(200, 1);
        sep.setFill(javafx.scene.paint.Color.web("#333333"));

        Label nameLbl = makeLbl(m.getName().toUpperCase(), "white", TXT_CARD_NAME, true);
        nameLbl.setAlignment(Pos.CENTER);

        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setMaxWidth(220);
        statsBox.getChildren().addAll(
            statRow.apply("TYPE",     m.getClass().getSimpleName()),
            statRow.apply("ROLE",     m.getRole().toString()),
            statRow.apply("ENERGY",   String.valueOf(m.getEnergy())),
            statRow.apply("POSITION", String.valueOf(m.getPosition())),
            statRow.apply("STATUS",   statusSb.toString())
        );

        Label descLbl = makeLbl(m.getDescription(), "#777777", TXT_CARD_BODY - 2, false);
        descLbl.setStyle(descLbl.getStyle() + "-fx-font-style:italic;");
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(220);
        descLbl.setAlignment(Pos.CENTER);

        // ── Close button only ─────────────────────────────────────────────
        Button closeBtn = styledOverlayButton("CLOSE", "rgba(30,30,30,0.90)", "#aaaaaa");
        closeBtn.setOnAction(e -> dismissMonsterOverlay());

        Label hint = makeLbl("tap outside to dismiss", "#444444", 9, false);
        hint.setStyle(hint.getStyle() + "-fx-font-style:italic;");

        monsterOverlayCard = new VBox(10,
            monsterOverlayFace, nameLbl, sep, statsBox, descLbl, closeBtn, hint);
        monsterOverlayCard.setAlignment(Pos.TOP_CENTER);
        monsterOverlayCard.setPadding(new Insets(20, 24, 18, 24));
        monsterOverlayCard.setMaxWidth(270);
        monsterOverlayCard.setStyle(
            "-fx-background-color:linear-gradient(to bottom,rgba(12,12,18,0.98),rgba(4,4,10,0.99));" +
            "-fx-background-radius:16;" +
            "-fx-border-color:#2a2a2a;" +
            "-fx-border-width:1.5;" +
            "-fx-border-radius:16;");
        addDropShadow(monsterOverlayCard, 40, Color.BLACK);

        // ── Dim layer ─────────────────────────────────────────────────────
        monsterDimLayer = new StackPane(monsterOverlayCard);
        monsterDimLayer.setStyle("-fx-background-color:rgba(0,0,0,0.55);");
        monsterDimLayer.setPickOnBounds(true);
        monsterDimLayer.setOpacity(0);
        monsterDimLayer.setOnMouseClicked(e -> {
            if (e.getTarget() == monsterDimLayer) dismissMonsterOverlay();
        });
        monsterOverlayCard.setOnMouseClicked(javafx.event.Event::consume);

        backgroundRoot.getChildren().add(monsterDimLayer);
        monsterDimLayer.toFront();

        // ── Blur + fade in with a gentle scale-up ─────────────────────────
        masterLayout.setEffect(worldBlur);
        Timeline blurIn = new Timeline(
            new KeyFrame(Duration.ZERO,       ev -> { worldBlur.setWidth(0);  worldBlur.setHeight(0); }),
            new KeyFrame(Duration.millis(350), ev -> { worldBlur.setWidth(14); worldBlur.setHeight(14); }));
        FadeTransition dimFade = new FadeTransition(Duration.millis(300), monsterDimLayer);
        dimFade.setFromValue(0); dimFade.setToValue(1);
        monsterOverlayCard.setScaleX(0.88); monsterOverlayCard.setScaleY(0.88);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), monsterOverlayCard);
        scaleIn.setFromX(0.88); scaleIn.setFromY(0.88);
        scaleIn.setToX(1.0);   scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(blurIn, dimFade, scaleIn).play();
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
        messageOverlay.setStyle("-fx-background-color:rgba(0,0,0,0.60);");
        messageOverlay.setVisible(false);
        messageOverlay.setOpacity(0);
        messageOverlay.setPickOnBounds(true);

        messageBox = new VBox(16);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(28, 32, 24, 32));
        messageBox.setMaxWidth(460);

        messageTitle   = makeLbl("", "#ffffff", 16, true);
        messageTitle.setAlignment(Pos.CENTER);
        messageBody    = makeLbl("", "#cccccc", 12, false);
        messageBody.setWrapText(true);
        messageBody.setMaxWidth(400);
        messageBody.setAlignment(Pos.CENTER);
        messageButtons = new HBox(16);
        messageButtons.setAlignment(Pos.CENTER);

        messageBox.getChildren().addAll(messageTitle, messageBody, messageButtons);
        messageOverlay.getChildren().add(messageBox);
        StackPane.setAlignment(messageBox, Pos.CENTER);
        backgroundRoot.getChildren().add(messageOverlay);
        messageOverlay.toFront();
    }

    /**
     * Shows a styled overlay for errors (InvalidMoveException, OutOfEnergyException, etc.)
     * or confirm dialogs (power-up).
     *
     * @param title     headline text
     * @param body      detail text
     * @param onConfirm callback for OK in confirm mode; null for error mode
     * @param confirm   true = confirm dialog (OK + CANCEL), false = error alert (OK only)
     */
    private void showMessageOverlay(String title, String body,
            Runnable onConfirm, boolean confirm) {
if (messageOverlay == null) return;

// Cancel any overlay transition in flight (e.g. a confirm dialog still
// fading out) so it can't sneak in later and hide content we're about
// to show — this is what was killing the insufficient-energy alert.
final int myToken = ++messageOverlayToken;
if (messageOverlayTransition != null) messageOverlayTransition.stop();

// ── Icon + colours per mode ───────────────────────────────────────
String iconText, titleColor, borderColor, boxBg;
if (confirm) {
iconText    = "⚡";
titleColor  = "#e0e0e0";
borderColor = "#444444";
boxBg       = "linear-gradient(to bottom,rgba(18,18,28,0.98),rgba(10,10,18,0.99))";
} else {
iconText    = "⚠";
titleColor  = "#ff6b6b";
borderColor = "#aa3333";
boxBg       = "linear-gradient(to bottom,rgba(28,10,10,0.98),rgba(14,4,4,0.99))";
}

messageBox.setStyle(
"-fx-background-color:" + boxBg + ";" +
"-fx-background-radius:18;" +
"-fx-border-color:" + borderColor + ";" +
"-fx-border-radius:18;" +
"-fx-border-width:1.5;");
addDropShadow(messageBox, 50, Color.BLACK);

Label iconLbl = makeLbl(iconText, titleColor, 28, true);
iconLbl.setAlignment(Pos.CENTER);

messageTitle.setText(title != null ? title.toUpperCase() : "");
messageTitle.setStyle(
"-fx-font-family:'" + FONT + "';" +
"-fx-font-size:16px;" +
"-fx-font-weight:bold;" +
"-fx-text-fill:" + titleColor + ";" +
"-fx-alignment:center;");

javafx.scene.shape.Rectangle sep = new javafx.scene.shape.Rectangle(320, 1);
sep.setFill(javafx.scene.paint.Color.web(borderColor));

messageBody.setText(body != null ? body : "");
messageBody.setStyle(
"-fx-font-family:'" + FONT + "';" +
"-fx-font-size:12px;" +
"-fx-text-fill:#aaaaaa;" +
"-fx-alignment:center;");

messageButtons.getChildren().clear();
messageBox.getChildren().setAll(iconLbl, messageTitle, sep, messageBody, messageButtons);

if (confirm) {
Button ok = overlayActionButton("CONFIRM", "#e0e0e0", "#1a1a2a");
ok.setOnAction(e -> { hideMessageOverlay(); if (onConfirm != null) onConfirm.run(); });
Button cancel = overlayActionButton("CANCEL", "#555555", "#111111");
cancel.setOnAction(e -> hideMessageOverlay());
messageButtons.getChildren().addAll(ok, cancel);
} else {
Button ok = overlayActionButton("DISMISS", "#e0e0e0", "#1a1a1a");
ok.setOnAction(e -> hideMessageOverlay());
messageButtons.getChildren().add(ok);
}

messageOverlay.setVisible(true);
messageOverlay.toFront();
messageOverlay.setOpacity(0);
messageBox.setTranslateX(0);
messageBox.setScaleX(0.88); messageBox.setScaleY(0.88);

FadeTransition fadeIn = new FadeTransition(Duration.millis(220), messageOverlay);
fadeIn.setFromValue(0); fadeIn.setToValue(1);
ScaleTransition scaleIn = new ScaleTransition(Duration.millis(220), messageBox);
scaleIn.setFromX(0.88); scaleIn.setFromY(0.88);
scaleIn.setToX(1.0);   scaleIn.setToY(1.0);
scaleIn.setInterpolator(Interpolator.EASE_OUT);

ParallelTransition arrive = new ParallelTransition(fadeIn, scaleIn);
arrive.setOnFinished(e -> {
if (myToken != messageOverlayToken) return;
// Give failed/blocked actions a clear "denied" cue.
if (!confirm) GameAnimationHelper.shakeNode(messageBox);
});
messageOverlayTransition = arrive;
arrive.play();
}

    /** Builds a flat, minimal button for the message overlay. */
    private Button overlayActionButton(String text, String fgColor, String bgColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-font-size:12px;" +
            "-fx-font-weight:bold;" +
            "-fx-text-fill:" + fgColor + ";" +
            "-fx-background-color:" + bgColor + ";" +
            "-fx-border-color:" + fgColor + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:8;" +
            "-fx-background-radius:8;" +
            "-fx-padding:8 28;" +
            "-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.75));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
        return btn;
    }

    private void styleOverlayButton(Button btn, String color) {
        // kept for any remaining callers — delegates to overlayActionButton styling
        btn.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-background-color:" + color + ";" +
            "-fx-text-fill:white;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:8;" +
            "-fx-padding:8 20;");
    }

    private void hideMessageOverlay() {
        if (messageOverlay == null) return;
        final int myToken = ++messageOverlayToken;
        if (messageOverlayTransition != null) messageOverlayTransition.stop();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), messageOverlay);
        fadeOut.setFromValue(messageOverlay.getOpacity()); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            if (myToken != messageOverlayToken) return;   // superseded — don't hide new content
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
        masterLayout.prefWidthProperty().bind(backgroundRoot.widthProperty());
        masterLayout.prefHeightProperty().bind(backgroundRoot.heightProperty());
        masterLayout.maxWidthProperty().bind(backgroundRoot.widthProperty());
        masterLayout.maxHeightProperty().bind(backgroundRoot.heightProperty());
        controlBar.prefWidthProperty().bind(backgroundRoot.widthProperty());
        backgroundRoot.widthProperty().addListener((obs, old, val) -> applyAllLayout());
        backgroundRoot.heightProperty().addListener((obs, old, val) -> applyAllLayout());
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
        playerPanelContainer.setStyle("-fx-padding: " + pad + " 4 4 8;");
        opponentPanelContainer.setStyle("-fx-padding: " + pad + " 4 4 4;");

        player.portrait.setFitWidth(panelW * PORTRAIT_W_MULT);
        opponent.portrait.setFitWidth(panelW * PORTRAIT_W_MULT);
        player.energyBar.setFitWidth(panelW * ENERGY_BAR_W_MULT);
        opponent.energyBar.setFitWidth(panelW * ENERGY_BAR_W_MULT);
        if (player.profileBg   != null) player.profileBg.setFitWidth(panelW * PROFILE_W_MULT);
        if (opponent.profileBg != null) opponent.profileBg.setFitWidth(panelW * PROFILE_W_MULT);

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
        AnchorPane.setLeftAnchor(cardDeckView, W * DECK_LEFT);
        AnchorPane.setTopAnchor(cardDeckView,  barH * DECK_TOP_FRAC);
        AnchorPane.setRightAnchor(cardDeckView,  null);
        AnchorPane.setBottomAnchor(cardDeckView, null);

        double diceSize = H * DICE_SIZE;
        diceView.setFitWidth(diceSize); diceView.setFitHeight(diceSize);
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
        powerUpImageBtn.setFitWidth(btnW); powerUpImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(powerUpImageBtn, W * POWERUP_RIGHT);
        AnchorPane.setTopAnchor(powerUpImageBtn,   barH * BTN_TOP_FRAC);
        AnchorPane.setLeftAnchor(powerUpImageBtn,  null);
        AnchorPane.setBottomAnchor(powerUpImageBtn,null);

        rollImageBtn.setFitWidth(btnW); rollImageBtn.setFitHeight(btnH);
        AnchorPane.setRightAnchor(rollImageBtn, W * ROLL_RIGHT);
        AnchorPane.setTopAnchor(rollImageBtn,   barH * BTN_TOP_FRAC);
        AnchorPane.setLeftAnchor(rollImageBtn,  null);
        AnchorPane.setBottomAnchor(rollImageBtn,null);


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
        if (game == null || isAnimating || cardOverlay.isVisible()) return;
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