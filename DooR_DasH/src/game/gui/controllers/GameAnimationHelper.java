package game.gui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;
import game.engine.cells.Cell;
import game.engine.cells.MonsterCell;
import game.engine.monsters.Monster;

/**
 * GameAnimationHelper
 * --------------------
 * Pure animation logic: dice roll, monster hop, energy-bar cross-fade,
 * floating energy popup, and card-overlay blur/reveal transitions.
 *
 * Every method is package-private static and references only the data
 * structures it receives as parameters — no hidden mutable state.
 *
 * Owner: Animation / VFX team member
 */
public final class GameAnimationHelper {

    private GameAnimationHelper() {}

    // =========================================================
    //  DICE ANIMATION
    // =========================================================

    /**
     * Spins the dice through random faces with accelerating delays,
     * then lands on {@code finalFace} and fires {@code onFinished}.
     *
     * @param diceView   the ImageView showing the dice
     * @param diceImages six dice-face images (index 0 = face 1)
     * @param resultLbl  label that shows the numeric result
     * @param finalFace  1-6 face to land on
     * @param onFinished callback invoked after the animation completes
     * @return the started Timeline (caller may keep a reference to stop it)
     */
    public static Timeline animateDice(ImageView diceView, Image[] diceImages,
                                        Label resultLbl, int finalFace,
                                        Runnable onFinished) {
        Timeline tl = new Timeline();
        java.util.Random rand = new java.util.Random();
        int[] delays = {60, 80, 100, 130, 160, 200, 250, 320, 400};
        int elapsed = 0;
        for (int d : delays) {
            elapsed += d;
            final int f = rand.nextInt(6);
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(elapsed),
                e -> diceView.setImage(diceImages[f])));
        }
        elapsed += 300;
        final int total = elapsed;
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(total), e -> {
            diceView.setImage(diceImages[finalFace - 1]);
            if (resultLbl != null) resultLbl.setText(String.valueOf(finalFace));
            if (onFinished != null) onFinished.run();
        }));
        tl.play();
        return tl;
    }

    // =========================================================
    //  MONSTER MOVEMENT ANIMATION
    // =========================================================

    /**
     * Animates the moving monster step-by-step (or in one hop for large jumps),
     * pulsing a spotlight on the moving cell, then invokes {@code onFinished}.
     */
    public static void animateMove(Monster current, Monster opponent,
                                    int oldPos, int newPos,
                                    ImageView[][] monsterViews,
                                    javafx.scene.shape.Rectangle[][] spotlightViews,
                                    GridPane grid,
                                    Runnable onFinished) {
        // Reset all sprite views
        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setTranslateX(0);
                monsterViews[r][c].setTranslateY(0);
                monsterViews[r][c].setImage(null);
                if (spotlightViews != null) spotlightViews[r][c].setOpacity(0);
            }
        // Re-draw stationed monsters
        for (Monster m : Board.getStationedMonsters()) {
            int[] rc = toRowCol(m.getPosition());
            monsterViews[rc[0]][rc[1]].setImage(monsterSprite(m.getName()));
        }
        drawMonsterOnGrid(opponent, monsterViews);

        int[] startRC = toRowCol(oldPos);
        monsterViews[startRC[0]][startRC[1]].setImage(monsterSprite(current.getName()));

        SequentialTransition seq = new SequentialTransition();
        int dist = Math.abs(newPos - oldPos);
        if (dist > 12 || oldPos == newPos) {
            seq.getChildren().add(makeHop(current, opponent, oldPos, newPos, 600,
                monsterViews, spotlightViews, grid));
        } else {
            int step = newPos > oldPos ? 1 : -1;
            for (int pos = oldPos; pos != newPos; pos += step)
                seq.getChildren().add(makeHop(current, opponent, pos, pos + step, 250,
                    monsterViews, spotlightViews, grid));
        }
        seq.setOnFinished(e -> {
            // Clear all spotlights when movement ends
            if (spotlightViews != null)
                for (int r = 0; r < Constants.BOARD_ROWS; r++)
                    for (int c = 0; c < Constants.BOARD_COLS; c++)
                        spotlightViews[r][c].setOpacity(0);
            onFinished.run();
        });
        seq.play();
    }

    /** Builds a single-cell hop animation for the moving monster. */
    private static Animation makeHop(Monster moving, Monster stationary,
                                      int from, int to, int ms,
                                      ImageView[][] monsterViews,
                                      javafx.scene.shape.Rectangle[][] spotlightViews,
                                      GridPane grid) {
        int[] fRC = toRowCol(from);
        int[] tRC = toRowCol(to);
        double cellW = grid.getWidth()  / Constants.BOARD_COLS;
        double cellH = grid.getHeight() / Constants.BOARD_ROWS;
        double dx    = (tRC[1] - fRC[1]) * cellW;
        double dy    = (tRC[0] - fRC[0]) * cellH;

        ImageView mv = monsterViews[fRC[0]][fRC[1]];
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), mv);
        tt.setByX(dx); tt.setByY(dy);
        tt.setOnFinished(e -> {
            mv.setTranslateX(0); mv.setTranslateY(0); mv.setImage(null);
            for (Monster s : Board.getStationedMonsters())
                if (s.getPosition() == from) { mv.setImage(monsterSprite(s.getName())); break; }
            if (stationary.getPosition() == from && mv.getImage() == null)
                mv.setImage(monsterSprite(stationary.getName()));
            monsterViews[tRC[0]][tRC[1]].setImage(monsterSprite(moving.getName()));

            // Move spotlight to destination cell
            if (spotlightViews != null) {
                spotlightViews[fRC[0]][fRC[1]].setOpacity(0);
                spotlightViews[tRC[0]][tRC[1]].setOpacity(1.0);
            }
        });

        // Fade spotlight into the destination just before the hop lands
        PauseTransition front = new PauseTransition(Duration.millis(1));
        front.setOnFinished(e -> {
            mv.getParent().toFront();
            // Light up source cell at start of hop
            if (spotlightViews != null) {
                spotlightViews[fRC[0]][fRC[1]].setOpacity(0.85);
            }
        });
        return new SequentialTransition(front, tt);
    }

    // =========================================================
    //  ENERGY BAR CROSS-FADE
    // =========================================================

    /**
     * Smoothly cross-fades the energy bar to the appropriate tier image.
     *
     * @param bar          the main energy-bar ImageView
     * @param wrapper      the StackPane that contains it
     * @param energy       current energy value
     * @param currentImage the image currently shown (to detect no-op)
     * @param tierImages   array of [en0, en25, en50, en75, en100]
     * @return the new current-image (caller should store it)
     */
    public static Image animateEnergyBar(ImageView bar, StackPane wrapper,
                                          int energy, Image currentImage,
                                          Image[] tierImages) {
        // tierImages: 0=en0, 1=en25, 2=en50, 3=en75, 4=en100
        int pct = (int) Math.min(100, Math.max(0, (energy / 1000.0) * 100));
        Image target;
        if      (pct >= 75) target = tierImages[4];
        else if (pct >= 50) target = tierImages[3];
        else if (pct >= 25) target = tierImages[2];
        else if (pct > 0)   target = tierImages[1];
        else                target = tierImages[0];

        if (currentImage == target) return currentImage;

        bar.setVisible(true);
        bar.setOpacity(1);

        ImageView overlay = new ImageView(target);
        overlay.setFitWidth(bar.getFitWidth());
        overlay.setPreserveRatio(true);
        overlay.setOpacity(0);
        wrapper.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), overlay);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            bar.setImage(target);
            bar.setOpacity(1);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), overlay);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> wrapper.getChildren().remove(overlay));
            fadeOut.play();
        });
        fadeIn.play();

        return target;
    }

    // =========================================================
    //  FLOATING ENERGY POPUP
    // =========================================================

    /**
     * Fires floating ⚡ popups on every MonsterCell whose stationed monster
     * matches {@code targetRole}.
     */
    public static void animateStationedMonsterPopups(boolean isIncrease, int amount,
                                                      Role targetRole,
                                                      Cell[][] boardCells,
                                                      GridPane grid) {
        for (int index = 0; index < 100; index++) {
            int bRow = index / Constants.BOARD_COLS;
            int bCol = index % Constants.BOARD_COLS;
            if (bRow % 2 == 1) bCol = Constants.BOARD_COLS - 1 - bCol;

            Cell cell = boardCells[bRow][bCol];
            if (!(cell instanceof MonsterCell)) continue;
            Monster cellMonster = ((MonsterCell) cell).getCellMonster();
            if (cellMonster == null || cellMonster.getRole() != targetRole) continue;

            int[] visualRC = toRowCol(index);
            for (Node child : grid.getChildren()) {
                Integer cIdx = GridPane.getColumnIndex(child);
                Integer rIdx = GridPane.getRowIndex(child);
                int col = (cIdx == null) ? 0 : cIdx;
                int row = (rIdx == null) ? 0 : rIdx;
                if (col == visualRC[1] && row == visualRC[0] && child instanceof StackPane) {
                    createFloatingPopup((StackPane) child, isIncrease, amount);
                    break;
                }
            }
        }
    }

    /**
     * Creates and animates a single floating energy label inside a cell's StackPane.
     */
    public static void createFloatingPopup(StackPane cellPane,
                                            boolean isIncrease, int amount) {
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
            moveUp.setFromY(0); moveUp.setToY(-15);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(1200), popupLabel);
            fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
            ParallelTransition seq = new ParallelTransition(moveUp, fadeOut);
            seq.setOnFinished(ev -> cellPane.getChildren().remove(popupLabel));
            seq.play();
        });
    }

    // =========================================================
    //  BOARD COORDINATE HELPERS  (shared with BoardRenderer)
    // =========================================================

    /** Converts a flat board index (0-99) to a visual [row, col] pair. */
    public static int[] toRowCol(int index) {
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 == 1) col = Constants.BOARD_COLS - 1 - col;
        row = Constants.BOARD_ROWS - 1 - row;
        return new int[]{row, col};
    }

    // =========================================================
    //  MONSTER SPRITE HELPERS  (mirrors GameController originals)
    // =========================================================

    public static Image monsterSprite(String name) {
        // Images are looked up from the ImageLoader cache.
        // Mapping identical to original GameController.monsterSprite().
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":               return GameUIHelper.loadImage("celia mae.png");
            case "fungus":                  return GameUIHelper.loadImage("Fungus.png");
            case "henry j. waternoose":
            case "henry j. waternoose iii": return GameUIHelper.loadImage("Henry_J._Waternoose_III.png");
            case "james p. sullivan":
            case "james sullivan":          return GameUIHelper.loadImage("James sullivan.png");
            case "mike wazowski":           return GameUIHelper.loadImage("Mike_Wazowski.png");
            case "randall boggs":
            case "randall":                 return GameUIHelper.loadImage("Randall.png");
            case "roz":                     return GameUIHelper.loadImage("Roz.png");
            case "yeti":                    return GameUIHelper.loadImage("Yeti.png");
            default: return null;
        }
    }

    /**
     * Draws a single monster sprite onto the grid at its current position.
     * Package-private so GameBoardRenderer can delegate here instead of
     * duplicating the toRowCol + monsterSprite logic.
     */
    static void drawMonsterOnGrid(Monster m, ImageView[][] monsterViews) {
        if (m == null) return;
        int[] rc = toRowCol(m.getPosition());
        monsterViews[rc[0]][rc[1]].setImage(monsterSprite(m.getName()));
    }
}
