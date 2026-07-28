package game.gui.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class StartController {

    @FXML private StackPane  rootPane;
    @FXML private AnchorPane mainPane;
    @FXML private ImageView  backgroundImage;
    @FXML private ImageView  logoImage;
    @FXML private StackPane  logoPane;
    @FXML private ImageView  scarerButton;
    @FXML private ImageView  laugherButton;
    @FXML private ImageView  instructionsNote;
    @FXML private ImageView  exitButton;
    @FXML private ImageView  creditsButton;
    @FXML private ImageView  optionsButton;
    @FXML private ImageView  bottomLeftPanel;
    @FXML private HBox       centerRow;
    @FXML private HBox       bottomButtonsRow;
    @FXML private StackPane  optionsPopup;
    @FXML private StackPane  creditsPopup;
    @FXML private Slider     volumeSlider;
    @FXML private Text       volumeLabel;
    @FXML private Button     muteButton;

    private boolean isMuted = false;
    private Canvas  beamsCanvas;
    private double  beamAngle = 0;
    private AnimationTimer beamTimer;

    private static final String IMG = "/game/gui/resources/images/";

    // ── Static image cache — loaded once, reused on every visit to StartScreen ──
    private static Image imgBackground;
    private static Image imgLogo;
    private static Image imgScarer;
    private static Image imgLaugher;
    private static Image imgInstructions;
    private static Image imgBottomPanel;
    private static Image imgExit;
    private static Image imgOptions;
    private static Image imgCredits;
    private static boolean imagesLoaded = false;

    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {
        loadImagesOnce();
        applyImages();
        setupLayout();
        setupLogoBeams();
        setupAudio();
        setupHoverEffects();
        setupMonsterDrawerTab();
        setupCellGuideButton();
        SoundManager.getInstance().preloadAll();
    }

    // =========================================================
    //  LOAD IMAGES — only on first visit
    // =========================================================
    private void loadImagesOnce() {
        if (imagesLoaded) return;
        imagesLoaded = true;

        // Background loaded at screen resolution to save heap
        imgBackground   = loadSmall(IMG + "StartScreen_Background.png", 1280, 720);
        imgLogo         = load(IMG + "DoorDash_Logo.png");
        imgScarer       = load(IMG + "scarer_button.png");
        imgLaugher      = load(IMG + "laugher_button.png");
        imgInstructions = load(IMG + "Instructions_Note.png");
        imgBottomPanel  = load(IMG + "Bottom_Left_Panel_Start_Screen.png");
        imgExit         = load(IMG + "Exit_Button.png");
        imgOptions      = load(IMG + "Options_Button.png");
        imgCredits      = load(IMG + "Credits_Button.png");
    }

    private void applyImages() {
        set(backgroundImage,   imgBackground);
        set(logoImage,         imgLogo);
        set(scarerButton,      imgScarer);
        set(laugherButton,     imgLaugher);
        set(instructionsNote,  imgInstructions);
        set(bottomLeftPanel,   imgBottomPanel);
        set(exitButton,        imgExit);
        set(optionsButton,     imgOptions);
        set(creditsButton,     imgCredits);
    }

    private void set(ImageView iv, Image img) {
        if (iv != null && img != null) iv.setImage(img);
    }

    // =========================================================
    //  LAYOUT
    // =========================================================
    private void setupLayout() {
        // Background fills the ACTUAL window exactly — no scale transform,
        // same treatment as the game screen's decorative backdrop.
        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        // Everything else is built at a FIXED 1280x720 reference size —
        // literally the same numbers regardless of window size — and
        // then the whole thing is scaled as one rigid unit via a real
        // Scale transform in applyResponsivePositions(). This is what
        // guarantees zero relative drift between elements: it's not
        // manual arithmetic doing the resizing, it's JavaFX's own
        // transform math on a single subtree, which by definition can't
        // shift internal proportions. Enlarging really is now "just
        // making the picture bigger," like scaling one flat image.
        mainPane.setMinWidth(REF_W);  mainPane.setPrefWidth(REF_W);  mainPane.setMaxWidth(REF_W);
        mainPane.setMinHeight(REF_H); mainPane.setPrefHeight(REF_H); mainPane.setMaxHeight(REF_H);

        logoImage.setFitWidth(REF_W * 0.35);
        logoImage.setPreserveRatio(true);

        logoPane.setClip(null);
        logoPane.setPickOnBounds(false);
        logoPane.setMouseTransparent(true);
        // FXML set TOP_CENTER alignment + a fixed translateY as a way to
        // position the logo — that combination doesn't scale cleanly
        // (translateY is a raw offset applied AFTER any scale, so it
        // doesn't shrink/grow with everything else). Using plain CENTER
        // alignment and setting translateY ourselves (scaled) each
        // resize in applyResponsivePositions() keeps it consistent.
        StackPane.setAlignment(logoPane, Pos.CENTER);

        scarerButton.setFitHeight(REF_H * 0.38);
        laugherButton.setFitHeight(REF_H * 0.38);
        instructionsNote.setFitHeight(REF_H * 0.46);

        bottomLeftPanel.setFitWidth(REF_W * 0.36);
        bottomLeftPanel.setFitHeight(REF_H * 0.1);

        for (ImageView btn : new ImageView[]{exitButton, optionsButton, creditsButton}) {
            btn.setFitWidth(REF_W * 0.10);
            btn.setFitHeight(REF_H * 0.10);
        }

        AnchorPane.setBottomAnchor(bottomLeftPanel,  0.0);
        AnchorPane.setLeftAnchor(bottomLeftPanel,    0.0);

        rootPane.widthProperty().addListener((obs, o, n) -> applyResponsivePositions());
        rootPane.heightProperty().addListener((obs, o, n) -> applyResponsivePositions());
        applyResponsivePositions();

        addDropShadow(instructionsNote, Color.BLACK, 18, 0.5);
        addDropShadow(bottomLeftPanel,  Color.BLACK, 12, 0.4);
    }

    // Reference values below were tuned at the 1280x720 design size —
    // same reference used throughout the rest of the game's UI.
    private static final double REF_W = 1280;
    private static final double REF_H = 720;
    private static final double REF_CENTER_ROW_TOP    = 240;
    private static final double REF_CENTER_ROW_BOTTOM = 130;
    private static final double REF_LOGO_TRANSLATE_Y  = 200;
    private static final double REF_BOTTOM_BTN_BOTTOM = 10;
    private static final double REF_BOTTOM_BTN_LEFT   = 8;

    // =========================================================
    //  PER-ELEMENT MOVE OFFSETS — START SCREEN
    //  Same idea as the game screen: change any X/Y pair below and
    //  rebuild. Units are reference pixels (1280x720 scale). Positive
    //  X = right, positive Y = down. 0/0 = default position.
    // =========================================================
    private static final double OFFSET_X_LOGO         = 0, OFFSET_Y_LOGO         = 0; // logo + light beams together
    private static final double OFFSET_X_CENTER_ROW   = 0, OFFSET_Y_CENTER_ROW   = 0; // whole scarer/laugher/instructions row
    private static final double OFFSET_X_SCARER_BTN   = 0, OFFSET_Y_SCARER_BTN   = 0;
    private static final double OFFSET_X_LAUGHER_BTN  = 0, OFFSET_Y_LAUGHER_BTN  = 0;
    private static final double OFFSET_X_INSTRUCTIONS = 0, OFFSET_Y_INSTRUCTIONS = 0;
    private static final double OFFSET_X_BOTTOM_PANEL = 0, OFFSET_Y_BOTTOM_PANEL = 0; // bottom-left plaque image
    private static final double OFFSET_X_BOTTOM_ROW   = 0, OFFSET_Y_BOTTOM_ROW   = 10; // whole exit/options/credits row
    private static final double OFFSET_X_EXIT_BTN     = 0, OFFSET_Y_EXIT_BTN     = 0;
    private static final double OFFSET_X_OPTIONS_BTN  = 0, OFFSET_Y_OPTIONS_BTN  = 0;
    private static final double OFFSET_X_CREDITS_BTN  = 0, OFFSET_Y_CREDITS_BTN  = 0;

    private void applyResponsivePositions() {
        double w = rootPane.getWidth();
        double h = rootPane.getHeight();
        if (w == 0 || h == 0) return;

        double scale = Math.min(w / REF_W, h / REF_H);

        // Anchors below are FIXED reference values — never multiplied by
        // scale — because mainPane itself is a fixed 1280x720 box that
        // gets scaled as a whole afterward. This is the "scale the whole
        // picture" approach: no per-element arithmetic to drift.
        AnchorPane.setTopAnchor(centerRow,    REF_CENTER_ROW_TOP);
        AnchorPane.setBottomAnchor(centerRow, REF_CENTER_ROW_BOTTOM);
        offset(centerRow, OFFSET_X_CENTER_ROW, OFFSET_Y_CENTER_ROW);
        offset(scarerButton,     OFFSET_X_SCARER_BTN,  OFFSET_Y_SCARER_BTN);
        offset(laugherButton,    OFFSET_X_LAUGHER_BTN, OFFSET_Y_LAUGHER_BTN);
        offset(instructionsNote, OFFSET_X_INSTRUCTIONS, OFFSET_Y_INSTRUCTIONS);

        offset(bottomLeftPanel, OFFSET_X_BOTTOM_PANEL, OFFSET_Y_BOTTOM_PANEL);

        AnchorPane.setBottomAnchor(bottomButtonsRow, REF_BOTTOM_BTN_BOTTOM);
        AnchorPane.setLeftAnchor(bottomButtonsRow,   REF_BOTTOM_BTN_LEFT);
        offset(bottomButtonsRow, OFFSET_X_BOTTOM_ROW, OFFSET_Y_BOTTOM_ROW);
        offset(exitButton,    OFFSET_X_EXIT_BTN,    OFFSET_Y_EXIT_BTN);
        offset(optionsButton, OFFSET_X_OPTIONS_BTN, OFFSET_Y_OPTIONS_BTN);
        offset(creditsButton, OFFSET_X_CREDITS_BTN, OFFSET_Y_CREDITS_BTN);

        // ── THE scale transform ──────────────────────────────────────
        // mainPane and logoPane are separate StackPane children of
        // rootPane, each built at the fixed reference size — StackPane
        // centers each independently, so applying the identical scale
        // to both keeps them perfectly coincident.
        mainPane.setScaleX(scale);
        mainPane.setScaleY(scale);
        logoPane.setScaleX(scale);
        logoPane.setScaleY(scale);
        // translateY is applied AFTER scale in JavaFX's transform order,
        // so it must be scaled explicitly too, or the logo's vertical
        // offset would stay a fixed raw pixel amount while everything
        // else around it shrinks/grows — exactly the kind of drift this
        // whole approach is meant to eliminate. Same reasoning applies to
        // OFFSET_X_LOGO/OFFSET_Y_LOGO, since logoPane carries its own
        // separate scale transform rather than inheriting one from a
        // parent (unlike everything above, which is safe to leave
        // unscaled since it's all inside mainPane's own scaled subtree).
        logoPane.setTranslateX(OFFSET_X_LOGO * scale);
        logoPane.setTranslateY((-REF_LOGO_TRANSLATE_Y + OFFSET_Y_LOGO) * scale);
    }

    /** Moves a node by a fixed reference-pixel offset without affecting anything else's layout. */
    private void offset(javafx.scene.Node n, double x, double y) {
        if (n == null) return;
        n.setTranslateX(x);
        n.setTranslateY(y);
    }

    // =========================================================
    //  AUDIO
    // =========================================================
    private void setupAudio() {
        double savedVol = SceneManager.getInstance().getMusicVolume();
        MediaPlayer mp = SceneManager.getInstance().getMediaPlayer();
        if (mp != null) mp.setVolume(savedVol);

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            if (!isMuted) SceneManager.getInstance().setMusicVolume(vol);
            volumeLabel.setText((int)(vol * 100) + "%");
        });
        volumeSlider.setValue(savedVol);
        volumeLabel.setText((int)(savedVol * 100) + "%");
    }

    // =========================================================
    //  HOVER EFFECTS
    // =========================================================
    private void setupHoverEffects() {
        addHoverEffect(scarerButton,  Color.RED);
        addHoverEffect(laugherButton, Color.YELLOW);
        addHoverEffect(exitButton,    Color.web("#FF4444"));
        addHoverEffect(creditsButton, Color.GOLD);
        addHoverEffect(optionsButton, Color.CYAN);
    }

    // =========================================================
    //  LOGO BEAMS
    // =========================================================
    private void setupLogoBeams() {
        beamsCanvas = new Canvas(1280, 720);
        logoPane.getChildren().add(0, beamsCanvas);

        DropShadow logoGlow = new DropShadow();
        logoGlow.setColor(Color.web("#FFD700"));
        logoGlow.setRadius(35);
        logoGlow.setSpread(0.25);
        Glow glow = new Glow(0.5);
        logoGlow.setInput(glow);
        logoImage.setEffect(logoGlow);

        beamTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                beamAngle += 0.25;
                drawBeams();
            }
        };
        beamTimer.start();

        // setupLayout() ran before this method created beamsCanvas, so its
        // resize listener couldn't size it yet — do that now that it exists.
        applyResponsivePositions();

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && beamTimer != null) {
                beamTimer.stop();
            }
        });
    }

    private void drawBeams() {
        GraphicsContext gc = beamsCanvas.getGraphicsContext2D();
        double w = beamsCanvas.getWidth(), h = beamsCanvas.getHeight();
        double cx = w / 2, cy = h / 2;
        gc.clearRect(0, 0, w, h);

        int numBeams = 12;
        double[] beamWidths = {9, 5, 9, 5, 9, 5, 9, 5, 9, 5, 9, 5};
        double lenX = w * 0.4, lenY = h * 0.28;

        for (int i = 0; i < numBeams; i++) {
            double angle     = Math.toRadians(beamAngle + (360.0 / numBeams) * i);
            double halfAngle = Math.toRadians(beamWidths[i] / 2.0);
            double tipX = cx + Math.cos(angle) * lenX;
            double tipY = cy + Math.sin(angle) * lenY;
            double x1 = cx + Math.cos(angle - halfAngle) * lenX;
            double y1 = cy + Math.sin(angle - halfAngle) * lenY;
            double x2 = cx + Math.cos(angle + halfAngle) * lenX;
            double y2 = cy + Math.sin(angle + halfAngle) * lenY;

            LinearGradient gradient = new LinearGradient(
                cx / w, cy / h, tipX / w, tipY / h, true, CycleMethod.NO_CYCLE,
                new Stop(0,    Color.color(1, 0.82, 0, 0.50)),
                new Stop(0.4,  Color.color(1, 0.82, 0, 0.22)),
                new Stop(0.75, Color.color(1, 0.82, 0, 0.07)),
                new Stop(1,    Color.color(1, 0.82, 0, 0)));

            gc.setFill(gradient);
            gc.beginPath();
            gc.moveTo(cx, cy);
            gc.lineTo(x1, y1);
            gc.lineTo(x2, y2);
            gc.closePath();
            gc.fill();
        }
    }

    // =========================================================
    //  BUTTON HANDLERS
    // =========================================================
    @FXML private void handlePlayAsScarer(MouseEvent e)  { SceneManager.getInstance().startGameScreen(game.engine.Role.SCARER); }
    @FXML private void handlePlayAsLaugher(MouseEvent e) { SceneManager.getInstance().startGameScreen(game.engine.Role.LAUGHER); }
    @FXML private void handleOptions(MouseEvent e)  { closeAllPopups(); optionsPopup.setVisible(true); optionsPopup.toFront(); }
    @FXML private void handleCloseOptions()         { optionsPopup.setVisible(false); }
    @FXML private void handleCredits(MouseEvent e)  { closeAllPopups(); creditsPopup.setVisible(true); creditsPopup.toFront(); }
    @FXML private void handleCloseCredits()         { creditsPopup.setVisible(false); }
    @FXML private void handleExit(MouseEvent e)     { System.exit(0); }

    private void closeAllPopups() {
        optionsPopup.setVisible(false);
        creditsPopup.setVisible(false);
        closeMonsterGuide();
        closeCellGuide();
    }

    @FXML private void handleMute() {
        MediaPlayer mp = SceneManager.getInstance().getMediaPlayer();
        if (mp == null) return;
        isMuted = !isMuted;
        if (isMuted) { mp.setVolume(0); muteButton.setText("🔊 UNMUTE"); }
        else         { mp.setVolume(volumeSlider.getValue()); muteButton.setText("🔇 MUTE"); }
    }

    // =========================================================
    //  IMAGE LOADING HELPERS
    // =========================================================

    /** Load at full resolution (for small images like buttons/logos). */
    private Image load(String path) {
        try {
            java.io.InputStream s = getClass().getResourceAsStream(path);
            if (s == null) { System.err.println("NOT FOUND: " + path); return null; }
            return new Image(s);
        } catch (OutOfMemoryError oom) {
            System.err.println("OOM loading " + path + " — add -Xmx512m to VM args");
            return null;
        }
    }

    /** Load at reduced resolution (for large backgrounds). */
    private Image loadSmall(String path, int w, int h) {
        try {
            java.io.InputStream s = getClass().getResourceAsStream(path);
            if (s == null) { System.err.println("NOT FOUND: " + path); return null; }
            return new Image(s, w, h, false, true);
        } catch (OutOfMemoryError oom) {
            System.err.println("OOM loading background — add -Xmx512m to VM args");
            return null;
        }
    }

    // =========================================================
    //  HOVER / SHADOW HELPERS
    // =========================================================
    private void addHoverEffect(ImageView button, Color glowColor) {
        DropShadow glow = new DropShadow();
        glow.setColor(glowColor); glow.setRadius(25); glow.setSpread(0.6);
        button.setOnMouseEntered(e -> { button.setEffect(glow); playScale(button, 1.0, 1.08, 150); });
        button.setOnMouseExited(e  -> { addDropShadow(button, Color.BLACK, 15, 0.4); playScale(button, 1.08, 1.0, 150); });
    }

    private void addDropShadow(ImageView target, Color color, double radius, double spread) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(color); shadow.setRadius(radius); shadow.setSpread(spread);
        target.setEffect(shadow);
    }

    private void playScale(ImageView target, double from, double to, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), target);
        st.setFromX(from); st.setFromY(from); st.setToX(to); st.setToY(to);
        st.play();
    }

    // =========================================================
    //  MONSTER GUIDE  (NEW)
    // -----------------------------------------------------------
    //  Each monster belongs to a FIXED team (Scarer or Laugher) and has
    //  one Monster Type (Dasher / Dynamo / MultiTasker / Schemer) that
    //  determines its passive trait and active powerup. Both teams use
    //  the plain character-portrait art (ImageLoader's "Monster portraits
    //  - Scarer" section) rather than the "*_Screen.png" art (which is
    //  the in-game laugher screen graphic, not the monster itself).
    // =========================================================
    private static final class MonsterInfo {
        final String name, image, type, personality, ability;
        final int energy;
        MonsterInfo(String name, String image, String type, int energy, String personality, String ability) {
            this.name = name; this.image = image; this.type = type;
            this.energy = energy; this.personality = personality; this.ability = ability;
        }
    }

    private static final String ABILITY_DASHER =
        "DASHER — Lightning Movement: dice movement is doubled (2x speed).\n" +
        "Powerup — Momentum Rush: 3x movement speed for the next 3 turns (replaces the passive while active).";
    private static final String ABILITY_DYNAMO =
        "DYNAMO — Energy Amplification: all energy gained or lost is doubled (2x) — a double-edged sword.\n" +
        "Powerup — Energy Freeze: freezes the opponent, forcing them to skip their entire next turn.";
    private static final String ABILITY_MULTITASKER =
        "MULTITASKER — Movement & Energy: dice movement is halved, but every energy gain or loss gets a +200 bonus.\n" +
        "Powerup — Focus Mode: moves at normal speed for the next 2 turns while keeping the +200 energy bonus.";
    private static final String ABILITY_SCHEMER =
        "SCHEMER — Energy Manipulation: every energy change (gain or loss) gets a +10 bonus.\n" +
        "Powerup — Chain Attack: steals 10 energy (or their total, if less) from every other monster on the board, ignoring shields, gained all at once.";

    private static final MonsterInfo[] SCARERS = {
        new MonsterInfo("James P. Sullivan", "James sullivan.png", "Dynamo", 300,
            "The top scarer—powerful and confident", ABILITY_DYNAMO),
        new MonsterInfo("Randall Boggs", "Randall.png", "Schemer", 20,
            "Sneaky and cunning—always has an angle", ABILITY_SCHEMER),
        new MonsterInfo("Roz", "Roz.png", "MultiTasker", 100,
            "Always watching—nothing escapes her notice", ABILITY_MULTITASKER),
        new MonsterInfo("Henry J. Waternoose", "Henry_J._Waternoose_III.png", "Schemer", 70,
            "Witty and strategic CEO", ABILITY_SCHEMER)
    };

    private static final MonsterInfo[] LAUGHERS = {
        new MonsterInfo("Mike Wazowski", "Mike_Wazowski.png", "Dasher", 100,
            "Fast and funny—the comedy speedster", ABILITY_DASHER),
        new MonsterInfo("Celia Mae", "celia mae.png", "MultiTasker", 50,
            "Organized receptionist—handles everything", ABILITY_MULTITASKER),
        new MonsterInfo("Fungus", "Fungus.png", "Dasher", 50,
            "Timid assistant—quick but nervous", ABILITY_DASHER),
        new MonsterInfo("Yeti", "Yeti.png", "Dynamo", 100,
            "Banished snow monster—surprisingly cheerful", ABILITY_DYNAMO)
    };

    // ── Monster drawer state ─────────────────────────────────────────
    private StackPane monsterDrawerOverlay;   // dim backdrop, null when closed
    private VBox       monsterDrawerCard;     // the sliding panel itself
    private Label       monsterTabArrow;      // arrow glyph, flips direction open/closed
    private boolean     monsterDrawerOpen = false;
    // Large fixed off-screen offset (bigger than any reasonable window
    // width) so the slide-in animation starts fully off-screen no matter
    // how big the window is, now that the panel fills the whole screen.
    private static final double DRAWER_HIDDEN_X = -2400;

    /**
     * Builds a small arrow tab pinned to the left edge of the window
     * (vertically centered, always on top, independent of the scaled
     * 1280x720 subtree so it stays put at any window size). Clicking /
     * "pulling" it slides the monster guide in from the left; clicking
     * it again (arrow now points back) slides it away. Replaces the old
     * centered "MONSTERS" button + modal popup.
     */
    private void setupMonsterDrawerTab() {
        monsterTabArrow = new Label("\u25B6"); // ▶
        monsterTabArrow.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-text-fill: #ffdd55;" +
            "-fx-font-weight: bold;");

        StackPane tab = new StackPane(monsterTabArrow);
        tab.setPrefSize(30, 74);
        tab.setMaxSize(30, 74);
        tab.setStyle(
            "-fx-background-color: rgba(20,20,30,0.75);" +
            "-fx-border-color: #c9a227;" +
            "-fx-border-width: 2 2 2 0;" +
            "-fx-background-radius: 0 12 12 0;" +
            "-fx-border-radius: 0 12 12 0;" +
            "-fx-cursor: hand;");

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#c9a227"));
        glow.setRadius(16);
        glow.setSpread(0.4);
        tab.setOnMouseEntered(e -> { tab.setEffect(glow); playScaleRegion(tab, 1.0, 1.08, 120); });
        tab.setOnMouseExited(e  -> { tab.setEffect(null); playScaleRegion(tab, 1.08, 1.0, 120); });
        tab.setOnMouseClicked(e -> toggleMonsterDrawer());

        StackPane.setAlignment(tab, Pos.CENTER_LEFT);
        rootPane.getChildren().add(tab);
        tab.toFront();
    }

    private void toggleMonsterDrawer() {
        if (monsterDrawerOpen) {
            closeMonsterGuide();
        } else {
            closeAllPopups();
            openMonsterDrawer();
        }
    }

    private void playScaleRegion(javafx.scene.layout.Region target, double from, double to, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), target);
        st.setFromX(from); st.setFromY(from); st.setToX(to); st.setToY(to);
        st.play();
    }

    /**
     * Slides the monster guide in from the left edge to fill the whole
     * screen. LAUGHERS are listed down the left side, SCARERS down the
     * right side, with a vertical divider separating the two teams. All
     * 8 monsters are visible at once — no ScrollPane.
     */
    private void openMonsterDrawer() {
        if (monsterDrawerOverlay != null) return;
        monsterDrawerOpen = true;
        monsterTabArrow.setText("\u25C0"); // ◀ — pull it back to close

        VBox laugherCol = new VBox(14, guideColumnHeader("LAUGHERS", "#ffee88"));
        laugherCol.setAlignment(Pos.TOP_CENTER);
        for (MonsterInfo m : LAUGHERS) laugherCol.getChildren().add(buildMonsterGuideEntry(m, "#ffee99", "#ffee88"));
        HBox.setHgrow(laugherCol, Priority.ALWAYS);

        VBox scarerCol = new VBox(14, guideColumnHeader("SCARERS", "#ff8888"));
        scarerCol.setAlignment(Pos.TOP_CENTER);
        for (MonsterInfo m : SCARERS) scarerCol.getChildren().add(buildMonsterGuideEntry(m, "#ff9999", "#ff8888"));
        HBox.setHgrow(scarerCol, Priority.ALWAYS);

        Region divider = new Region();
        divider.setPrefWidth(2);
        divider.setStyle("-fx-background-color: #c9a227;");
        divider.setOpacity(0.6);

        HBox columns = new HBox(28, laugherCol, divider, scarerCol);
        columns.setAlignment(Pos.TOP_CENTER);
        columns.setFillHeight(true);
        VBox.setVgrow(columns, Priority.ALWAYS);

        Label title = new Label("MONSTER GUIDE");
        title.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ffdd55;");

        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #dddddd;" +
            "-fx-background-color: #1a1a1a;" +
            "-fx-border-color: #888888;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 26;" +
            "-fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setOpacity(0.8));
        closeBtn.setOnMouseExited(e  -> closeBtn.setOpacity(1.0));
        closeBtn.setOnAction(e -> closeMonsterGuide());

        VBox card = new VBox(18, title, columns, closeBtn);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(28, 40, 24, 40));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom, rgba(16,16,24,0.97), rgba(6,6,12,0.99));" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #c9a227;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;");
        DropShadow cardShadow = new DropShadow();
        cardShadow.setRadius(30);
        cardShadow.setColor(Color.BLACK);
        card.setEffect(cardShadow);
        card.setOnMouseClicked(javafx.event.Event::consume);
        card.setTranslateX(DRAWER_HIDDEN_X);

        StackPane overlay = new StackPane(card);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.setPickOnBounds(true);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(card, Pos.CENTER);
        StackPane.setMargin(card, new Insets(24));
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) closeMonsterGuide(); });
        overlay.setOpacity(0);

        rootPane.getChildren().add(overlay);
        overlay.toFront();
        monsterDrawerOverlay = overlay;
        monsterDrawerCard = card;

        FadeTransition fade = new FadeTransition(Duration.millis(180), overlay);
        fade.setFromValue(0); fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(280), card);
        slide.setFromX(DRAWER_HIDDEN_X);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();
    }

    private void closeMonsterGuide() {
        if (monsterDrawerOverlay == null) return;
        monsterDrawerOpen = false;
        monsterTabArrow.setText("\u25B6"); // ▶

        final StackPane overlay = monsterDrawerOverlay;
        final VBox card = monsterDrawerCard;
        monsterDrawerOverlay = null;
        monsterDrawerCard = null;

        FadeTransition fade = new FadeTransition(Duration.millis(160), overlay);
        fade.setFromValue(overlay.getOpacity()); fade.setToValue(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(220), card);
        slide.setFromX(card.getTranslateX());
        slide.setToX(DRAWER_HIDDEN_X);
        slide.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition close = new ParallelTransition(fade, slide);
        close.setOnFinished(e -> rootPane.getChildren().remove(overlay));
        close.play();
    }

    private Label guideColumnHeader(String text, String color) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + color + ";");
        return l;
    }

    /** One monster's portrait + name + type/energy + ability blurb, framed with an outline, for the full-screen guide. */
    private HBox buildMonsterGuideEntry(MonsterInfo m, String nameColor, String outlineColor) {
        ImageView iv = new ImageView(GameUIHelper.loadImage(m.image));
        iv.setPreserveRatio(true);
        iv.setFitWidth(92);
        DropShadow ds = new DropShadow();
        ds.setRadius(10);
        ds.setColor(Color.BLACK);
        iv.setEffect(ds);

        Label nameLbl = new Label(m.name);
        nameLbl.setWrapText(true);
        nameLbl.setAlignment(Pos.CENTER_LEFT);
        nameLbl.setTextAlignment(TextAlignment.LEFT);
        nameLbl.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + nameColor + ";");

        Label typeLbl = new Label(m.type.toUpperCase() + "  •  " + m.energy + " ENERGY");
        typeLbl.setWrapText(true);
        typeLbl.setAlignment(Pos.CENTER_LEFT);
        typeLbl.setTextAlignment(TextAlignment.LEFT);
        typeLbl.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #c9a227;");

        Label personalityLbl = new Label(m.personality);
        personalityLbl.setWrapText(true);
        personalityLbl.setAlignment(Pos.CENTER_LEFT);
        personalityLbl.setTextAlignment(TextAlignment.LEFT);
        personalityLbl.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 10px;" +
            "-fx-font-style: italic;" +
            "-fx-text-fill: #999999;");

        Label abilityLbl = new Label(m.ability);
        abilityLbl.setWrapText(true);
        abilityLbl.setAlignment(Pos.CENTER_LEFT);
        abilityLbl.setTextAlignment(TextAlignment.LEFT);
        abilityLbl.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 10.5px;" +
            "-fx-text-fill: #cccccc;");

        VBox textBox = new VBox(3, nameLbl, typeLbl, personalityLbl, abilityLbl);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox row = new HBox(14, iv, textBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 14));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + outlineColor + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;");
        return row;
    }

    // =========================================================
    //  BOARD CELL GUIDE  (NEW)
    // -----------------------------------------------------------
    //  A blue "?" badge pinned to the top-right corner of the window
    //  (same fixed, unscaled placement approach as the monster tab).
    //  Clicking it opens a centered modal — the same visual style the
    //  monster guide used to use — listing every board-cell type with
    //  its image and a short description. Laid out in a FlowPane so
    //  every cell fits on screen at once with no scrolling.
    // =========================================================
    private static final class CellInfo {
        final String name, image, description;
        CellInfo(String name, String image, String description) {
            this.name = name; this.image = image; this.description = description;
        }
    }

    private static final CellInfo[] CELLS = {
        new CellInfo("Normal Cell", "NormalCell.png",
            "An empty space on the board — no special effect, just a step along the path."),
        new CellInfo("Scarer Door (Locked)", "Scarer_ClosedDoor_Cell2.png",
            "A locked door on the Scarer path. The number shown is the energy needed to open it."),
        new CellInfo("Scarer Door (Open)", "Scarer_OpenDoor_Cell.png",
            "An unlocked Scarer door — already activated, so it's safe to pass through."),
        new CellInfo("Laugher Door (Locked)", "Laugher_ClosedDoor_Cell.png",
            "A locked door on the Laugher path. The number shown is the energy needed to open it."),
        new CellInfo("Laugher Door (Open)", "Laugher_OpenDoor_Cell.png",
            "An unlocked Laugher door — already activated, so it's safe to pass through."),
        new CellInfo("Monster Cell", "MonsterCell_Grey.png",
            "A station where a monster is holding position. Tap it in-game to see that monster's full stats."),
        new CellInfo("Conveyor Belt", "Conveyor_belt_cell.png",
            "Automatically carries any monster that lands here forward or backward. Tap it to preview where it leads."),
        new CellInfo("Contamination Sock", "contamination_sock_cell.png",
            "A trap cell that flings a monster to a different spot on the board. Tap it to preview its destination."),
        new CellInfo("Card Cell", "CardCell.png",
            "Landing here draws a random card from the deck, triggering its special effect.")
    };

    private StackPane cellGuideOverlay;

    /** Builds the blue "?" badge and pins it to the top-right corner. */
    private void setupCellGuideButton() {
        Label q = new Label("?");
        q.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;");

        StackPane badge = new StackPane(q);
        badge.setPrefSize(42, 42);
        badge.setMaxSize(42, 42);
        badge.setStyle(
            "-fx-background-color: #2b7fe0;" +
            "-fx-background-radius: 999;" +
            "-fx-border-color: #cfe4ff;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 999;" +
            "-fx-cursor: hand;");

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#2b7fe0"));
        glow.setRadius(18);
        glow.setSpread(0.5);
        badge.setOnMouseEntered(e -> { badge.setEffect(glow); playScaleRegion(badge, 1.0, 1.1, 120); });
        badge.setOnMouseExited(e  -> { badge.setEffect(null); playScaleRegion(badge, 1.1, 1.0, 120); });
        badge.setOnMouseClicked(e -> { closeAllPopups(); showCellGuide(); });

        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(18, 18, 0, 0));
        rootPane.getChildren().add(badge);
        badge.toFront();
    }

    private void showCellGuide() {
        if (cellGuideOverlay != null) return;

        FlowPane grid = new FlowPane(20, 20);
        grid.setPrefWrapLength(780);
        grid.setAlignment(Pos.CENTER);
        for (CellInfo c : CELLS) grid.getChildren().add(buildCellGuideEntry(c));

        Label title = new Label("BOARD CELL GUIDE");
        title.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #8ec4ff;");

        Button closeBtn = new Button("CLOSE");
        closeBtn.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #dddddd;" +
            "-fx-background-color: #1a1a1a;" +
            "-fx-border-color: #888888;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 26;" +
            "-fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setOpacity(0.8));
        closeBtn.setOnMouseExited(e  -> closeBtn.setOpacity(1.0));
        closeBtn.setOnAction(e -> closeCellGuide());

        VBox card = new VBox(16, title, grid, closeBtn);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(24));
        card.setMaxWidth(840);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom, rgba(16,16,24,0.97), rgba(6,6,12,0.99));" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #2b7fe0;" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;");
        DropShadow cardShadow = new DropShadow();
        cardShadow.setRadius(30);
        cardShadow.setColor(Color.BLACK);
        card.setEffect(cardShadow);
        card.setOnMouseClicked(javafx.event.Event::consume);

        cellGuideOverlay = new StackPane(card);
        cellGuideOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);");
        cellGuideOverlay.setPickOnBounds(true);
        cellGuideOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cellGuideOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == cellGuideOverlay) closeCellGuide();
        });
        cellGuideOverlay.setOpacity(0);

        rootPane.getChildren().add(cellGuideOverlay);
        cellGuideOverlay.toFront();

        FadeTransition fade = new FadeTransition(Duration.millis(200), cellGuideOverlay);
        fade.setFromValue(0); fade.setToValue(1);
        fade.play();
    }

    private void closeCellGuide() {
        if (cellGuideOverlay == null) return;
        final StackPane layer = cellGuideOverlay;
        cellGuideOverlay = null;
        FadeTransition fade = new FadeTransition(Duration.millis(160), layer);
        fade.setFromValue(layer.getOpacity()); fade.setToValue(0);
        fade.setOnFinished(e -> rootPane.getChildren().remove(layer));
        fade.play();
    }

    /** One cell type's image + name + short description, sized so all 9 fit on screen with no scrolling. */
    private VBox buildCellGuideEntry(CellInfo c) {
        ImageView iv = new ImageView(GameUIHelper.loadImage(c.image));
        iv.setPreserveRatio(true);
        iv.setFitWidth(84);
        DropShadow ds = new DropShadow();
        ds.setRadius(10);
        ds.setColor(Color.BLACK);
        iv.setEffect(ds);

        Label nameLbl = new Label(c.name);
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(220);
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setTextAlignment(TextAlignment.CENTER);
        nameLbl.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 12.5px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #8ec4ff;");

        Label descLbl = new Label(c.description);
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(220);
        descLbl.setAlignment(Pos.CENTER);
        descLbl.setTextAlignment(TextAlignment.CENTER);
        descLbl.setStyle(
            "-fx-font-family: '" + GameUIConstants.FONT + "';" +
            "-fx-font-size: 10px;" +
            "-fx-text-fill: #cccccc;");

        VBox box = new VBox(6, iv, nameLbl, descLbl);
        box.setAlignment(Pos.TOP_CENTER);
        box.setMaxWidth(230);
        return box;
    }
}