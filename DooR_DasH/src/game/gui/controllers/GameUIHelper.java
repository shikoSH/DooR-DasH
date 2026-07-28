package game.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * GameUIHelper
 * ------------
 * Stateless factory and utility methods shared by every controller helper.
 * Nothing here holds mutable state; every method is package-private static
 * so it can be called directly without an instance.
 *
 * Owner: UI / Widgets team member
 */
public final class GameUIHelper {

    private GameUIHelper() {}

    // =========================================================
    //  LABEL FACTORY
    // =========================================================

    /** Creates an arcade-font label with the given text, colour, size and weight. */
    public static Label makeLbl(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: " + size + "px;" +
            "-fx-text-fill: " + color + ";" +
            (bold ? "-fx-font-weight: bold;" : ""));
        l.setWrapText(true);
        l.setMaxWidth(9999);
        return l;
    }

    // =========================================================
    //  FONT-SIZE RESCALE
    // =========================================================

    /**
     * Updates only the font-size portion of a label's inline style, leaving
     * color, background, border, glow, etc. untouched. Used to rescale
     * panel/card text every layout pass so it tracks the window size
     * instead of staying at the fixed pixel value it was built with.
     */
    public static void setFontSize(Label l, double px) {
        if (l == null) return;
        String style = l.getStyle();
        if (style == null) style = "";
        if (style.matches("(?s).*-fx-font-size:\\s*[0-9.]+px;.*")) {
            style = style.replaceAll("-fx-font-size:\\s*[0-9.]+px;", "-fx-font-size: " + px + "px;");
        } else {
            style = style + "-fx-font-size: " + px + "px;";
        }
        l.setStyle(style);
    }

    // =========================================================
    //  GLOW / SHADOW HELPERS
    // =========================================================

    public static void addDropShadow(Node node, double radius, Color color) {
        node.setEffect(new DropShadow(radius, color));
    }

    public static void addGlow(Label label, Color color, double radius, double spread) {
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(radius);
        glow.setSpread(spread);
        label.setEffect(glow);
    }

    // =========================================================
    //  BUTTON HOVER EFFECT
    // =========================================================

    public static void addButtonHover(ImageView btn) {
        btn.setOnMouseEntered(e -> btn.setOpacity(0.80));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.00));
    }

    // =========================================================
    //  STATUS-LIGHT WIDGET HELPERS
    // =========================================================

    /**
     * Creates a single status-light ImageView pre-loaded from the ImageLoader cache.
     */
    public static ImageView makeLight(String filename) {
        ImageView iv = new ImageView(loadImage(filename));
        iv.setPreserveRatio(true);
        iv.setFitWidth(28);
        addDropShadow(iv, 6, Color.BLACK);
        return iv;
    }

    /**
     * Lays out a row of five stacked (off/on) status lights.
     */
    public static HBox makeLightRow(
            ImageView tOff, ImageView tOn,
            ImageView cOff, ImageView cOn,
            ImageView fOff, ImageView fOn,
            ImageView sOff, ImageView sOn,
            ImageView pOff, ImageView pOn) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane turnLight   = makeStackedLight(tOff, tOn);
        StackPane confLight   = makeStackedLight(cOff, cOn);
        StackPane freezeLight = makeStackedLight(fOff, fOn);
        StackPane shieldLight = makeStackedLight(sOff, sOn);
        StackPane powerLight  = makeStackedLight(pOff, pOn);

        // NEW — hover tooltips so the player knows what each status icon
        // means. Purely additive: doesn't touch the cross-fade logic in
        // setLight(), just layers a Tooltip + tiny hover-scale on top of
        // the same nodes. Order matches the fixed (turn, confusion,
        // freeze, shield, power) order every caller already uses.
        installLightTooltip(turnLight,   "TURN\nLights up during this monster's turn.");
        installLightTooltip(confLight,   "CONFUSED\nThis monster's next move may go to a random cell instead of the rolled one.");
        installLightTooltip(freezeLight, "FROZEN\nThis monster will miss its next turn.");
        installLightTooltip(shieldLight, "SHIELDED\nBlocks the next negative effect used against this monster.");
        installLightTooltip(powerLight,  "POWER-UP\nA power-up effect is currently active for this monster.");

        row.getChildren().addAll(turnLight, confLight, freezeLight, shieldLight, powerLight);
        return row;
    }

    /**
     * Installs a hover tooltip on a status-light StackPane and adds a
     * small scale-up hover cue, so hovering any of the turn / confusion /
     * freeze / shield / power icons tells the player what it means.
     * Does not affect the on/off visibility or opacity driven by setLight().
     */
    private static void installLightTooltip(StackPane light, String text) {
        Tooltip tip = new Tooltip(text);
        // NOTE: setShowDelay()/setHideDelay() need JavaFX 9+; left out for
        // compatibility. Tooltip just uses its default show/hide timing.
        tip.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 12px;" +
            "-fx-background-color: rgba(10,10,14,0.95);" +
            "-fx-text-fill: #f0f0f0;" +
            "-fx-border-color: #c9a227;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 6 10;");
        Tooltip.install(light, tip);

        light.setOnMouseEntered(e -> {
            light.setScaleX(1.18);
            light.setScaleY(1.18);
        });
        light.setOnMouseExited(e -> {
            light.setScaleX(1.0);
            light.setScaleY(1.0);
        });
    }

    /** Stacks an off and an on light image, with only the off one initially visible. */
    public static StackPane makeStackedLight(ImageView off, ImageView on) {
        off.setVisible(true);  off.setOpacity(1);
        on.setVisible(false);  on.setOpacity(0);
        StackPane sp = new StackPane(off, on);
        // No fixed max size here — the light images are resized every
        // layout pass in GameController.applyAllLayout() based on
        // panelW * LIGHT_SIZE_MULT. A fixed 36x36 cap here clipped the
        // lights on any window bigger than the size it was designed at.
        return sp;
    }

    /**
     * Cross-fades a status light between its off and on states.
     * Only runs an animation when the state actually changes.
     *
     * @param offView      the "off" ImageView
     * @param onView       the "on"  ImageView
     * @param active       whether the light should currently be on
     * @param currentState the light's previous on/off state
     * @param stateSetter  callback to persist the new state in the caller
     */
    public static void setLight(ImageView offView, ImageView onView,
                                 boolean active, boolean currentState,
                                 Consumer<Boolean> stateSetter) {
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
    //  INTERNAL IMAGE LOADER SHORTCUT
    // =========================================================

    /** Delegates to the singleton ImageLoader. */
    public static Image loadImage(String filename) {
        return ImageLoader.getInstance().loadImage(filename);
    }
}