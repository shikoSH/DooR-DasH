package game.gui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cells.Cell;
import game.engine.cells.MonsterCell;
import game.engine.monsters.Monster;

/**
 * Owns all visual animations triggered during a turn:
 *  - Dice roll shuffle effect
 *  - Monster movement hop-by-hop across the grid
 *
 * Worker contract: construct with the shared {@link ImageLoader} and
 * {@link BoardRenderer} (needed for grid coordinates and monster view arrays).
 * Then call {@link #animateDiceRoll} or {@link #animateMonsterMove}; each
 * accepts a {@code Runnable onFinished} callback that GameController uses to
 * chain logic after the animation completes.
 */
public class AnimationManager {

    // ── Dependencies ──────────────────────────────────────────
    private final ImageLoader   images;
    private final BoardRenderer board;

    // ── Dice state ────────────────────────────────────────────
    private Timeline diceTimeline;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================

    public AnimationManager(ImageLoader images, BoardRenderer board) {
        this.images = images;
        this.board  = board;
    }

    // =========================================================
    //  DICE ROLL ANIMATION
    // =========================================================

    /**
     * Plays a shuffle animation on {@code diceView}, landing on
     * {@code finalFace} (1-based), then calls {@code onFinished}.
     *
     * @param diceView        the ImageView showing the die
     * @param diceResultLabel label to update with "Rolled: N"
     * @param finalFace      the face the die should end on (1..6)
     * @param onFinished      callback invoked once the animation ends
     */
    public void animateDiceRoll(ImageView diceView, javafx.scene.control.Label diceResultLabel,
                                int finalFace, Runnable onFinished) {
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
                e -> diceView.setImage(images.diceImages[face])));
        }
        elapsed += 300;
        final int total = elapsed;
        diceTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(total), e -> {
            diceView.setImage(images.diceImages[finalFace - 1]);
            diceResultLabel.setText("Rolled: " + finalFace);
            if (onFinished != null) onFinished.run();
        }));
        diceTimeline.play();
    }

    // =========================================================
    //  MONSTER MOVE ANIMATION
    // =========================================================

    /**
     * Animates {@code current} moving from {@code oldPos} to {@code newPos},
     * leaving {@code opponent} stationary. Calls {@code onFinished} when done.
     *
     * <p>Short moves (≤12 squares) step cell-by-cell; long moves / teleports
     * use a single hop to keep the animation snappy.
     *
     * @param grid      the GridPane (needed for cell dimensions)
     * @param current   monster that is moving
     * @param opponent  monster that is standing still this turn
     * @param oldPos    board index the moving monster started on
     * @param newPos    board index the moving monster ends on
     * @param onFinished callback after the last hop completes
     */
    public void animateMonsterMove(javafx.scene.layout.GridPane grid,
                                   Monster current, Monster opponent,
                                   int oldPos, int newPos, Runnable onFinished) {
        // Reset all translation offsets and clear sprites
        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                board.getMonsterView(r, c).setTranslateX(0);
                board.getMonsterView(r, c).setTranslateY(0);
                board.getMonsterView(r, c).setImage(null);
            }

        // Redraw stationed monsters
        for (Monster m : Board.getStationedMonsters()) {
            int[] rc = board.indexToRowCol(m.getPosition());
            board.getMonsterView(rc[0], rc[1]).setImage(images.getMonsterImage(m.getName()));
        }

        // Draw the stationary opponent
        board.drawMonsterOverlay(opponent);

        // Place the moving monster at its start position
        int[] startRC = board.indexToRowCol(oldPos);
        board.getMonsterView(startRC[0], startRC[1]).setImage(
            images.getMonsterImage(current.getName()));

        // Build the hop sequence
        SequentialTransition seq = new SequentialTransition();
        int dist = Math.abs(newPos - oldPos);
        if (dist > 12 || oldPos == newPos) {
            seq.getChildren().add(createHop(grid, current, opponent, oldPos, newPos, 600));
        } else {
            int step = newPos > oldPos ? 1 : -1;
            for (int pos = oldPos; pos != newPos; pos += step)
                seq.getChildren().add(createHop(grid, current, opponent, pos, pos + step, 250));
        }
        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }

    // =========================================================
    //  SINGLE HOP
    // =========================================================

    /**
     * Builds one translate animation moving {@code moving} from cell
     * {@code from} to cell {@code to}. On finish it restores the cell images
     * correctly so the next hop (or the final state) looks right.
     */
    private Animation createHop(javafx.scene.layout.GridPane grid,
                                 Monster moving, Monster stationary,
                                 int from, int to, int ms) {
        int[] fromRC = board.indexToRowCol(from);
        int[] toRC   = board.indexToRowCol(to);
        double cellW = grid.getWidth()  / Constants.BOARD_COLS;
        double cellH = grid.getHeight() / Constants.BOARD_ROWS;
        double dx = (toRC[1] - fromRC[1]) * cellW;
        double dy = (toRC[0] - fromRC[0]) * cellH;

        ImageView mv = board.getMonsterView(fromRC[0], fromRC[1]);
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), mv);
        tt.setByX(dx);
        tt.setByY(dy);
        tt.setOnFinished(e -> {
            mv.setTranslateX(0);
            mv.setTranslateY(0);
            mv.setImage(null);

            // Restore any stationed monster that was at the 'from' cell
            for (Monster stationed : Board.getStationedMonsters())
                if (stationed.getPosition() == from) {
                    mv.setImage(images.getMonsterImage(stationed.getName()));
                    break;
                }

            // Restore stationary opponent if it was at 'from'
            if (stationary.getPosition() == from && mv.getImage() == null)
                mv.setImage(images.getMonsterImage(stationary.getName()));

            // Place the moving monster at the destination
            board.getMonsterView(toRC[0], toRC[1]).setImage(
                images.getMonsterImage(moving.getName()));
        });

        // Bring the moving cell to the front so it renders above neighbours
        PauseTransition front = new PauseTransition(Duration.millis(1));
        front.setOnFinished(e -> {
            Node hopParent = mv.getParent();
            if (hopParent != null) hopParent.toFront();
        });
        return new SequentialTransition(front, tt);
    }
    
    //--- Energy of stationed monster change pop up 
    // FIXED: Added 'int amount' parameters here to forward value to the popup text builder
    public void animateStationedMonsterPopups(GridPane grid, Role movingRole,
            boolean isIncrease, int amount) {
        if (movingRole == null || amount <= 0) return;

        for (Monster stationed : Board.getStationedMonsters()) {
            if (stationed.getRole() != movingRole) continue;

            int[] visualRC = board.indexToRowCol(stationed.getPosition());
            Node parent = board.getMonsterView(visualRC[0], visualRC[1]).getParent();
            if (parent instanceof StackPane) {
                createFloatingPopup((StackPane) parent, isIncrease, amount);
            }
        }
    }

    /**
     * Generates, styles, layouts, and triggers the animation sequences for the popup label node.
     */
    // FIXED: Added 'int amount' here to inject the numerical difference into the string creation
    private void createFloatingPopup(StackPane cellPane, boolean isIncrease, int amount) {
        // Build styling properties based on the action type
        String text = isIncrease ? "+" + amount + "⚡" : "-" + amount + "⚡";
        String color = isIncrease ? "#00FF00" : "#FF3333"; // Vibrant Green or Deep Warning Red

        Label popupLabel = new Label(text);
        popupLabel.setStyle(
            "-fx-font-family: 'Arial';" + 
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 3, 0, 0, 0);" +
            "-fx-padding: 2 0 0 4;"
        );

        // Position explicitly inside the Top-Left quadrant requested
        StackPane.setAlignment(popupLabel, Pos.TOP_LEFT);
        
        // Ensure UI processing safely registers layout generation additions
        Platform.runLater(() -> {
            cellPane.getChildren().add(popupLabel);

            // Animate moving subtly upward over time
            TranslateTransition moveUp = new TranslateTransition(Duration.millis(1200), popupLabel);
            moveUp.setFromY(0);
            moveUp.setToY(-15);

            // Animate fading smooth cleanly out of visual view
            FadeTransition fadeOut = new FadeTransition(Duration.millis(1200), popupLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // Bundle transitions simultaneously
            ParallelTransition sequence = new ParallelTransition(moveUp, fadeOut);
            
            // Clean memory references by trimming node allocations upon task completion
            sequence.setOnFinished(event -> cellPane.getChildren().remove(popupLabel));
            sequence.play();
        });
    }
}