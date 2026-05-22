package game.gui.controllers;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import game.engine.*;
import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.cells.DoorCell;
import game.engine.monsters.Monster;

/**
 * Thin coordinator — owns all {@code @FXML} fields (JavaFX requires them here)
 * and delegates every distinct responsibility to a focused helper class:
 *
 * <ul>
 *   <li>{@link ImageLoader}     — image loading and lookup</li>
 *   <li>{@link PanelBuilder}    — building side panels and the card overlay</li>
 *   <li>{@link BoardRenderer}   — cell/monster rendering</li>
 *   <li>{@link UIUpdater}       — pushing game state to labels</li>
 *   <li>{@link AnimationManager}— dice and movement animations</li>
 * </ul>
 *
 * No game logic or image data lives here — only wiring and the two
 * {@code @FXML} action handlers ({@link #handleRollDice()},
 * {@link #handlePowerUp()}) that need direct access to all the pieces.
 */
public class GameController {

    // =========================================================
    //  FXML FIELDS  (JavaFX injects these — do not rename)
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
    //  LAYOUT MULTIPLIERS — edit these to move/resize elements
    // =========================================================

    private static final double BOARD_SIZE_MULT = 0.77;
    private static final double CONTROL_BAR_MULT    = 0.16;
    private static final double SIDE_PANEL_WIDTH = 0.13;
    private static final double PORTRAIT_WIDTH_MULT = 0.78;
    private static final double ENERGY_BAR_H_MULT   = 0.13;
    private static final double PANEL_TOP_PAD_MULT  = 0.19;
    private static final double PANEL_PORTRAIT_DOWN_MULT = 0.024;
    private static final double BOARD_TOP_OFFSET_MULT = 0.052;

    private static final double DECK_LEFT_MULT      = 0.245;
    private static final double DECK_TOP_MULT       = -0.58;
    private static final double DECK_W_MULT         = 0.162;
    private static final double DECK_H_MULT         = 0.324;

    private static final double DICE_SIZE_MULT      = 0.27;
    private static final double DICE_TOP_MULT       = -0.55;

    private static final double BTN_W_MULT          = 0.15;
    private static final double BTN_H_MULT          = 0.15;
    private static final double BTN_TOP_MULT        = -0.46;
    private static final double ROLL_RIGHT_MULT     = 0.175;
    private static final double POWERUP_RIGHT_MULT  = 0.28;

    // =========================================================
    //  HELPER INSTANCES
    // =========================================================

    private ImageLoader    imageLoader;
    private PanelBuilder   panelBuilder;
    private BoardRenderer  boardRenderer;
    private UIUpdater      uiUpdater;
    private AnimationManager animMgr;

    // =========================================================
    //  STATE
    // =========================================================

    private Game   game;
    private boolean isAnimating = false;

    // ── Parallax ──────────────────────────────────────────────
    private double         targetX = 0, targetY = 0;
    private AnimationTimer parallaxTimer;

    // =========================================================
    //  INITIALIZE
    // =========================================================

    @FXML
    private void initialize() {
        try {
            // 1. Images first — everything else depends on them
            imageLoader = new ImageLoader();
            imageLoader.loadAll();

            // 2. Static background / control panel views
            backgroundView.setImage(imageLoader.backgroundGameImage);
            controlPanelView.setImage(imageLoader.controlPanelImage);

            // 3. Parallax
            setupParallax();

            // 4. Board grid
            boardRenderer = new BoardRenderer(imageLoader);
            boardRenderer.buildGrid(grid);

            // 5. Side panels + card overlay
            panelBuilder = new PanelBuilder(imageLoader);
            panelBuilder.buildPlayerPanel(playerPanelContainer);
            panelBuilder.buildOpponentPanel(opponentPanelContainer);
            panelBuilder.buildActionLog(actionLogContainer);
            panelBuilder.buildDicePanel(diceContainer);
            panelBuilder.buildCardOverlay(boardContainer);
            panelBuilder.buildMonsterOverlay(boardContainer);
            boardRenderer.setOnMonsterCellClick(monster -> {
                if (!panelBuilder.isAnyOverlayVisible()) {
                    panelBuilder.showMonsterOverlay(monster);
                }
            });

            // 6. UI updater — wires panel builder's labels into one place
            uiUpdater = new UIUpdater(
                imageLoader,
                panelBuilder.playerPortrait,    panelBuilder.playerEnergyBar,
                panelBuilder.playerNameLabel,   panelBuilder.playerTypeLabel,
                panelBuilder.playerOrigRoleLabel, panelBuilder.playerCurrRoleLabel,
                panelBuilder.playerIndexLabel,  panelBuilder.playerEnergyLabel,
                panelBuilder.playerStatusLabel, panelBuilder.playerTurnLabel, panelBuilder.playerSignals,
                panelBuilder.opponentPortrait,  panelBuilder.opponentEnergyBar,
                panelBuilder.opponentNameLabel, panelBuilder.opponentTypeLabel,
                panelBuilder.opponentOrigRoleLabel, panelBuilder.opponentCurrRoleLabel,
                panelBuilder.opponentIndexLabel, panelBuilder.opponentEnergyLabel,
                panelBuilder.opponentStatusLabel, panelBuilder.opponentSignals
            );

            // 7. Animation manager
            animMgr = new AnimationManager(imageLoader, boardRenderer);

            // 8. Button images + hover
            if (powerUpImageBtn != null) {
                powerUpImageBtn.setImage(imageLoader.powerUpButtonImage);
                powerUpImageBtn.setOnMouseEntered(e -> powerUpImageBtn.setOpacity(0.80));
                powerUpImageBtn.setOnMouseExited(e  -> powerUpImageBtn.setOpacity(1.00));
            }
            if (rollImageBtn != null) {
                rollImageBtn.setImage(imageLoader.rollButtonImage);
                rollImageBtn.setOnMouseEntered(e -> rollImageBtn.setOpacity(0.80));
                rollImageBtn.setOnMouseExited(e  -> rollImageBtn.setOpacity(1.00));
            }
            if (cardDeckView != null) cardDeckView.setImage(imageLoader.deckFull);
            if (diceView     != null) diceView.setImage(imageLoader.diceImages[0]);

            // 9. Cheat codes + responsive layout
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
    //  RESPONSIVE LAYOUT
    // =========================================================

    private void setupResponsiveLayout() {
        masterLayout.prefWidthProperty().bind(backgroundRoot.widthProperty());
        masterLayout.prefHeightProperty().bind(backgroundRoot.heightProperty());
        masterLayout.maxWidthProperty().bind(backgroundRoot.widthProperty());
        masterLayout.maxHeightProperty().bind(backgroundRoot.heightProperty());

        backgroundView.fitWidthProperty().bind(backgroundRoot.widthProperty());
        backgroundView.fitHeightProperty().bind(backgroundRoot.heightProperty());

        controlPanelView.fitWidthProperty().bind(backgroundRoot.widthProperty());
        controlPanelView.fitHeightProperty().bind(backgroundRoot.heightProperty());

        controlBar.prefWidthProperty().bind(backgroundRoot.widthProperty());

        javafx.beans.binding.NumberBinding centerWidth =
            backgroundRoot.widthProperty()
                .subtract(backgroundRoot.widthProperty().multiply(SIDE_PANEL_WIDTH * 2));
        javafx.beans.binding.NumberBinding boardFromHeight =
            backgroundRoot.heightProperty().multiply(BOARD_SIZE_MULT);
        javafx.beans.binding.NumberBinding boardFromWidth =
            centerWidth.multiply(0.92);
        javafx.beans.binding.NumberBinding boardSize =
            Bindings.min(boardFromHeight, boardFromWidth);

        boardHolderView.fitWidthProperty().bind(boardSize.multiply(1.12));
        boardHolderView.fitHeightProperty().bind(boardSize.multiply(1.12));
        boardHolderView.setImage(imageLoader.boardHolderImage);

        boardImageView.fitWidthProperty().bind(boardSize);
        boardImageView.fitHeightProperty().bind(boardSize);

        grid.prefWidthProperty().bind(boardSize);
        grid.prefHeightProperty().bind(boardSize);
        grid.maxWidthProperty().bind(boardSize);
        grid.maxHeightProperty().bind(boardSize);
        grid.minWidthProperty().bind(boardSize);
        grid.minHeightProperty().bind(boardSize);

        boardContainer.prefWidthProperty().bind(boardSize.multiply(1.12));
        boardContainer.prefHeightProperty().bind(boardSize.multiply(1.12));
        boardContainer.maxWidthProperty().bind(boardSize.multiply(1.12));
        boardContainer.maxHeightProperty().bind(boardSize.multiply(1.12));

        javafx.beans.value.ChangeListener<Number> onResize =
            (obs, old, val) -> applyAllLayout();
        backgroundRoot.widthProperty().addListener(onResize);
        backgroundRoot.heightProperty().addListener(onResize);
        controlBar.widthProperty().addListener(onResize);
        controlBar.heightProperty().addListener(onResize);
    }

    private void applyAllLayout() {
        double W = backgroundRoot.getWidth();
        double H = backgroundRoot.getHeight();
        if (W == 0 || H == 0) return;

        double barH = H * CONTROL_BAR_MULT;
        double boardTop = 6 + H * BOARD_TOP_OFFSET_MULT;
        BorderPane.setMargin(boardContainer, new Insets(boardTop, 6, barH + 6, 6));
        controlBar.setPrefHeight(barH);

        double panelW = W * SIDE_PANEL_WIDTH;
        playerPanelContainer.setPrefWidth(panelW);
        if (masterLayout.getRight() != null) {
            VBox rightPanel = (VBox) masterLayout.getRight();
            rightPanel.setPrefWidth(panelW);
            double pad = H * PANEL_TOP_PAD_MULT;
            rightPanel.setStyle("-fx-padding: " + pad + " 6 6 6;");
        }

        panelBuilder.applyPanelFontSize(panelW);

        double portraitW = panelW * PORTRAIT_WIDTH_MULT;
        double portraitDown = H * PANEL_PORTRAIT_DOWN_MULT;
        panelBuilder.playerPortrait.setFitWidth(portraitW);
        panelBuilder.opponentPortrait.setFitWidth(portraitW);
        panelBuilder.playerPortrait.setTranslateY(portraitDown);
        panelBuilder.opponentPortrait.setTranslateY(portraitDown);
        panelBuilder.playerEnergyBar.setFitWidth(portraitW);
        panelBuilder.opponentEnergyBar.setFitWidth(portraitW);

        double energyH = H * ENERGY_BAR_H_MULT * 1.65;
        panelBuilder.playerEnergyBar.setFitHeight(energyH);
        panelBuilder.opponentEnergyBar.setFitHeight(energyH);

        double pad = H * PANEL_TOP_PAD_MULT;
        playerPanelContainer.setStyle("-fx-padding: " + pad + " 4 4 4;");
        opponentPanelContainer.setStyle("-fx-padding: 4 4 4 4;");

        double centerW = Math.max(1, W - panelW * 2);
        double boardSide = Math.min(H * BOARD_SIZE_MULT, centerW * 0.92);
        panelBuilder.scaleCardOverlay(boardSide);
        panelBuilder.scaleMonsterOverlay(boardSide);

        // Sizes use full window (bar is only ~16% tall — bar-based sizing made deck/buttons tiny)
        cardDeckView.setFitWidth(W * DECK_W_MULT);
        cardDeckView.setFitHeight(H * DECK_H_MULT);
        AnchorPane.setLeftAnchor(cardDeckView,   W * DECK_LEFT_MULT);
        AnchorPane.setTopAnchor(cardDeckView,    barH * DECK_TOP_MULT);
        AnchorPane.setRightAnchor(cardDeckView,  null);
        AnchorPane.setBottomAnchor(cardDeckView, null);

        double diceSize = H * DICE_SIZE_MULT;
        diceView.setFitWidth(diceSize);
        diceView.setFitHeight(diceSize);
        AnchorPane.setLeftAnchor(diceView,   (W / 2) - (diceSize / 2) + 6);
        AnchorPane.setTopAnchor(diceView,    barH * DICE_TOP_MULT);
        AnchorPane.setRightAnchor(diceView,  null);
        AnchorPane.setBottomAnchor(diceView, null);

        double labelOffset = Math.max(40, W * 0.05);
        AnchorPane.setLeftAnchor(diceResultLabel,   (W / 2) - labelOffset);
        AnchorPane.setBottomAnchor(diceResultLabel, barH * 0.04);
        AnchorPane.setRightAnchor(diceResultLabel,  null);
        AnchorPane.setTopAnchor(diceResultLabel,    null);
        diceResultLabel.setStyle(
            "-fx-text-fill: #00ff88;" +
            "-fx-font-size: " + Math.max(10, H * 0.013) + "px;" +
            "-fx-font-weight: bold;");

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

        int statusFont = (int) Math.max(12, H * 0.018);
        myLabel.setStyle(
            "-fx-font-size: " + statusFont + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 0 3 0;" +
            "-fx-text-fill: #00ff88;");
    }

    // =========================================================
    //  PARALLAX
    // =========================================================

    private void setupParallax() {
        parallaxTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double currentX = backgroundView.getTranslateX();
                double currentY = backgroundView.getTranslateY();
                backgroundView.setTranslateX(currentX + (targetX - currentX) * 0.08);
                backgroundView.setTranslateY(currentY + (targetY - currentY) * 0.08);
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
    //  CONVENIENCE DELEGATES
    // =========================================================

    /** Repaints the board and updates the deck image. */
    private void refreshBoard() {
        boardRenderer.refreshBoard(game);
        uiUpdater.updateDeckImage(cardDeckView);
    }

    /** Pushes the current game state to every panel label. */
    private void updateUI() {
        uiUpdater.updateUI(game, myLabel);
    }

    // =========================================================
    //  ROLL HANDLER
    // =========================================================

    @FXML
    private void handleRollDice() {
        if (game == null || panelBuilder.isAnyOverlayVisible() || isAnimating) return;
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
                panelBuilder.actionLine1.setText("❄ " + current.getName() + " FROZEN — skipped!");
                panelBuilder.actionLine2.setText("");
                panelBuilder.actionLine3.setText("");
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

            game.playTurn();

            int newPos       = current.getPosition();
            int newEnergy    = current.getEnergy();
            int newOppEnergy = opponent.getEnergy();
            boolean cardDrawn = topCard != null &&
                (Board.cards.isEmpty() || Board.cards.get(0) != topCard);

            int moved = newPos - oldPos;
            if (moved < 0) moved += 100;
            int diceFace = Math.max(1, Math.min(6, moved));

            panelBuilder.actionLine1.setText(
                current.getName() + " → pos " + newPos + " (+" + moved + ")");
            panelBuilder.actionLine2.setText(cardDrawn && topCard != null
                ? "🃏 " + topCard.getName() + ": " + PanelBuilder.getCardEffectType(topCard.getName())
                : "");

            if (wasShielded && !current.isShielded())
                panelBuilder.actionLine3.setText("🛡 Shield blocked energy loss!");
            else if (newEnergy != oldEnergy) {
                int diff = newEnergy - oldEnergy;
                panelBuilder.actionLine3.setText(current.getName() + " energy "
                    + (diff > 0 ? "+" : "") + diff + " → " + newEnergy);

                animMgr.animateStationedMonsterPopups(grid, game, diff > 0, Math.abs(diff));
            } else if (newOppEnergy != oldOppEnergy) {
                int diff = newOppEnergy - oldOppEnergy;
                panelBuilder.actionLine3.setText(opponent.getName() + " energy "
                    + (diff > 0 ? "+" : "") + diff + " → " + newOppEnergy);
            } else {
                panelBuilder.actionLine3.setText("");
            }

            final Card finalTopCard = topCard;
            final boolean finalCardDrawn = cardDrawn;
            final Monster finalCurrent = current;
            final Monster finalOpponent = opponent;
            final int finalOldPos = oldPos;
            final int finalNewPos = newPos;
            final boolean[][] finalDoorWasActivated = doorWasActivated;
            final Cell[][] finalCells = cells;

            animMgr.animateDiceRoll(diceView, diceResultLabel, diceFace, () ->
                animMgr.animateMonsterMove(grid, finalCurrent, finalOpponent,
                    finalOldPos, finalNewPos, () -> {
                        if (finalCardDrawn && finalTopCard != null) {
                            refreshBoard(); updateUI();
                            // Check door sound before card overlay
                            int[] rowCol = game.getBoard().indexToRowCol(finalNewPos);
                            Cell landedCell = finalCells[rowCol[0]][rowCol[1]];
                            if (landedCell instanceof DoorCell) {
                                if (!finalDoorWasActivated[rowCol[0]][rowCol[1]]
                                        && ((DoorCell) landedCell).isActivated()) {
                                    SoundManager.getInstance().playDoorOpening();
                                }
                            }
                            SoundManager.getInstance().playCardDraw();
                            panelBuilder.showCardOverlay(finalTopCard, () -> {
                                refreshBoard(); updateUI();
                            });
                        } else {
                            refreshBoard(); updateUI();
                            // Check door sound
                            int[] rowCol = game.getBoard().indexToRowCol(finalNewPos);
                            Cell landedCell = finalCells[rowCol[0]][rowCol[1]];
                            if (landedCell instanceof DoorCell) {
                                if (!finalDoorWasActivated[rowCol[0]][rowCol[1]]
                                        && ((DoorCell) landedCell).isActivated()) {
                                    SoundManager.getInstance().playDoorOpening();
                                }
                            }
                        }
                        checkWinner();
                        isAnimating = false;
                    }));

        } catch (game.engine.exceptions.InvalidMoveException ex) {
            panelBuilder.actionLine1.setText("⚠ INVALID: " + ex.getMessage());
            panelBuilder.actionLine2.setText("Roll again!");
            panelBuilder.actionLine3.setText("");
            refreshBoard(); updateUI();
            isAnimating = false;
            showErrorAlert("Invalid Move", ex.getMessage());
        } catch (Exception ex) {
            panelBuilder.actionLine1.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
            isAnimating = false;
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

        if (parallaxTimer != null) parallaxTimer.stop();

        myLabel.setText(winner.getName() + " WINS! 🏆");
        myLabel.setStyle(
            "-fx-font-size: 18px; -fx-font-weight: bold;" +
            "-fx-font-family: " + PanelBuilder.LED + "; -fx-text-fill: #ffcc00;");

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
    //  POWER-UP HANDLER
    // =========================================================

    @FXML
    private void handlePowerUp() {
        if (game == null || panelBuilder.isAnyOverlayVisible()) return;
        boolean ok = showConfirmDialog("Use Powerup", "Activate Powerup?",
            "Costs " + Constants.POWERUP_COST + " energy. Proceed?");
        if (ok) {
            try {
                Monster current = game.getCurrent();
                String name = current.getName();
                
                game.usePowerup();
                
                panelBuilder.actionLine1.setText("⚡ " + name + " activated powerup!");
                panelBuilder.actionLine2.setText("");
                panelBuilder.actionLine3.setText("");
                
                animMgr.animateStationedMonsterPopups(grid, game, false, Constants.POWERUP_COST);                
                refreshBoard(); updateUI();
            } catch (Exception ex) {
                showErrorAlert("Powerup Failed", ex.getMessage());
            }
        }
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
        if (game == null || isAnimating || panelBuilder.isAnyOverlayVisible()) return;
        Monster current = game.getCurrent();
        if (e.getCode() == KeyCode.W) {
            current.setPosition(99);
            panelBuilder.actionLine1.setText("CHEAT: warped!");
            panelBuilder.actionLine2.setText("");
            panelBuilder.actionLine3.setText("");
            refreshBoard(); updateUI(); checkWinner();
        } else if (e.getCode() == KeyCode.E) {
            current.setEnergy(current.getEnergy() + 50);
            panelBuilder.actionLine1.setText("CHEAT: +50 energy!");
            panelBuilder.actionLine2.setText("");
            panelBuilder.actionLine3.setText("");
            
            animMgr.animateStationedMonsterPopups(grid, game, true, 50);            
            refreshBoard(); updateUI();
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
        Label c = new Label(content);
        c.setWrapText(true);
        Button ok = new Button("OK"), no = new Button("Cancel");
        ok.setOnAction(ev -> { result[0] = true; dialog.close(); });
        no.setOnAction(ev -> dialog.close());
        HBox btns = new HBox(10, ok, no);
        btns.setAlignment(Pos.CENTER);
        VBox layout = new VBox(12, h, c, btns);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
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
        Label c = new Label(msg != null ? msg : "Unknown error");
        c.setWrapText(true);
        Button ok = new Button("OK");
        ok.setOnAction(ev -> dialog.close());
        HBox btns = new HBox(ok);
        btns.setAlignment(Pos.CENTER);
        VBox layout = new VBox(12, h, c, btns);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        dialog.setScene(new Scene(layout, 320, 140));
        dialog.showAndWait();
    }
}