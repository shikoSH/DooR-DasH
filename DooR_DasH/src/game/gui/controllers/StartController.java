package game.gui.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
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
        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());

        mainPane.prefWidthProperty().bind(rootPane.widthProperty());
        mainPane.prefHeightProperty().bind(rootPane.heightProperty());

        logoImage.fitWidthProperty().bind(rootPane.widthProperty().multiply(0.65));
        logoImage.setPreserveRatio(true);

        logoPane.setClip(null);
        logoPane.setPickOnBounds(false);
        logoPane.setMouseTransparent(true);

        scarerButton.fitHeightProperty().bind(rootPane.heightProperty().multiply(0.48));
        laugherButton.fitHeightProperty().bind(rootPane.heightProperty().multiply(0.48));
        instructionsNote.fitHeightProperty().bind(rootPane.heightProperty().multiply(0.56));

        bottomLeftPanel.fitWidthProperty().bind(rootPane.widthProperty().multiply(0.36));
        bottomLeftPanel.fitHeightProperty().bind(rootPane.heightProperty().multiply(0.1));

        for (ImageView btn : new ImageView[]{exitButton, optionsButton, creditsButton}) {
            btn.fitWidthProperty().bind(rootPane.widthProperty().multiply(0.10));
            btn.fitHeightProperty().bind(rootPane.heightProperty().multiply(0.10));
        }

        AnchorPane.setBottomAnchor(bottomLeftPanel,  0.0);
        AnchorPane.setLeftAnchor(bottomLeftPanel,    0.0);
        AnchorPane.setBottomAnchor(bottomButtonsRow, 10.0);
        AnchorPane.setLeftAnchor(bottomButtonsRow,   8.0);

        addDropShadow(instructionsNote, Color.BLACK, 18, 0.5);
        addDropShadow(bottomLeftPanel,  Color.BLACK, 12, 0.4);
    }

    // =========================================================
    //  AUDIO
    // =========================================================
    private void setupAudio() {
        MediaPlayer mp = SceneManager.getInstance().getMediaPlayer();
        if (mp != null) mp.setVolume(0.25);

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            MediaPlayer player = SceneManager.getInstance().getMediaPlayer();
            if (player != null && !isMuted) player.setVolume(vol);
            volumeLabel.setText((int)(vol * 100) + "%");
        });
        volumeSlider.setValue(0.25);
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
}
