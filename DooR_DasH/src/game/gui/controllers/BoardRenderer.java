package game.gui.controllers;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cells.*;
import game.engine.monsters.Monster;

/**
 * Owns the per-cell ImageView/Label grid arrays and is solely responsible for
 * translating backend board state into visual cell images and monster sprites.
 *
 * Worker contract:
 *  1. Construct with the shared {@link ImageLoader}.
 *  2. Call {@link #buildGrid(GridPane)} once during initialization — this
 *     populates {@code backgroundViews}, {@code monsterViews}, and
 *     {@code energyLabels} and inserts them into the JavaFX GridPane.
 *  3. Call {@link #refreshBoard(Game)} after every turn to re-render everything.
 */
public class BoardRenderer {

    // ── Font constant (shared style) ──────────────────────────
    private static final String LED = PanelBuilder.LED;
    private javafx.beans.property.DoubleProperty cellSize = 
        new javafx.beans.property.SimpleDoubleProperty(40);
    
    // ── Dependencies ──────────────────────────────────────────
    private final ImageLoader images;

    // ── Grid view arrays (populated by buildGrid) ─────────────
    private ImageView[][] backgroundViews;
    private ImageView[][] monsterViews;
    private Label[][]     energyLabels;

    // ── Reference to the GridPane (needed for cell sizing binds) ─
    private GridPane grid;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================

    public BoardRenderer(ImageLoader images) {
        this.images = images;
    }

    // =========================================================
    //  BUILD GRID
    // =========================================================

    /**
     * Creates all cell StackPanes, binds their sizes to {@code grid}, and
     * inserts them. Must be called exactly once before {@link #refreshBoard}.
     */
    public void buildGrid(GridPane grid) {
        this.grid = grid;
        cellSize.bind(grid.heightProperty().divide(Constants.BOARD_ROWS));
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

             // Replace the static style block with:
                Label indexLabel = new Label(String.valueOf(boardIndex));
                cellSize.addListener((obs, old, val) -> indexLabel.setStyle(
                    "-fx-font-family: " + LED + ";" +
                    "-fx-font-size: " + Math.max(6, val.doubleValue() * 0.18) + "px;" +
                    "-fx-text-fill: rgba(255,255,255,0.65);" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 1 2 0 0;"));
                StackPane.setAlignment(indexLabel, Pos.TOP_RIGHT);

                Label energyLabel = new Label("");
                cellSize.addListener((obs, old, val) -> energyLabel.setStyle(
                    "-fx-font-family: " + LED + ";" +
                    "-fx-font-size: " + Math.max(5, val.doubleValue() * 0.16) + "px;" +
                    "-fx-text-fill: #FFD700;" +
                    "-fx-font-weight: bold;" +
                    "-fx-alignment: center;" +
                    "-fx-text-alignment: center;" +
                    "-fx-padding: 0 0 1 0;"));
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
    //  REFRESH BOARD
    // =========================================================

    /**
     * Full board repaint: clears all monster sprites, repaints every cell,
     * then overlays stationed and active monsters.
     *
     * @param game the current Game (must not be null)
     */
    public void refreshBoard(Game game) {
        if (game == null) return;
        Cell[][] cells = game.getBoard().getBoardCells();

        // Clear
        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setImage(null);
                energyLabels[r][c].setVisible(false);
            }

        // Paint cells
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
                    String roleText = (door.getRole() == Role.SCARER) ? "SCARER" : "LAUGHER";
                    energyLabels[rc[0]][rc[1]].setText(
                        door.isActivated() ? roleText : roleText + "\n⚡" + door.getEnergy());
                    energyLabels[rc[0]][rc[1]].setVisible(true);
                }
            } else {
                backgroundViews[rc[0]][rc[1]].setImage(images.normalImage);
            }
        }

        // Draw stationed monsters (decoration cells)
        for (Monster m : Board.getStationedMonsters()) {
            int[] rc = indexToRowCol(m.getPosition());
            monsterViews[rc[0]][rc[1]].setImage(images.getMonsterImage(m.getName()));
        }

        // Draw active monsters
        drawMonsterOverlay(game.getPlayer());
        drawMonsterOverlay(game.getOpponent());
    }

    // =========================================================
    //  MONSTER OVERLAY
    // =========================================================

    /** Places one monster's sprite at its current board position. */
    public void drawMonsterOverlay(Monster m) {
        if (m == null) return;
        int[] rc = indexToRowCol(m.getPosition());
        monsterViews[rc[0]][rc[1]].setImage(images.getMonsterImage(m.getName()));
    }

    // =========================================================
    //  CELL IMAGE
    // =========================================================

    private void setCellImage(int row, int col, Cell cell) {
        if (cell instanceof MonsterCell) {
            backgroundViews[row][col].setImage(images.monsterCellGreyImage);
            MonsterCell monsterCell = (MonsterCell) cell;
            String roleText = (monsterCell.getCellMonster().getRole() == Role.SCARER)
                ? "SCARER" : "LAUGHER";
            energyLabels[row][col].setText(roleText);
            energyLabels[row][col].setVisible(true);
        } else if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            backgroundViews[row][col].setImage(door.isActivated()
                ? (door.getRole() == Role.SCARER ? images.scarerOpenDoorImage  : images.laugherOpenDoorImage)
                : (door.getRole() == Role.SCARER ? images.ScarerdoorImage      : images.laugherdoorImage));
        } else if (cell instanceof ConveyorBelt) {
            backgroundViews[row][col].setImage(images.conveyorImage);
        } else if (cell instanceof ContaminationSock) {
            backgroundViews[row][col].setImage(images.contaminationImage);
        } else if (cell instanceof CardCell) {
            backgroundViews[row][col].setImage(images.cardCellImage);
        } else {
            backgroundViews[row][col].setImage(images.normalImage);
        }
    }

    // =========================================================
    //  COORDINATE HELPER
    // =========================================================

    /**
     * Converts a flat board index (0-99, back-end coordinate system) to a
     * [row, col] pair in the visual grid (origin = top-left on screen).
     *
     * This is a shared utility — AnimationManager also calls it.
     */
    public int[] indexToRowCol(int index) {
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        row = Constants.BOARD_ROWS - 1 - row;
        return new int[]{row, col};
    }

    // =========================================================
    //  GRID VIEW ACCESSORS (used by AnimationManager)
    // =========================================================

    /** Returns the monster ImageView at visual grid [row][col]. */
    public ImageView getMonsterView(int row, int col) {
        return monsterViews[row][col];
    }

    /** Returns the background ImageView at visual grid [row][col]. */
    public ImageView getBackgroundView(int row, int col) {
        return backgroundViews[row][col];
    }
}
