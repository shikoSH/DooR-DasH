package game.gui.controllers;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.beans.property.DoubleProperty;
import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;
import game.engine.cells.*;
import game.engine.monsters.Monster;

import static game.gui.controllers.GameUIConstants.*;
import static game.gui.controllers.GameUIHelper.loadImage;

/**
 * GameBoardRenderer
 * -----------------
 * Owns the GridPane construction and board-refresh logic.
 * Keeps the three grid arrays (bgViews, monsterViews, energyLabels) and
 * exposes methods that GameController delegates to.
 *
 * Owner: Board / Grid team member
 */
public class GameBoardRenderer {

    // =========================================================
    //  GRID STATE
    // =========================================================
    private final GridPane grid;
    private final DoubleProperty cellSize;

    private ImageView[][] bgViews;
    private ImageView[][] monsterViews;
    private Label[][]     energyLabels;
    private javafx.scene.shape.Rectangle[][] spotlightViews;

    // ── Cell images ──────────────────────────────────────────
    private final Image normalImage;
    private final Image doorSC, doorLC, doorSO, doorLO;
    private final Image monsterCellImage;
    private final Image conveyorImage;
    private final Image sockImage;
    private final Image cardCellImage;

    /** Callback so GameController can handle monster-cell clicks. */
    private final java.util.function.IntConsumer onMonsterCellClick;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public GameBoardRenderer(GridPane grid, DoubleProperty cellSize,
                              java.util.function.IntConsumer onMonsterCellClick) {
        this.grid               = grid;
        this.cellSize           = cellSize;
        this.onMonsterCellClick = onMonsterCellClick;

        normalImage      = loadImage(IMG_NORMAL);
        doorSC           = loadImage(IMG_DOOR_SC);
        doorLC           = loadImage(IMG_DOOR_LC);
        doorSO           = loadImage(IMG_DOOR_SO);
        doorLO           = loadImage(IMG_DOOR_LO);
        monsterCellImage = loadImage(IMG_MONSTER_CELL);
        conveyorImage    = loadImage(IMG_CONVEYOR);
        sockImage        = loadImage(IMG_SOCK);
        cardCellImage    = loadImage(IMG_CARD_CELL);
    }

    // =========================================================
    //  GRID CONSTRUCTION
    // =========================================================

    /** Builds every cell StackPane and registers click handlers for monster slots. */
    public void buildGrid() {
        cellSize.bind(grid.heightProperty().divide(Constants.BOARD_ROWS));

        bgViews        = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        monsterViews   = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        energyLabels   = new Label[Constants.BOARD_ROWS][Constants.BOARD_COLS];
        spotlightViews = new Rectangle[Constants.BOARD_ROWS][Constants.BOARD_COLS];

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

                // Index label
                Label indexLabel = new Label(String.valueOf(boardIndex));
                indexLabel.setMouseTransparent(true);
                cellSize.addListener((obs, old, val) -> indexLabel.setStyle(
                    "-fx-font-family: '" + FONT + "';" +
                    "-fx-font-size: " + Math.max(TXT_CELL_INDEX, val.doubleValue() * 0.18) + "px;" +
                    "-fx-text-fill: rgba(255,255,255,0.85);" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-color: rgba(0,0,0,0.35);" +
                    "-fx-background-radius: 5;" +
                    "-fx-padding: 1 4 1 4;"));
                indexLabel.setStyle(
                    "-fx-font-family: '" + FONT + "';" +
                    "-fx-font-size: " + TXT_CELL_INDEX + "px;" +
                    "-fx-text-fill: rgba(255,255,255,0.85);" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-color: rgba(0,0,0,0.35);" +
                    "-fx-background-radius: 5;" +
                    "-fx-padding: 1 4 1 4;");
                StackPane.setAlignment(indexLabel, Pos.TOP_RIGHT);

                // Energy label
                Label energyLabel = new Label("");
                cellSize.addListener((obs, old, val) -> {
                    Object kind = energyLabel.getUserData();
                    if      ("door".equals(kind))    applyDoorEnergyLabelStyle(energyLabel, val.doubleValue());
                    else if ("monster".equals(kind)) applyMonsterEnergyLabelStyle(energyLabel, val.doubleValue());
                });
                energyLabel.setVisible(false);
                StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER);

                // Spotlight overlay — invisible by default, pulsed during monster movement
                Rectangle spotlight = new Rectangle();
                spotlight.setMouseTransparent(true);
                spotlight.setOpacity(0);
                spotlight.widthProperty().bind(
                    grid.widthProperty().divide(Constants.BOARD_COLS));
                spotlight.heightProperty().bind(
                    grid.heightProperty().divide(Constants.BOARD_ROWS));
                // Warm yellow-white radial gradient: bright centre, transparent edge
                spotlight.setFill(new RadialGradient(
                    0, 0, 0.5, 0.5, 0.65, true, CycleMethod.NO_CYCLE,
                    new Stop(0.00, Color.color(1.0, 0.95, 0.60, 0.90)),
                    new Stop(0.35, Color.color(1.0, 0.85, 0.30, 0.55)),
                    new Stop(0.70, Color.color(0.8, 0.60, 0.10, 0.20)),
                    new Stop(1.00, Color.color(0.0, 0.00, 0.00, 0.00))));
                spotlightViews[row][col] = spotlight;

                bgViews[row][col]      = bgView;
                monsterViews[row][col] = mView;
                energyLabels[row][col] = energyLabel;

                if (isMonsterSlot) {
                    final int idx = boardIndex;
                    cellStack.setCursor(Cursor.HAND);
                    cellStack.setOnMouseClicked(e -> onMonsterCellClick.accept(idx));
                }

                cellStack.getChildren().addAll(bgView, spotlight, mView, indexLabel, energyLabel);
                grid.add(cellStack, col, row);
            }
        }
    }

    // =========================================================
    //  BOARD REFRESH
    // =========================================================

    /** Re-renders the entire board to match current engine state. */
    public void refreshBoard(game.engine.Game game,
                              Image deckFull, Image deckMid, Image deckLeast,
                              ImageView cardDeckView) {
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
            int[] rc  = GameAnimationHelper.toRowCol(index);
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
            int[] rc = GameAnimationHelper.toRowCol(m.getPosition());
            monsterViews[rc[0]][rc[1]].setImage(GameAnimationHelper.monsterSprite(m.getName()));
        }
        drawMonster(game.getPlayer());
        drawMonster(game.getOpponent());
        updateDeck(cardDeckView, deckFull, deckMid, deckLeast);
    }

    // =========================================================
    //  ACCESSORS for GameController
    // =========================================================
    public ImageView[][] getMonsterViews() { return monsterViews; }
    public ImageView[][] getBgViews()      { return bgViews; }
    public Label[][]     getEnergyLabels() { return energyLabels; }
    public javafx.scene.shape.Rectangle[][] getSpotlightViews() { return spotlightViews; }

    // =========================================================
    //  PRIVATE HELPERS
    // =========================================================

    /** Delegates to GameAnimationHelper to avoid duplicating toRowCol + monsterSprite logic. */
    private void drawMonster(Monster m) {
        if (m == null) return;
        // Reuse the static helper in GameAnimationHelper — single source of truth.
        GameAnimationHelper.drawMonsterOnGrid(m, monsterViews);
    }

    private void setCellImage(int row, int col, Cell cell) {
        if (cell instanceof MonsterCell) {
            bgViews[row][col].setImage(monsterCellImage);
        } else if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            bgViews[row][col].setImage(d.isActivated()
                ? (d.getRole() == Role.SCARER ? doorSO : doorLO)
                : (d.getRole() == Role.SCARER ? doorSC : doorLC));
        } else if (cell instanceof ConveyorBelt) {
            bgViews[row][col].setImage(conveyorImage);
        } else if (cell instanceof ContaminationSock) {
            bgViews[row][col].setImage(sockImage);
        } else if (cell instanceof CardCell) {
            bgViews[row][col].setImage(cardCellImage);
        } else {
            bgViews[row][col].setImage(normalImage);
        }
    }

    private void updateDeck(ImageView cardDeckView,
                             Image deckFull, Image deckMid, Image deckLeast) {
        if (cardDeckView == null) return;
        int remaining = Board.cards.size();
        int total     = Board.getOriginalCards().size();
        if (total == 0) return;
        double ratio  = (double) remaining / total;
        cardDeckView.setImage(ratio > 0.60 ? deckFull
            : ratio > 0.25 ? deckMid : deckLeast);
    }

    private void applyDoorEnergyLabelStyle(Label lbl, double cellPx) {
        lbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + Math.max(TXT_DOOR_ENERGY, cellPx * 0.15) + "px;" +
            "-fx-text-fill: rgba(175,228,255,0.95);" +
            "-fx-font-weight: bold;");
    }

    private void applyMonsterEnergyLabelStyle(Label lbl, double cellPx) {
        lbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + Math.max(TXT_DOOR_ENERGY, cellPx * 0.15) + "px;" +
            "-fx-text-fill: rgba(100,255,160,0.95);" +
            "-fx-font-weight: bold;");
    }
}
