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
 * Constructs the player/opponent side-panels and the action-log widget.
 *
 * Profile card text is positioned to match the monster_profile.png artwork:
 *   • Row 1  – MONSTER: <name>  (large slot under "MONSTER:" label in image)
 *   • Row 2  – CLASS:   <type>  (left column)
 *   • Row 3  – FACTION: <role>  (left column, beneath CLASS)
 *   • Row 4  – STATUS:  <value> (bottom STATUS block)
 *
 * Action-log: the ACTION LOG label is painted directly above the image
 * using a StackPane with fixed TOP_LEFT anchoring so it never drifts on resize.
 * The log-line text box is exposed via ActionLogRefs.textBox so the
 * controller can rescale its padding/font in step with the image at runtime.
 */
public final class GamePanelBuilder {

    private GamePanelBuilder() {}

    // =========================================================
    //  PLAYER PANEL
    // =========================================================

    public static PlayerPanelRefs buildPlayerPanel(VBox container, Image en100) {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(4);

        PlayerPanelRefs r = new PlayerPanelRefs();

        // ── Status lights ─────────────────────────────────────────────────
        r.turnOff = makeLight(IMG_TURN_OFF);   r.turnOn  = makeLight(IMG_TURN_ON);
        r.confOff = makeLight(IMG_CONF_OFF);   r.confOn  = makeLight(IMG_CONF_ON);
        r.frzOff  = makeLight(IMG_FREEZE_OFF); r.frzOn   = makeLight(IMG_FREEZE_ON);
        r.shldOff = makeLight(IMG_SHIELD_OFF); r.shldOn  = makeLight(IMG_SHIELD_ON);
        r.pwrOff  = makeLight(IMG_POWER_OFF);  r.pwrOn   = makeLight(IMG_POWER_ON);
        HBox lights = makeLightRow(
            r.turnOff, r.turnOn, r.confOff, r.confOn,
            r.frzOff,  r.frzOn,  r.shldOff, r.shldOn, r.pwrOff, r.pwrOn);
        r.lightsRow = lights;

        // ── Portrait + position badge ─────────────────────────────────────
        r.portrait.setPreserveRatio(true);
        addDropShadow(r.portrait, 12, Color.BLACK);
        r.posLbl = makeLbl("0", "#00ff88", TXT_PLAYER_POS, true);
        // Black filled pill with a bright green text and glow — clearly readable over any portrait
        r.posLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + (TXT_PLAYER_POS + 2) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #00ff88;" +
            "-fx-background-color: rgba(0,0,0,0.85);" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: #00ff88;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 2 7 2 7;" +
            "-fx-effect: dropshadow(three-pass-box,rgba(0,255,136,0.7),6,0.4,0,0);");
        StackPane portraitPane = new StackPane(r.portrait, r.posLbl);
        r.portraitPane = portraitPane;
        StackPane.setAlignment(r.posLbl, Pos.BOTTOM_LEFT);
        StackPane.setMargin(r.posLbl, new Insets(0, 0, 6, 6));

        // ── Profile card ──────────────────────────────────────────────────
        // The background image has these labelled regions (top→bottom):
        //   "MONSTER:" header → big name slot → CLASS/FACTION rows → STATUS block
        // We overlay a GridPane that places our dynamic labels into each slot.

        r.profileBg = new ImageView(loadImage(IMG_PROFILE));
        r.profileBg.setPreserveRatio(true);

        // Name label — sits in the wide dark box under "MONSTER:"
        r.nameLbl = makeLbl("-", "#e8f4ff", TXT_PLAYER_NAME, true);
        r.nameLbl.setWrapText(true);

        // CLASS row value — right of "CLASS:" text in image
        r.typeLbl = makeLbl("-", "#c8e0ff", TXT_PLAYER_TYPE, false);

        // FACTION row value — right of "FACTION:" text in image
        r.roleLbl = makeLbl("-", "#c8e0ff", TXT_PLAYER_ROLE, false);

        // STATUS value — inside the STATUS block at the bottom
        r.statusLbl = makeLbl("NORMAL", "#99bbdd", TXT_PLAYER_STATUS, false);
        r.statusLbl.setWrapText(true);

        // Build an overlay GridPane that matches the image's internal grid
        // Row 0: name slot (the big dark rectangle)
        // Row 1: CLASS value
        // Row 2: FACTION value
        // Row 3: STATUS value
        GridPane grid = new GridPane();
        grid.setVgap(0);
        grid.setHgap(0);

        // Column 0: left offset (past "CLASS:" / "FACTION:" labels baked into image)
        ColumnConstraints colLeft  = new ColumnConstraints();
        colLeft.setPercentWidth(46); // leave room for CLASS:/FACTION: baked text
        ColumnConstraints colRight = new ColumnConstraints();
        colRight.setPercentWidth(54);
        colRight.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(colLeft, colRight);

        RowConstraints rowName   = new RowConstraints(); rowName.setPercentHeight(30);
        RowConstraints rowClass  = new RowConstraints(); rowClass.setPercentHeight(20);
        RowConstraints rowFact   = new RowConstraints(); rowFact.setPercentHeight(20);
        RowConstraints rowStatus = new RowConstraints(); rowStatus.setPercentHeight(30);
        grid.getRowConstraints().addAll(rowName, rowClass, rowFact, rowStatus);

        // Name spans both columns in the top name slot
        grid.add(r.nameLbl, 0, 0, 2, 1);
        GridPane.setValignment(r.nameLbl, javafx.geometry.VPos.CENTER);
        GridPane.setMargin(r.nameLbl, new Insets(2, 4, 0, 8));

        // CLASS value — right column, row 1
        grid.add(r.typeLbl, 1, 1);
        GridPane.setValignment(r.typeLbl, javafx.geometry.VPos.CENTER);
        GridPane.setMargin(r.typeLbl, new Insets(1, 4, 0, 2));

        // FACTION value — right column, row 2
        grid.add(r.roleLbl, 1, 2);
        GridPane.setValignment(r.roleLbl, javafx.geometry.VPos.CENTER);
        GridPane.setMargin(r.roleLbl, new Insets(1, 4, 0, 2));

        // STATUS value — spans both columns, row 3 (bottom section)
        grid.add(r.statusLbl, 0, 3, 2, 1);
        GridPane.setValignment(r.statusLbl, javafx.geometry.VPos.BOTTOM);
        GridPane.setMargin(r.statusLbl, new Insets(0, 4, 6, 8));

        // Overlay the grid on top of the profile image
        StackPane profilePane = new StackPane(r.profileBg, grid);
        r.profilePane = profilePane;
        StackPane.setAlignment(grid, Pos.TOP_LEFT);

        // ── Energy label ──────────────────────────────────────────────────
        r.energyLbl = makeLbl("-", "#00ff88", TXT_PLAYER_ENERGY, true);
        addGlow(r.energyLbl, Color.web("#00ff88"), 16, 0.6);
        HBox energyRow = new HBox(r.energyLbl);
        r.energyRow = energyRow;
        energyRow.setAlignment(Pos.CENTER_LEFT);
        energyRow.setPadding(new Insets(0, 0, 0, 10));

        // ── Energy bar ────────────────────────────────────────────────────
        r.energyBar.setPreserveRatio(true);
        r.energyBar.setImage(en100);
        r.energyBarCurrentImage = en100;
        r.energyWrapper = new StackPane(r.energyBar);
        r.energyWrapper.setAlignment(Pos.CENTER_LEFT);
        r.energyWrapper.setPadding(new Insets(0, 0, 0, 10));

        // ── Turn label ────────────────────────────────────────────────────
        r.turnLbl = makeLbl("", "#ffcc00", TXT_PLAYER_TURN, true);
        HBox turnRow = new HBox(r.turnLbl);
        r.turnRow = turnRow;
        turnRow.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
            lights, portraitPane, profilePane,
            energyRow, r.energyWrapper, turnRow);

        return r;
    }

    // =========================================================
    //  OPPONENT PANEL
    // =========================================================

    public static OpponentPanelRefs buildOpponentPanel(VBox container, Image en100) {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(4);

        OpponentPanelRefs r = new OpponentPanelRefs();

        // ── Status lights ─────────────────────────────────────────────────
        r.turnOff = makeLight(IMG_TURN_OFF);   r.turnOn  = makeLight(IMG_TURN_ON);
        r.confOff = makeLight(IMG_CONF_OFF);   r.confOn  = makeLight(IMG_CONF_ON);
        r.frzOff  = makeLight(IMG_FREEZE_OFF); r.frzOn   = makeLight(IMG_FREEZE_ON);
        r.shldOff = makeLight(IMG_SHIELD_OFF); r.shldOn  = makeLight(IMG_SHIELD_ON);
        r.pwrOff  = makeLight(IMG_POWER_OFF);  r.pwrOn   = makeLight(IMG_POWER_ON);
        HBox lights = makeLightRow(
            r.turnOff, r.turnOn, r.confOff, r.confOn,
            r.frzOff,  r.frzOn,  r.shldOff, r.shldOn, r.pwrOff, r.pwrOn);
        r.lightsRow = lights;

        // ── Portrait + position badge ─────────────────────────────────────
        r.portrait.setPreserveRatio(true);
        addDropShadow(r.portrait, 12, Color.BLACK);
        r.posLbl = makeLbl("0", "#ff6666", TXT_PLAYER_POS, true);
        r.posLbl.setStyle(
            "-fx-font-family: '" + FONT + "';" +
            "-fx-font-size: " + (TXT_PLAYER_POS + 2) + "px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ff6666;" +
            "-fx-background-color: rgba(0,0,0,0.85);" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: #ff6666;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 2 7 2 7;" +
            "-fx-effect: dropshadow(three-pass-box,rgba(255,102,102,0.7),6,0.4,0,0);");
        StackPane portPane = new StackPane(r.portrait, r.posLbl);
        r.portraitPane = portPane;
        StackPane.setAlignment(r.posLbl, Pos.BOTTOM_LEFT);
        StackPane.setMargin(r.posLbl, new Insets(0, 0, 6, 6));

        // ── Profile card ──────────────────────────────────────────────────
        r.profileBg = new ImageView(loadImage(IMG_PROFILE));
        r.profileBg.setPreserveRatio(true);

        r.nameLbl   = makeLbl("-", "#ffe8e8", TXT_PLAYER_NAME, true);
        r.nameLbl.setWrapText(true);
        r.typeLbl   = makeLbl("-", "#ffc8c8", TXT_PLAYER_TYPE, false);
        r.roleLbl   = makeLbl("-", "#ffc8c8", TXT_PLAYER_ROLE, false);
        r.statusLbl = makeLbl("NORMAL", "#ddbbbb", TXT_PLAYER_STATUS, false);
        r.statusLbl.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setVgap(0);
        grid.setHgap(0);

        ColumnConstraints colLeft  = new ColumnConstraints();
        colLeft.setPercentWidth(46);
        ColumnConstraints colRight = new ColumnConstraints();
        colRight.setPercentWidth(54);
        colRight.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(colLeft, colRight);

        RowConstraints rowName   = new RowConstraints(); rowName.setPercentHeight(30);
        RowConstraints rowClass  = new RowConstraints(); rowClass.setPercentHeight(20);
        RowConstraints rowFact   = new RowConstraints(); rowFact.setPercentHeight(20);
        RowConstraints rowStatus = new RowConstraints(); rowStatus.setPercentHeight(30);
        grid.getRowConstraints().addAll(rowName, rowClass, rowFact, rowStatus);

        grid.add(r.nameLbl, 0, 0, 2, 1);
        GridPane.setValignment(r.nameLbl, javafx.geometry.VPos.CENTER);
        GridPane.setMargin(r.nameLbl, new Insets(2, 4, 0, 8));

        grid.add(r.typeLbl, 1, 1);
        GridPane.setValignment(r.typeLbl, javafx.geometry.VPos.CENTER);
        GridPane.setMargin(r.typeLbl, new Insets(1, 4, 0, 2));

        grid.add(r.roleLbl, 1, 2);
        GridPane.setValignment(r.roleLbl, javafx.geometry.VPos.CENTER);
        GridPane.setMargin(r.roleLbl, new Insets(1, 4, 0, 2));

        grid.add(r.statusLbl, 0, 3, 2, 1);
        GridPane.setValignment(r.statusLbl, javafx.geometry.VPos.BOTTOM);
        GridPane.setMargin(r.statusLbl, new Insets(0, 4, 6, 8));

        StackPane profilePane = new StackPane(r.profileBg, grid);
        r.profilePane = profilePane;
        StackPane.setAlignment(grid, Pos.TOP_LEFT);

        // ── Energy label ──────────────────────────────────────────────────
        r.energyLbl = makeLbl("-", "#ff6666", TXT_PLAYER_ENERGY, true);
        addGlow(r.energyLbl, Color.web("#ff6666"), 16, 0.6);
        HBox energyRow = new HBox(r.energyLbl);
        r.energyRow = energyRow;
        energyRow.setAlignment(Pos.CENTER_LEFT);
        energyRow.setPadding(new Insets(0, 0, 0, 10));

        // ── Energy bar ────────────────────────────────────────────────────
        r.energyBar.setPreserveRatio(true);
        r.energyBar.setImage(en100);
        r.energyBarCurrentImage = en100;
        r.energyWrapper = new StackPane(r.energyBar);
        r.energyWrapper.setAlignment(Pos.CENTER_LEFT);
        r.energyWrapper.setPadding(new Insets(0, 0, 0, 10));

        container.getChildren().addAll(
            lights, portPane, profilePane,
            energyRow, r.energyWrapper);

        return r;
    }

    // =========================================================
    //  ACTION LOG
    // =========================================================

    /**
     * Builds the action-log widget.
     *
     * Layout (from top to bottom inside the container):
     *   1. A fixed "ACTION LOG" label in the game font — anchored to TOP_LEFT of
     *      the StackPane so it sits directly above the monitor image and never
     *      moves when the window is resized.
     *   2. A StackPane containing the Action_Log.png image as background and
     *      the three scrolling log lines overlaid on the dark screen area.
     *
     * The three log-line labels start with a sane default style, but the
     * controller rescales their font size and the wrapping textBox's padding
     * every layout pass (see ActionLogRefs.textBox) so the text always tracks
     * the image's actual rendered size instead of using fixed pixel values.
     */
    public static ActionLogRefs buildActionLog(VBox container) {
        // ── Background image ──────────────────────────────────────────────
        ImageView bg = new ImageView(loadImage(IMG_ACTION_LOG));
        bg.setPreserveRatio(true);

        // ── Log text lines (appear inside the dark monitor screen) ─────────
        Label line1 = makeLbl("", ACTION_LOG_LINE1_COLOR, TXT_ACTION_LOG, false);
        Label line2 = makeLbl("", ACTION_LOG_LINE2_COLOR, TXT_ACTION_LOG, false);
        Label line3 = makeLbl("", ACTION_LOG_LINE3_COLOR, TXT_ACTION_LOG, false);
        for (Label l : new Label[]{line1, line2, line3}) {
            l.setWrapText(true);
            // Initial fallback sizing — the controller overrides this with a
            // size proportional to the rendered image on the first layout pass.
            l.setStyle(l.getStyle() +
                "-fx-font-size:" + TXT_ACTION_LOG + "px;" +
                "-fx-font-family:'" + FONT + "';");
        }

        VBox logText = new VBox(2, line1, line2, line3);
        logText.setAlignment(Pos.TOP_LEFT);
        // Initial fallback padding — replaced by the controller with values
        // proportional to the rendered image size every layout pass.
        logText.setPadding(new Insets(14, 8, 6, 14));
        logText.setMouseTransparent(true);

        // Image + text overlay
        StackPane logPane = new StackPane(bg, logText);
        StackPane.setAlignment(logText, Pos.TOP_LEFT);

        // ── "ACTION LOG" header label ──────────────────────────────────────
        Label header = new Label("ACTION LOG");
        header.setStyle(
            "-fx-font-family:'" + FONT + "';" +
            "-fx-font-size:11px;" +          // fixed, never scales
            "-fx-font-weight:bold;" +
            "-fx-text-fill:#d4a843;" +        // amber — matches bezel colour in image
            "-fx-letter-spacing: 1;" +
            "-fx-padding: 0 0 2 2;");
        header.setMouseTransparent(true);
        HBox headerRow = new HBox(header);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(0, 0, 0, 2));

        // Put header above the log image in the VBox
        VBox actionLogGroup = new VBox(0, headerRow, logPane);
        actionLogGroup.setAlignment(Pos.TOP_LEFT);

        container.getChildren().add(actionLogGroup);
        container.setStyle("-fx-padding: 4;");

        return new ActionLogRefs(line1, line2, line3, bg, logText);
    }

    // =========================================================
    //  DATA CLASSES
    // =========================================================

    public static class ActionLogRefs {
        public final Label     line1, line2, line3;
        public final ImageView background;
        public final VBox      textBox;   // wraps line1-3; controller rescales its padding to match the image
		public Label headerLbl;

        public ActionLogRefs(Label l1, Label l2, Label l3, ImageView bg, VBox textBox) {
            this.line1 = l1; this.line2 = l2; this.line3 = l3;
            this.background = bg;
            this.textBox = textBox;
        }
    }

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
        public HBox    lightsRow;     // exposed so GameController can move it independently
        public StackPane portraitPane; // portrait + position badge, as one movable unit
        public StackPane profilePane;  // profile card image + its text overlay, as one movable unit
        public HBox    energyRow;      // wraps the energy number label
        public HBox    turnRow;        // wraps the "YOUR TURN" label (player only)
    }

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
        public HBox    lightsRow;     // exposed so GameController can move it independently
        public StackPane portraitPane; // portrait + position badge, as one movable unit
        public StackPane profilePane;  // profile card image + its text overlay, as one movable unit
        public HBox    energyRow;      // wraps the energy number label
    }
}