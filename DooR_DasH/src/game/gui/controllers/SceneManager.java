package game.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SceneManager {

    private Stage primaryStage;
    private final StackPane sceneHolder = new StackPane();
    private Scene persistentScene;
    private final HashMap<String, Parent> cachedRoots = new HashMap<>();
    private MediaPlayer mediaPlayer;
    private double savedMusicVolume = 0.25;
    private boolean fullScreenPromptShown = false;
    private boolean startScreenShownOnce = false;

    private static final double DEFAULT_WIDTH  = 1280;
    private static final double DEFAULT_HEIGHT = 720;

    // Only true while the game screen is showing — this lock is scoped
    // to just this screen. It's the only one that needs the window
    // itself to stay 16:9 for its printed control-panel artwork to fit
    // edge-to-edge; other screens use whatever window size they're
    // given correctly on their own.
    private boolean gameScreenActive = false;
    private boolean adjustingForAspect = false; // re-entrancy guard

    private SceneManager() {}

    /** Thread-safe initialization-on-demand holder. */
    private static final class Holder {
        static final SceneManager INSTANCE = new SceneManager();
    }

    public static SceneManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("DooR DasH: Scare vs Laugh Touchdown");
        this.primaryStage.setWidth(DEFAULT_WIDTH);
        this.primaryStage.setHeight(DEFAULT_HEIGHT);
        this.primaryStage.setMinWidth(960);
        this.primaryStage.setMinHeight(640);
        this.primaryStage.setResizable(true);
        this.primaryStage.centerOnScreen();
        // Disable the built-in ESC key so we manage fullscreen toggling ourselves
        this.primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        this.primaryStage.setFullScreenExitHint("");

        sceneHolder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Black fill so any content swap never flashes the Scene/StackPane default white
        sceneHolder.setStyle("-fx-background-color: black;");
        persistentScene = new Scene(sceneHolder, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        persistentScene.setFill(Color.BLACK);

        // ── Game-screen-only windowed 16:9 lock ─────────────────────────
        // Reacts to the SCENE's own live size (the real content area,
        // immune to guessing window-chrome thickness) rather than a
        // one-shot guess. Fires on every resize and self-corrects using
        // whatever the Stage-to-Scene delta actually is right now.
        persistentScene.widthProperty().addListener((obs, o, n) -> maintainGameAspectRatio());
        persistentScene.heightProperty().addListener((obs, o, n) -> maintainGameAspectRatio());

        // ESC toggles: fullscreen <-> 1280x720 windowed — never exits the game
        persistentScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (primaryStage.isFullScreen()) {
                    primaryStage.setFullScreen(false);
                    // Deferred: setting size immediately after
                    // setFullScreen(false) can race the OS's own
                    // fullscreen-exit animation on some platforms and
                    // silently get dropped, leaving the Scene's content
                    // laid out at its old (fullscreen) size even though
                    // the window itself is now smaller — this is what
                    // made the start/game-over screens look "stuck" at
                    // fullscreen size and cramped in the smaller window.
                    // Running this on the next pulse lets that
                    // transition finish first.
                    Platform.runLater(() -> {
                        primaryStage.setWidth(DEFAULT_WIDTH);
                        primaryStage.setHeight(DEFAULT_HEIGHT);
                        primaryStage.centerOnScreen();
                        sceneHolder.requestLayout();
                    });
                } else {
                    primaryStage.setFullScreen(true);
                }
                event.consume();
            }
        });
        primaryStage.setScene(persistentScene);
        // Start in fullscreen by default
        primaryStage.setFullScreen(true);
    }

    /**
     * Keeps the SCENE's content area (not the Stage's outer frame) at
     * exactly 16:9 while the game screen is showing and the window
     * isn't fullscreen. Only touches Stage height, computed from the
     * width the Scene just reported plus whatever the Stage-to-Scene
     * "chrome" delta happens to be right now.
     */
    private void maintainGameAspectRatio() {
        if (!gameScreenActive || adjustingForAspect || primaryStage == null) return;
        if (primaryStage.isFullScreen()) return;

        double sceneW = persistentScene.getWidth();
        double sceneH = persistentScene.getHeight();
        if (sceneW <= 0 || sceneH <= 0) return;

        double targetRatio  = DEFAULT_WIDTH / DEFAULT_HEIGHT; // 16:9
        double currentRatio = sceneW / sceneH;
        if (Math.abs(currentRatio - targetRatio) < 0.002) return; // already close enough

        adjustingForAspect = true;
        double chromeH = primaryStage.getHeight() - sceneH;
        double targetSceneH = sceneW / targetRatio;
        primaryStage.setHeight(targetSceneH + Math.max(0, chromeH));
        adjustingForAspect = false;
    }

    // ===== MUSIC =====

    public void startMusic() {
        try {
            URL musicUrl = getClass().getResource("/game/resources/audio/monsters_inc_theme.mp3");
            if (musicUrl == null) {
                System.err.println("WARNING: Music file not found, skipping.");
                return;
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            Media media = new Media(musicUrl.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(savedMusicVolume);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();
            System.out.println("DEBUG: Music started");
        } catch (Exception e) {
            System.err.println("WARNING: Could not play music: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
    
    public void setMusicVolume(double v) {
        savedMusicVolume = Math.max(0, Math.min(1, v));
        if (mediaPlayer != null) mediaPlayer.setVolume(savedMusicVolume);
    }

    public double getMusicVolume() {
        return savedMusicVolume;
    }
    
    // ===== SCREENS =====

    public void switchToIntroScreen() {
        gameScreenActive = false;
        try {
            URL fxmlUrl = getClass().getResource("/game/gui/views/IntroScreen.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: IntroScreen.fxml not found — going straight to StartScreen");
                switchToStartScreen();
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            // Start transparent to prevent white flash on first render
            root.setOpacity(0);
            switchToContent(root, true);
            showStageIfNeeded();
            // Fade in after JavaFX has completed its first layout pass
            Platform.runLater(() -> Platform.runLater(() -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            }));
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load IntroScreen");
            e.printStackTrace();
            switchToStartScreen();
        }
    }

    public void switchToStartScreen() {
        if (!startScreenShownOnce) {
            // First time after intro: reveal loading under the still-visible intro
            // (no fade-to-black), hold it, then fade into the start screen.
            showLoadingScreen();
            showStageIfNeeded();
            PauseTransition loadingDelay = new PauseTransition(Duration.seconds(3));
            loadingDelay.setOnFinished(ev -> loadStartScreenContent());
            loadingDelay.play();
        } else {
            loadStartScreenContent();
        }
    }

    private void loadStartScreenContent() {
        gameScreenActive = false;
        try {
            URL fxmlUrl = getClass().getResource("/game/gui/views/StartScreen.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: StartScreen.fxml not found in classpath!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            addStylesheetOnce("/game/gui/resources/css/styles.css");
            addStylesheetOnce("/game/gui/resources/css/start-screen.css");

            // Fade the start screen in ON TOP of whatever is showing (loading /
            // previous screen) so we never clear to an empty black/white frame.
            // Do not release bindings on the underlying node yet — it must keep
            // filling the stage until the fade finishes and we remove it.
            root.setOpacity(0);
            prepareRootForFill(root);
            sceneHolder.getChildren().add(root);

            boolean firstReveal = !startScreenShownOnce;
            if (firstReveal) {
                startScreenShownOnce = true;
            }
            double fadeMs = firstReveal ? 700 : 400;

            Platform.runLater(() -> Platform.runLater(() -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(fadeMs), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.setOnFinished(ev -> {
                    java.util.List<javafx.scene.Node> toRemove = new java.util.ArrayList<>();
                    for (javafx.scene.Node n : sceneHolder.getChildren()) {
                        if (n != root) toRemove.add(n);
                    }
                    for (javafx.scene.Node n : toRemove) {
                        if (n instanceof Region) {
                            Region region = (Region) n;
                            try { region.prefWidthProperty().unbind(); }  catch (Exception ignored) {}
                            try { region.prefHeightProperty().unbind(); } catch (Exception ignored) {}
                        }
                    }
                    sceneHolder.getChildren().removeAll(toRemove);
                });
                fadeIn.play();
            }));

            if (!fullScreenPromptShown) {
                Platform.runLater(this::showFullScreenPrompt);
            }

            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load StartScreen");
            e.printStackTrace();
        }
    }
    /**
     * Puts the Loading_Screen image up as the current scene content.
     * Installs a black-backed loading root UNDER the intro, forces a layout
     * pass so it is paint-ready, then removes the intro — never an empty frame.
     */
    private void showLoadingScreen() {
        try {
            javafx.scene.image.Image loadingImage = ImageLoader.getInstance().loadImage("Loading_Screen.png");
            if (loadingImage == null) {
                System.err.println("WARNING: Loading_Screen.png not found, skipping loading screen.");
                return;
            }

            javafx.scene.image.ImageView loadingView = new javafx.scene.image.ImageView(loadingImage);
            loadingView.setPreserveRatio(false);
            loadingView.setSmooth(true);

            // Font is otherwise only loaded in GameController (after this screen)
            try {
                javafx.scene.text.Font.loadFont(
                    getClass().getResourceAsStream(GameUIConstants.FONT_PATH), 14);
            } catch (Exception ignored) {
                // Fall back to system fonts via CSS family list below
            }

            // Simple arcade-style ring spinner (no stock ProgressIndicator chrome).
            // Invisible circle keeps layout/rotation bounds centered on the open Arc.
            Arc spinnerArc = new Arc(0, 0, 18, 18, 0, 270);
            spinnerArc.setType(ArcType.OPEN);
            spinnerArc.setStroke(Color.web("#f0f0f0"));
            spinnerArc.setStrokeWidth(4);
            spinnerArc.setFill(null);
            spinnerArc.setStrokeLineCap(StrokeLineCap.ROUND);
            Circle spinnerBounds = new Circle(0, 0, 22);
            spinnerBounds.setOpacity(0);
            Group spinner = new Group(spinnerBounds, spinnerArc);

            RotateTransition spin = new RotateTransition(Duration.millis(900), spinner);
            spin.setByAngle(360);
            spin.setCycleCount(RotateTransition.INDEFINITE);
            spin.setInterpolator(Interpolator.LINEAR);
            spin.play();

            Label loadingLabel = new Label("Loading...");
            loadingLabel.setStyle(
                "-fx-font-family: '" + GameUIConstants.FONT + "', 'Courier New', monospace;" +
                "-fx-font-size: 36px;" +
                "-fx-text-fill: #f0f0f0;"
            );

            VBox loadingStack = new VBox(14, spinner, loadingLabel);
            loadingStack.setAlignment(Pos.CENTER);
            loadingStack.setMouseTransparent(true);
            StackPane.setAlignment(loadingStack, Pos.BOTTOM_CENTER);
            StackPane.setMargin(loadingStack, new Insets(0, 0, 56, 0));

            // Subtle pulse so the label reads as "in progress"
            FadeTransition pulse = new FadeTransition(Duration.millis(900), loadingLabel);
            pulse.setFromValue(0.45);
            pulse.setToValue(1.0);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(FadeTransition.INDEFINITE);
            pulse.play();

            // Black-backed Region so any sub-pixel / layout gap stays black, not white
            StackPane loadingRoot = new StackPane(loadingView, loadingStack);
            loadingRoot.setStyle("-fx-background-color: black;");
            prepareRootForFill(loadingRoot);
            loadingView.fitWidthProperty().bind(loadingRoot.widthProperty());
            loadingView.fitHeightProperty().bind(loadingRoot.heightProperty());

            releaseRootBindings();
            java.util.List<javafx.scene.Node> outgoing = new java.util.ArrayList<>(sceneHolder.getChildren());
            // Underlay first while intro still covers the stage
            sceneHolder.getChildren().add(0, loadingRoot);
            sceneHolder.applyCss();
            sceneHolder.layout();
            // Now safe to drop the intro — loading is already sized and laid out
            sceneHolder.getChildren().removeAll(outgoing);
        } catch (Exception e) {
            System.err.println("WARNING: Could not show loading screen: " + e.getMessage());
        }
    }

    public void switchToInstructionsScreen() {
        gameScreenActive = false;
        loadCachedScreen("InstructionsScreen", "/game/gui/views/InstructionsScreen.fxml");
    }

    public void startGameScreen(game.engine.Role playerRole) {
        gameScreenActive = true;
        System.out.println("DEBUG: startGameScreen() called with role = " + playerRole);
        try {
            URL fxmlUrl = getClass().getResource("/game/gui/views/GameScreen.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: GameScreen.fxml not found in classpath!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("DEBUG: GameScreen.fxml loaded OK");

            GameController controller = loader.getController();
            if (controller == null) {
                System.err.println("ERROR: GameController is null — check fx:controller in GameScreen.fxml");
                return;
            }
            controller.startGame(playerRole);
            System.out.println("DEBUG: controller.startGame() called OK");

            addStylesheetOnce("/game/gui/resources/css/styles.css");
            switchToContent(root, false);
            showStageIfNeeded();
            System.out.println("DEBUG: Switched to GameScreen successfully");

        } catch (IOException e) {
            System.err.println("ERROR: IOException loading GameScreen.fxml");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception in startGameScreen()");
            e.printStackTrace();
        }
    }

    public void switchToGameOverScreen(
            String winnerName,
            String winnerRole,
            game.engine.Role playerRole,
            String playerName,
            String playerRoleStr,
            int playerEnergy,
            String opponentName,
            String opponentRole,
            int opponentEnergy) {
        gameScreenActive = false;
        try {
            URL fxmlUrl = getClass().getResource("/game/gui/views/GameOverScreen.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: GameOverScreen.fxml not found!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            GameOverController controller = loader.getController();
            controller.setWinner(
                winnerName, winnerRole, playerRole,
                playerName, playerRoleStr, playerEnergy,
                opponentName, opponentRole, opponentEnergy
            );

            addStylesheetOnce("/game/gui/resources/css/styles.css");
            switchToContent(root, false);
            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load GameOverScreen");
            e.printStackTrace();
        }
    }

    private void loadCachedScreen(String name, String fxmlPath) {
        try {
            if (!cachedRoots.containsKey(name)) {
                URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    System.err.println("ERROR: FXML not found: " + fxmlPath);
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                addStylesheetOnce("/game/gui/resources/css/styles.css");
                cachedRoots.put(name, root);
            }
            switchToContent(cachedRoots.get(name), false);
            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Swaps the visible screen inside one persistent {@link Scene} so the stage
     * never calls {@code setScene} again (that resets size and exits fullscreen).
     */
    private void switchToContent(Parent content, boolean showPrompt) {
        if (content == null || primaryStage == null) return;

        releaseRootBindings();
        prepareRootForFill(content);
        sceneHolder.getChildren().setAll(content);

        if (showPrompt) {
            Platform.runLater(this::showFullScreenPrompt);
        }
    }

    /** Unbind size properties from the outgoing screen so the next screen can bind cleanly. */
    private void releaseRootBindings() {
        if (sceneHolder.getChildren().isEmpty()) return;
        javafx.scene.Node old = sceneHolder.getChildren().get(0);
        if (old instanceof Region) {
            Region region = (Region) old;
            try { region.prefWidthProperty().unbind(); }  catch (Exception ignored) {}
            try { region.prefHeightProperty().unbind(); } catch (Exception ignored) {}
        }
    }

    /** Fit the screen root to the stage without forcing a larger minimum size. */
    private void prepareRootForFill(Parent root) {
        if (!(root instanceof Region)) return;
        Region region = (Region) root;
        region.setMinWidth(0);
        region.setMinHeight(0);
        region.setMaxWidth(Double.MAX_VALUE);
        region.setMaxHeight(Double.MAX_VALUE);
        region.prefWidthProperty().bind(sceneHolder.widthProperty());
        region.prefHeightProperty().bind(sceneHolder.heightProperty());
    }

    private void showStageIfNeeded() {
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    /** In-scene toast that slides in from the top, holds, then fades out. No separate window. */
    private void showFullScreenPrompt() {
        if (fullScreenPromptShown || primaryStage == null) return;
        fullScreenPromptShown = true;

        Label toast = new Label("Press  ESC  to toggle fullscreen / windowed");
        toast.setStyle(
            "-fx-background-color: rgba(15,15,25,0.88);" +
            "-fx-background-radius: 30;" +
            "-fx-border-color: rgba(100,200,255,0.55);" +
            "-fx-border-radius: 30;" +
            "-fx-border-width: 1.5;" +
            "-fx-text-fill: #e0f0ff;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-padding: 10 28 10 28;"
        );
        toast.setMouseTransparent(true);

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(18, 0, 0, 0));
        toast.setOpacity(0);
        toast.setTranslateY(-20);

        sceneHolder.getChildren().add(toast);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), toast);
        slideIn.setFromY(-20);
        slideIn.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        ParallelTransition enterAnim = new ParallelTransition(slideIn, fadeIn);

        PauseTransition hold = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> sceneHolder.getChildren().remove(toast));

        new SequentialTransition(enterAnim, hold, fadeOut).play();
    }

    private void addStylesheetOnce(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("WARNING: Stylesheet not found, skipping: " + path);
            return;
        }
        String external = url.toExternalForm();
        if (!persistentScene.getStylesheets().contains(external)) {
            persistentScene.getStylesheets().add(external);
        }
    }
}