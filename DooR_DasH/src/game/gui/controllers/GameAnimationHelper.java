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
 * floating energy popup, conveyor-belt destination pointer, and card-overlay
 * blur/reveal transitions.
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
     * Spins the dice through random faces with an eased eased-out cadence
     * (fast at first, slowing smoothly into the landing), avoids showing
     * the same face twice in a row, then lands on {@code finalFace} with a
     * small overshoot bounce and fires {@code onFinished}.
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

        // Smooth ease-out cadence: many fast frames early, fewer/slower near landing.
        int totalSpins = 18;
        int totalMillis = 900;
        double[] times = new double[totalSpins];
        for (int i = 0; i < totalSpins; i++) {
            double t = (double) (i + 1) / totalSpins;       // 0..1
            double eased = 1 - Math.pow(1 - t, 3);            // ease-out cubic
            times[i] = eased * totalMillis;
        }

        int lastFace = -1;
        for (int i = 0; i < totalSpins; i++) {
            int f;
            do { f = rand.nextInt(6); } while (f == lastFace);
            lastFace = f;
            final int face = f;
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(times[i]),
                e -> diceView.setImage(diceImages[face])));
        }

        double landTime = totalMillis + 120;
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(landTime), e -> {
            diceView.setImage(diceImages[finalFace - 1]);
            if (resultLbl != null) resultLbl.setText(String.valueOf(finalFace));

            diceView.setScaleX(1.0); diceView.setScaleY(1.0);
            ScaleTransition bounce = new ScaleTransition(Duration.millis(160), diceView);
            bounce.setFromX(1.18); bounce.setFromY(1.18);
            bounce.setToX(1.0);    bounce.setToY(1.0);
            bounce.setInterpolator(Interpolator.EASE_OUT);
            bounce.setOnFinished(ev -> { if (onFinished != null) onFinished.run(); });
            bounce.play();
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
     * Each step glides with ease-in/ease-out and a light vertical "hop" bounce
     * instead of a flat linear slide.
     */
    public static void animateMove(Monster current, Monster opponent,
            int oldPos, int newPos,
            ImageView[][] monsterViews,
            javafx.scene.shape.Rectangle[][] spotlightViews,
            GridPane grid,
            Runnable onFinished) {
animateMove(current, opponent, oldPos, newPos, monsterViews, spotlightViews,
grid, onFinished, false);
}
    
    public static void animateMove(Monster current, Monster opponent,
            int oldPos, int newPos,
            ImageView[][] monsterViews,
            javafx.scene.shape.Rectangle[][] spotlightViews,
            GridPane grid,
            Runnable onFinished,
            boolean forceSingleHop) {
        // Reset all sprite views
        for (int r = 0; r < Constants.BOARD_ROWS; r++)
            for (int c = 0; c < Constants.BOARD_COLS; c++) {
                monsterViews[r][c].setTranslateX(0);
                monsterViews[r][c].setTranslateY(0);
                monsterViews[r][c].setScaleY(1.0);
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
        if (forceSingleHop || dist > 12 || oldPos == newPos) {
            seq.getChildren().add(makeHop(current, opponent, oldPos, newPos, 650,
                monsterViews, spotlightViews, grid, true));
        } else {
            int step = newPos > oldPos ? 1 : -1;
            int stepCount = dist;
            int i = 0;
            for (int pos = oldPos; pos != newPos; pos += step, i++) {
                int ms = stepDuration(i, stepCount);
                seq.getChildren().add(makeHop(current, opponent, pos, pos + step, ms,
                    monsterViews, spotlightViews, grid, false));
            }
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

    /**
     * Eases per-step duration across a multi-step move: a touch quicker in
     * the middle of the sequence, slightly gentler at the start and end, so
     * longer moves don't feel mechanically uniform.
     */
    private static int stepDuration(int index, int total) {
        if (total <= 1) return 220;
        double mid = (total - 1) / 2.0;
        double distFromMid = Math.abs(index - mid) / mid; // 0 at middle, 1 at ends
        return (int) (170 + distFromMid * 60); // 170ms mid, up to 230ms at ends
    }

    /** Builds a single-cell hop animation for the moving monster. */
    private static Animation makeHop(Monster moving, Monster stationary,
                                      int from, int to, int ms,
                                      ImageView[][] monsterViews,
                                      javafx.scene.shape.Rectangle[][] spotlightViews,
                                      GridPane grid,
                                      boolean isLongJump) {
        int[] fRC = toRowCol(from);
        int[] tRC = toRowCol(to);
        double cellW = grid.getWidth()  / Constants.BOARD_COLS;
        double cellH = grid.getHeight() / Constants.BOARD_ROWS;
        double dx    = (tRC[1] - fRC[1]) * cellW;
        double dy    = (tRC[0] - fRC[0]) * cellH;

        ImageView mv = monsterViews[fRC[0]][fRC[1]];

        // Horizontal/vertical glide, eased rather than linear
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), mv);
        tt.setByX(dx); tt.setByY(dy);
        tt.setInterpolator(Interpolator.EASE_BOTH);

        // Light vertical "hop" bounce layered on top of the glide via a scale
        // pulse, so each step reads as a hop rather than a flat slide.
        double hopHeight = isLongJump ? 24 : 10;
        Timeline arc = new Timeline(
            new KeyFrame(Duration.millis(0),       new KeyValue(mv.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(ms * 0.5), new KeyValue(mv.scaleYProperty(), 1.0 + hopHeight / 400.0)),
            new KeyFrame(Duration.millis(ms),       new KeyValue(mv.scaleYProperty(), 1.0))
        );

        ParallelTransition glideAndArc = new ParallelTransition(tt, arc);

        glideAndArc.setOnFinished(e -> {
            mv.setTranslateX(0); mv.setTranslateY(0); mv.setScaleY(1.0); mv.setImage(null);
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
            SoundManager.getInstance().playMovement();
            // Light up source cell at start of hop
            if (spotlightViews != null) {
                spotlightViews[fRC[0]][fRC[1]].setOpacity(0.85);
            }
        });
        return new SequentialTransition(front, glideAndArc);
    }

    // =========================================================
    //  CONVEYOR BELT DESTINATION POINTER
    // =========================================================

    /**
     * Draws a brief animated arrow from a conveyor-belt cell to the cell it
     * will transport a monster to, plus a pulsing ring on the destination
     * cell, then fades both out. Purely visual — does not move any monster
     * or touch engine state.
     *
     * @param fromIndex board index of the conveyor cell that was tapped
     * @param toIndex   board index of the destination cell
     * @param grid      the board GridPane (used to locate cell panes and sizing)
     */
    public static void animateConveyorPointer(int fromIndex, int toIndex, GridPane grid) {
        StackPane fromPane = findCellPane(grid, fromIndex);
        StackPane toPane   = findCellPane(grid, toIndex);
        if (fromPane == null || toPane == null) return;

        double cellW = grid.getWidth()  / Constants.BOARD_COLS;
        double cellH = grid.getHeight() / Constants.BOARD_ROWS;

        int[] fRC = toRowCol(fromIndex);
        int[] tRC = toRowCol(toIndex);
        double dx = (tRC[1] - fRC[1]) * cellW;
        double dy = (tRC[0] - fRC[0]) * cellH;
        double angleDeg = Math.toDegrees(Math.atan2(dy, dx));

        // Arrow glyph, centered on the source cell, rotated to point at target
        Label arrow = new Label("\u279C"); // ➜
        arrow.setStyle(
            "-fx-font-size: " + Math.max(18, cellW * 0.5) + "px;" +
            "-fx-text-fill: #ffdd33;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 4, 0.4, 0, 0);" +
            "-fx-font-weight: bold;");
        arrow.setMouseTransparent(true);
        arrow.setRotate(angleDeg);
        arrow.setOpacity(0);
        StackPane.setAlignment(arrow, Pos.CENTER);
        fromPane.getChildren().add(arrow);

        // Pulsing ring on the destination cell
        Label destPing = new Label("\u25CE"); // ◎
        destPing.setStyle(
            "-fx-font-size: " + Math.max(20, cellW * 0.55) + "px;" +
            "-fx-text-fill: #ffdd33;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 4, 0.4, 0, 0);");
        destPing.setMouseTransparent(true);
        destPing.setOpacity(0);
        StackPane.setAlignment(destPing, Pos.CENTER);
        toPane.getChildren().add(destPing);

        arrow.setTranslateX(0); arrow.setTranslateY(0);
        FadeTransition arrowIn = new FadeTransition(Duration.millis(150), arrow);
        arrowIn.setFromValue(0); arrowIn.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(550), arrow);
        double dist = Math.min(cellW, cellH) * 0.35;
        double rad  = Math.toRadians(angleDeg);
        slide.setByX(Math.cos(rad) * dist);
        slide.setByY(Math.sin(rad) * dist);
        slide.setCycleCount(2);
        slide.setAutoReverse(true);
        slide.setInterpolator(Interpolator.EASE_BOTH);

        PauseTransition hold = new PauseTransition(Duration.millis(750));

        FadeTransition arrowOut = new FadeTransition(Duration.millis(250), arrow);
        arrowOut.setFromValue(1); arrowOut.setToValue(0);
        arrowOut.setOnFinished(e -> fromPane.getChildren().remove(arrow));

        FadeTransition pingIn = new FadeTransition(Duration.millis(200), destPing);
        pingIn.setFromValue(0); pingIn.setToValue(1);
        ScaleTransition pingPulse = new ScaleTransition(Duration.millis(500), destPing);
        pingPulse.setFromX(0.6); pingPulse.setFromY(0.6);
        pingPulse.setToX(1.3);   pingPulse.setToY(1.3);
        pingPulse.setCycleCount(2);
        pingPulse.setAutoReverse(true);
        FadeTransition pingOut = new FadeTransition(Duration.millis(300), destPing);
        pingOut.setFromValue(1); pingOut.setToValue(0);
        pingOut.setOnFinished(e -> toPane.getChildren().remove(destPing));

        SequentialTransition fullSeq = new SequentialTransition(
            new ParallelTransition(arrowIn, pingIn),
            new ParallelTransition(slide, pingPulse, hold),
            new ParallelTransition(arrowOut, pingOut)
        );
        fullSeq.play();
    }

    /** Locates the StackPane cell at a given board index by scanning grid children. */
    private static StackPane findCellPane(GridPane grid, int boardIndex) {
        int[] rc = toRowCol(boardIndex);
        for (Node child : grid.getChildren()) {
            Integer cIdx = GridPane.getColumnIndex(child);
            Integer rIdx = GridPane.getRowIndex(child);
            int col = (cIdx == null) ? 0 : cIdx;
            int row = (rIdx == null) ? 0 : rIdx;
            if (col == rc[1] && row == rc[0] && child instanceof StackPane)
                return (StackPane) child;
        }
        return null;
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
    
 // =========================================================
//  TRANSPORT SLIDE  (conveyor belt / contamination sock — real movement)
// =========================================================

/**
 * Slides the monster sprite smoothly along a connecting line from the
 * transport cell it landed on to the cell it's been transported to.
 * Conveyor belts and contamination socks get visually distinct line
 * colors/styles so the two hazards read differently at a glance.
 * Calls {@code onFinished} once the slide completes.
 *
 * @param moving      the monster being transported
 * @param fromIndex   the conveyor/sock cell's board index (where it landed)
 * @param toIndex     the transport destination board index
 * @param isConveyor  true = conveyor belt styling (forward/cyan), false = sock styling (backward/purple)
 * @param monsterViews shared sprite-view grid
 * @param grid        the board GridPane
 * @param onFinished  callback invoked after the slide + cleanup completes
 */
public static void animateTransportSlide(Monster moving, int fromIndex, int toIndex,
                                           boolean isConveyor,
                                           ImageView[][] monsterViews,
                                           GridPane grid,
                                           Runnable onFinished) {
    StackPane fromPane = findCellPane(grid, fromIndex);
    StackPane toPane    = findCellPane(grid, toIndex);
    int[] fRC = toRowCol(fromIndex);
    int[] tRC = toRowCol(toIndex);

    if (fromPane == null || toPane == null) {
        // Fallback: just snap the sprite to the destination, no visual line.
        monsterViews[fRC[0]][fRC[1]].setImage(null);
        monsterViews[tRC[0]][tRC[1]].setImage(monsterSprite(moving.getName()));
        if (onFinished != null) onFinished.run();
        return;
    }

    double cellW = grid.getWidth()  / Constants.BOARD_COLS;
    double cellH = grid.getHeight() / Constants.BOARD_ROWS;
    double dx = (tRC[1] - fRC[1]) * cellW;
    double dy = (tRC[0] - fRC[0]) * cellH;
    double lineLength = Math.hypot(dx, dy);
    double angleDeg = Math.toDegrees(Math.atan2(dy, dx));

    // Style: conveyor = energetic cyan/green dashed line; sock = sickly purple/grey dashed line.
    String lineColor   = isConveyor ? "#33ffaa" : "#9966cc";
    String glowColor   = isConveyor ? "rgba(50,255,170,0.9)" : "rgba(150,80,200,0.85)";
    String dashPattern = isConveyor ? "10,6" : "4,7"; // conveyor = even mechanical dashes, sock = irregular/sickly

    javafx.scene.shape.Line track = new javafx.scene.shape.Line(0, 0, lineLength, 0);
    track.setStroke(javafx.scene.paint.Color.web(lineColor));
    track.setStrokeWidth(3);
    track.getStrokeDashArray().setAll(
        Double.parseDouble(dashPattern.split(",")[0]),
        Double.parseDouble(dashPattern.split(",")[1]));
    track.setEffect(new javafx.scene.effect.Glow(0.8));
    track.setMouseTransparent(true);
    track.setOpacity(0);
    track.setRotate(angleDeg);

    // Anchor the line's start at the source cell's centre; StackPane
    // children are centred by default, so translate from there.
    track.setTranslateX((dx / 2.0));
    track.setTranslateY((dy / 2.0));
    fromPane.getChildren().add(track);
    track.toFront();

    // Hide the static sprite at the source cell; a separate sliding
    // sprite copy travels along the line so it isn't clipped by either
    // cell's StackPane bounds.
    ImageView sourceSprite = monsterViews[fRC[0]][fRC[1]];
    Image spriteImage = monsterSprite(moving.getName());
    sourceSprite.setImage(null);

    ImageView slidingSprite = new ImageView(spriteImage);
    slidingSprite.setPreserveRatio(true);
    slidingSprite.setFitWidth(sourceSprite.getFitWidth() > 0 ? sourceSprite.getFitWidth() : cellW * 0.8);
    slidingSprite.setMouseTransparent(true);
    slidingSprite.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.web(glowColor)));
    fromPane.getChildren().add(slidingSprite);
    slidingSprite.toFront();

    FadeTransition trackIn = new FadeTransition(Duration.millis(150), track);
    trackIn.setFromValue(0); trackIn.setToValue(0.95);

    TranslateTransition slide = new TranslateTransition(Duration.millis(550), slidingSprite);
    slide.setToX(dx); slide.setToY(dy);
    slide.setInterpolator(isConveyor ? Interpolator.EASE_BOTH : Interpolator.LINEAR);

    // Conveyor: confident, slightly accelerating glide.
    // Sock: a little unsteady — small wobble pulses layered on top.
    Animation motion;
    if (isConveyor) {
        motion = slide;
    } else {
        Timeline wobble = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(slidingSprite.rotateProperty(), 0.0)),
            new KeyFrame(Duration.millis(140), new KeyValue(slidingSprite.rotateProperty(), -6.0)),
            new KeyFrame(Duration.millis(280), new KeyValue(slidingSprite.rotateProperty(), 5.0)),
            new KeyFrame(Duration.millis(420), new KeyValue(slidingSprite.rotateProperty(), -3.0)),
            new KeyFrame(Duration.millis(550), new KeyValue(slidingSprite.rotateProperty(), 0.0))
        );
        motion = new ParallelTransition(slide, wobble);
    }

    FadeTransition trackOut = new FadeTransition(Duration.millis(200), track);
    trackOut.setFromValue(0.95); trackOut.setToValue(0);

    SequentialTransition seq = new SequentialTransition(trackIn, motion, trackOut);
    seq.setOnFinished(e -> {
        fromPane.getChildren().remove(track);
        fromPane.getChildren().remove(slidingSprite);
        monsterViews[tRC[0]][tRC[1]].setImage(spriteImage);
        if (onFinished != null) onFinished.run();
    });
    seq.play();
}


//=========================================================
//POWER-UP ACTIVATION FX
//=========================================================

/**
* Plays a short "power surge" visual on a successful powerup activation: a
* golden glow + scale pulse on the activating monster's portrait, a floating
* "POWER UP!" label rising away from it, and a brief full-screen golden
* flash. Purely cosmetic — does not touch game state.
*/
public static void animatePowerUpActivation(StackPane backgroundRoot, ImageView portrait) {
if (backgroundRoot == null) return;

if (portrait != null) {
    javafx.scene.effect.DropShadow glow =
        new javafx.scene.effect.DropShadow(0, javafx.scene.paint.Color.web("#ffcc00"));
    glow.setSpread(0.45);
    javafx.scene.effect.Effect original = portrait.getEffect();
    portrait.setEffect(glow);

    Timeline glowPulse = new Timeline(
        new KeyFrame(Duration.millis(0),   new KeyValue(glow.radiusProperty(), 0)),
        new KeyFrame(Duration.millis(220), new KeyValue(glow.radiusProperty(), 45)),
        new KeyFrame(Duration.millis(600), new KeyValue(glow.radiusProperty(), 0))
    );
    glowPulse.setOnFinished(e -> portrait.setEffect(original));

    ScaleTransition pulse = new ScaleTransition(Duration.millis(260), portrait);
    pulse.setFromX(1.0); pulse.setFromY(1.0);
    pulse.setToX(1.12);  pulse.setToY(1.12);
    pulse.setCycleCount(2);
    pulse.setAutoReverse(true);
    pulse.setInterpolator(Interpolator.EASE_BOTH);

    new ParallelTransition(glowPulse, pulse).play();

    javafx.geometry.Bounds bounds = portrait.localToScene(portrait.getBoundsInLocal());
    javafx.geometry.Point2D topCenter = backgroundRoot.sceneToLocal(
        (bounds.getMinX() + bounds.getMaxX()) / 2, bounds.getMinY());

    Label burst = new Label("POWER UP!");
    burst.setStyle(
        "-fx-font-family: 'ARCADECLASSIC';" +
        "-fx-font-size: 15px;" +
        "-fx-font-weight: bold;" +
        "-fx-text-fill: #ffdd33;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.85), 5, 0.5, 0, 0);");
    burst.setMouseTransparent(true);
    burst.setOpacity(0);

    double rootW = backgroundRoot.getWidth();
    double rootH = backgroundRoot.getHeight();
    burst.setTranslateX(topCenter.getX() - rootW / 2.0 - 35);
    burst.setTranslateY(topCenter.getY() - rootH / 2.0 - 8);

    backgroundRoot.getChildren().add(burst);
    burst.toFront();

    FadeTransition burstIn = new FadeTransition(Duration.millis(150), burst);
    burstIn.setFromValue(0); burstIn.setToValue(1);
    TranslateTransition burstUp = new TranslateTransition(Duration.millis(900), burst);
    burstUp.setByY(-40);
    burstUp.setInterpolator(Interpolator.EASE_OUT);
    FadeTransition burstOut = new FadeTransition(Duration.millis(300), burst);
    burstOut.setFromValue(1); burstOut.setToValue(0);

    SequentialTransition fadeSeq = new SequentialTransition(
        burstIn, new PauseTransition(Duration.millis(450)), burstOut);
    ParallelTransition full = new ParallelTransition(burstUp, fadeSeq);
    full.setOnFinished(e -> backgroundRoot.getChildren().remove(burst));
    full.play();
}

javafx.scene.shape.Rectangle flash = new javafx.scene.shape.Rectangle();
flash.widthProperty().bind(backgroundRoot.widthProperty());
flash.heightProperty().bind(backgroundRoot.heightProperty());
flash.setFill(javafx.scene.paint.Color.web("#ffdd33"));
flash.setOpacity(0);
flash.setMouseTransparent(true);
backgroundRoot.getChildren().add(flash);
flash.toFront();

FadeTransition flashIn = new FadeTransition(Duration.millis(70), flash);
flashIn.setFromValue(0); flashIn.setToValue(0.30);
FadeTransition flashOut = new FadeTransition(Duration.millis(250), flash);
flashOut.setFromValue(0.30); flashOut.setToValue(0);
SequentialTransition flashSeq = new SequentialTransition(flashIn, flashOut);
flashSeq.setOnFinished(e -> backgroundRoot.getChildren().remove(flash));
flashSeq.play();
}

//=========================================================
//GENERIC "DENIED" SHAKE
//=========================================================

/** Quick horizontal shake to draw the eye to a blocked/failed action without resizing or hiding the node. */
public static void shakeNode(Node node) {
if (node == null) return;
Timeline shake = new Timeline(
    new KeyFrame(Duration.millis(0),   new KeyValue(node.translateXProperty(), 0)),
    new KeyFrame(Duration.millis(60),  new KeyValue(node.translateXProperty(), -10)),
    new KeyFrame(Duration.millis(120), new KeyValue(node.translateXProperty(), 9)),
    new KeyFrame(Duration.millis(180), new KeyValue(node.translateXProperty(), -7)),
    new KeyFrame(Duration.millis(240), new KeyValue(node.translateXProperty(), 4)),
    new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(), 0))
);
shake.play();
}
}