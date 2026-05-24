package game.gui.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import static game.gui.controllers.GameUIConstants.*;
import static game.gui.controllers.GameUIHelper.*;

/**
 * GamePanelBuilder
 * ----------------
 * Responsible for constructing the player side-panel, the opponent side-panel,
 * and the action-log widget.  After {@code build*()} is called the caller
 * receives a {@link PlayerPanelRefs} or {@link OpponentPanelRefs} data-class
 * that bundles every widget reference the controller needs to update later.
 *
 * Owner: Side-panel / HUD team member
 */
public final class GamePanelBuilder {

    private GamePanelBuilder() {}

    // =========================================================
    //  PLAYER PANEL
    // =========================================================

    /**
     * Builds all widgets and adds them to {@code container}.
     * All text labels are anchored inside a fixed StackPane so they do not
     * shift when the window is resized.
     */
    public static PlayerPanelRefs buildPlayerPanel(VBox container, Image en100) {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(6);

        PlayerPanelRefs r = new PlayerPanelRefs();

        // ── Status lights ─────────────────────────────────────────────────
        r.turnOff  = makeLight(IMG_TURN_OFF);   r.turnOn  = makeLight(IMG_TURN_ON);
        r.confOff  = makeLight(IMG_CONF_OFF);   r.confOn  = makeLight(IMG_CONF_ON);
        r.frzOff   = makeLight(IMG_FREEZE_OFF); r.frzOn   = makeLight(IMG_FREEZE_ON);
        r.shldOff  = makeLight(IMG_SHIELD_OFF); r.shldOn  = makeLight(IMG_SHIELD_ON);
        r.pwrOff   = makeLight(IMG_POWER_OFF);  r.pwrOn   = makeLight(IMG_POWER_ON);
        HBox lights = makeLightRow(
            r.turnOff, r.turnOn, r.confOff, r.confOn,
            r.frzOff,  r.frzOn,  r.shldOff, r.shldOn, r.pwrOff, r.pwrOn);

        // ── Portrait + position badge ─────────────────────────────────────
        r.portrait.setPreserveRatio(true);
        addDropShadow(r.portrait, 12, Color.BLACK);
        r.posLbl = makeLbl("0", "#00ff88", TXT_PLAYER_POS, true);
        StackPane portraitPane = new StackPane(r.portrait, r.posLbl);
        StackPane.setAlignment(r.posLbl, Pos.BOTTOM_LEFT);
        StackPane.setMargin(r.posLbl, new Insets(0, 0, 6, 6));

        // ── Profile card: background image + text anchored on top ─────────
        r.profileBg = new ImageView(loadImage(IMG_PROFILE));
        r.profileBg.setPreserveRatio(true);

        r.nameLbl   = makeLbl("-",        "white",    TXT_PLAYER_NAME, true);
        r.typeLbl   = makeLbl("Type: -",  "#aaaaaa",  TXT_PLAYER_TYPE, false);
        r.roleLbl   = makeLbl("Role: -",  "white",    TXT_PLAYER_ROLE, false);

        VBox profileText = new VBox(2, r.nameLbl, r.typeLbl, r.roleLbl);
        profileText.setPadding(new Insets(8, 6, 6, 10));
        profileText.setAlignment(Pos.TOP_LEFT);
        // Use a StackPane so the text floats on top of the bg image at a
        // fixed TOP_LEFT anchor — no translate offsets that drift on resize.
        StackPane profilePane = new StackPane(r.profileBg, profileText);
        StackPane.setAlignment(profileText, Pos.TOP_LEFT);

        // ── Energy label ──────────────────────────────────────────────────
        r.energyLbl = makeLbl("-", "#00ff88", TXT_PLAYER_ENERGY, true);
        addGlow(r.energyLbl, Color.web("#00ff88"), 16, 0.6);
        HBox energyRow = new HBox(r.energyLbl);
        energyRow.setAlignment(Pos.CENTER_LEFT);
        energyRow.setPadding(new Insets(0, 0, 0, 10));

        // ── Energy bar ────────────────────────────────────────────────────
        r.energyBar.setPreserveRatio(true);
        r.energyBar.setImage(en100);
        r.energyBarCurrentImage = en100;
        r.energyWrapper = new StackPane(r.energyBar);
        r.energyWrapper.setAlignment(Pos.CENTER_LEFT);
        r.energyWrapper.setPadding(new Insets(0, 0, 0, 10));

        // ── Status string ─────────────────────────────────────────────────
        r.statusLbl = makeLbl("NORMAL", "#aaaaaa", TXT_PLAYER_STATUS, false);
        HBox statusRow = new HBox(r.statusLbl);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setPadding(new Insets(0, 0, 0, 10));

        // ── Turn label ────────────────────────────────────────────────────
        r.turnLbl = makeLbl("", "#ffcc00", TXT_PLAYER_TURN, true);
        HBox turnRow = new HBox(r.turnLbl);
        turnRow.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
            lights, portraitPane, profilePane,
            energyRow, r.energyWrapper,
            statusRow, turnRow);

        return r;
    }

    // =========================================================
    //  OPPONENT PANEL
    // =========================================================

    /**
     * Builds all widgets and adds them to {@code container}.
     * All text labels are anchored inside a fixed StackPane so they do not
     * shift when the window is resized.
     */
    public static OpponentPanelRefs buildOpponentPanel(VBox container, Image en100) {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(6);

        OpponentPanelRefs r = new OpponentPanelRefs();

        // ── Status lights ─────────────────────────────────────────────────
        r.turnOff  = makeLight(IMG_TURN_OFF);   r.turnOn  = makeLight(IMG_TURN_ON);
        r.confOff  = makeLight(IMG_CONF_OFF);   r.confOn  = makeLight(IMG_CONF_ON);
        r.frzOff   = makeLight(IMG_FREEZE_OFF); r.frzOn   = makeLight(IMG_FREEZE_ON);
        r.shldOff  = makeLight(IMG_SHIELD_OFF); r.shldOn  = makeLight(IMG_SHIELD_ON);
        r.pwrOff   = makeLight(IMG_POWER_OFF);  r.pwrOn   = makeLight(IMG_POWER_ON);
        HBox lights = makeLightRow(
            r.turnOff, r.turnOn, r.confOff, r.confOn,
            r.frzOff,  r.frzOn,  r.shldOff, r.shldOn, r.pwrOff, r.pwrOn);

        // ── Portrait + position badge ─────────────────────────────────────
        r.portrait.setPreserveRatio(true);
        addDropShadow(r.portrait, 12, Color.BLACK);
        r.posLbl = makeLbl("0", "#ff6666", TXT_PLAYER_POS, true);
        StackPane portPane = new StackPane(r.portrait, r.posLbl);
        StackPane.setAlignment(r.posLbl, Pos.BOTTOM_LEFT);
        StackPane.setMargin(r.posLbl, new Insets(0, 0, 6, 6));

        // ── Profile card ──────────────────────────────────────────────────
        r.profileBg = new ImageView(loadImage(IMG_PROFILE));
        r.profileBg.setPreserveRatio(true);

        r.nameLbl = makeLbl("-",        "white",    TXT_PLAYER_NAME, true);
        r.typeLbl = makeLbl("Type: -",  "#aaaaaa",  TXT_PLAYER_TYPE, false);
        r.roleLbl = makeLbl("Role: -",  "white",    TXT_PLAYER_ROLE, false);

        VBox profileText = new VBox(2, r.nameLbl, r.typeLbl, r.roleLbl);
        profileText.setPadding(new Insets(8, 6, 6, 10));
        profileText.setAlignment(Pos.TOP_LEFT);
        StackPane profilePane = new StackPane(r.profileBg, profileText);
        StackPane.setAlignment(profileText, Pos.TOP_LEFT);

        // ── Energy label ──────────────────────────────────────────────────
        r.energyLbl = makeLbl("-", "#ff6666", TXT_PLAYER_ENERGY, true);
        addGlow(r.energyLbl, Color.web("#ff6666"), 16, 0.6);
        HBox energyRow = new HBox(r.energyLbl);
        energyRow.setAlignment(Pos.CENTER_LEFT);
        energyRow.setPadding(new Insets(0, 0, 0, 10));

        // ── Energy bar ────────────────────────────────────────────────────
        r.energyBar.setPreserveRatio(true);
        r.energyBar.setImage(en100);
        r.energyBarCurrentImage = en100;
        r.energyWrapper = new StackPane(r.energyBar);
        r.energyWrapper.setAlignment(Pos.CENTER_LEFT);
        r.energyWrapper.setPadding(new Insets(0, 0, 0, 10));

        // ── Status string ─────────────────────────────────────────────────
        r.statusLbl = makeLbl("NORMAL", "#aaaaaa", TXT_PLAYER_STATUS, false);
        HBox statusRow = new HBox(r.statusLbl);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setPadding(new Insets(0, 0, 0, 10));

        container.getChildren().addAll(
            lights, portPane, profilePane,
            energyRow, r.energyWrapper,
            statusRow);

        return r;
    }

    // =========================================================
    //  ACTION LOG
    // =========================================================

    /**
     * Builds and inserts the action-log widget.
     * Returns an {@link ActionLogRefs} so the caller can bind widths and
     * update labels without relying on a hidden {@code userData} trick.
     */
    public static ActionLogRefs buildActionLog(VBox container) {
        ImageView bg = new ImageView(loadImage(IMG_ACTION_LOG));
        bg.setPreserveRatio(true);

        Label line1 = makeLbl("", "white",   TXT_ACTION_LOG, false);
        Label line2 = makeLbl("", "#aaffaa", TXT_ACTION_LOG, false);
        Label line3 = makeLbl("", "#aaaaff", TXT_ACTION_LOG, false);
        for (Label l : new Label[]{line1, line2, line3}) l.setWrapText(true);

        VBox logText = new VBox(2, line1, line2, line3);
        logText.setAlignment(Pos.TOP_LEFT);
        logText.setPadding(new Insets(26, 8, 6, 10));

        StackPane logPane = new StackPane(bg, logText);
        StackPane.setAlignment(logText, Pos.TOP_LEFT);

        container.getChildren().add(logPane);
        container.setStyle("-fx-padding: 4;");

        return new ActionLogRefs(line1, line2, line3, bg);
    }

    // =========================================================
    //  DATA CLASSES
    // =========================================================

    /** Holds every reference the controller needs from the action-log widget. */
    public static class ActionLogRefs {
        public final Label     line1, line2, line3;
        public final ImageView background;

        public ActionLogRefs(Label line1, Label line2, Label line3, ImageView background) {
            this.line1      = line1;
            this.line2      = line2;
            this.line3      = line3;
            this.background = background;
        }
    }

    /** All mutable widget references for the player side-panel. */
    public static class PlayerPanelRefs {
        public final ImageView portrait    = new ImageView();
        public final ImageView energyBar   = new ImageView();
        public StackPane energyWrapper;
        public Label nameLbl, typeLbl, roleLbl;
        public Label posLbl, energyLbl, statusLbl, turnLbl;
        public ImageView profileBg;

        public ImageView turnOff, turnOn;
        public ImageView confOff, confOn;
        public ImageView frzOff,  frzOn;
        public ImageView shldOff, shldOn;
        public ImageView pwrOff,  pwrOn;

        public boolean turnState, confState, frzState, shldState, pwrState;
        public Image   energyBarCurrentImage;
    }

    /** All mutable widget references for the opponent side-panel. */
    public static class OpponentPanelRefs {
        public final ImageView portrait    = new ImageView();
        public final ImageView energyBar   = new ImageView();
        public StackPane energyWrapper;
        public Label nameLbl, typeLbl, roleLbl;
        public Label posLbl, energyLbl, statusLbl;
        public ImageView profileBg;

        public ImageView turnOff, turnOn;
        public ImageView confOff, confOn;
        public ImageView frzOff,  frzOn;
        public ImageView shldOff, shldOn;
        public ImageView pwrOff,  pwrOn;

        public boolean turnState, confState, frzState, shldState, pwrState;
        public Image   energyBarCurrentImage;
    }
}
