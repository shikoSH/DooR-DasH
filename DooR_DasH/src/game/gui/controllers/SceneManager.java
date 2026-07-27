package game.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

    // How long the fade-to-black / fade-from-black halves of every scene
    // transition take. Same value both ways so a transition always feels
    // symmetric; bump this up/down to make every scene change slower/faster.
    private static final double SCENE_FADE_MS = 350;

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
        fadeToBlackThenShow(() -> {
            try {
                URL fxmlUrl = getClass().getResource("/game/gui/views/IntroScreen.fxml");
                if (fxmlUrl == null) {
                    System.err.println("ERROR: IntroScreen.fxml not found — going straight to StartScreen");
                    loadStartScreenContent();
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                switchToContent(root, true);
                showStageIfNeeded();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to load IntroScreen");
                e.printStackTrace();
                loadStartScreenContent();
            }
        });
    }

    public void switchToStartScreen() {
        if (!startScreenShownOnce) {
            // Font is otherwise only loaded in GameController (after this
            // screen) — preload it here so the start screen has the game
            // font ready immediately instead of a brief fallback-font flash.
            try {
                javafx.scene.text.Font.loadFont(
                    getClass().getResourceAsStream(GameUIConstants.FONT_PATH), 14);
            } catch (Exception ignored) {
                // Fall back to system fonts via CSS family list.
            }
        }
        fadeToBlackThenShow(this::loadStartScreenContent);
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

            switchToContent(root, false);
            startScreenShownOnce = true;

            if (!fullScreenPromptShown) {
                Platform.runLater(this::showFullScreenPrompt);
            }
            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load StartScreen");
            e.printStackTrace();
        }
    }

    public void switchToInstructionsScreen() {
        gameScreenActive = false;
        fadeToBlackThenShow(() ->
            loadCachedScreen("InstructionsScreen", "/game/gui/views/InstructionsScreen.fxml"));
    }

    public void startGameScreen(game.engine.Role playerRole) {
        gameScreenActive = true;
        System.out.println("DEBUG: startGameScreen() called with role = " + playerRole);
        fadeToBlackThenShow(() -> {
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
        });
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
        fadeToBlackThenShow(() -> {
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
        });
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
     * Generic fade-to-black-then-in transition used by EVERY scene switch
     * above. Fades a solid black rectangle IN over whatever's currently
     * showing, then — once fully black — runs {@code swapContent} to load
     * and attach the new screen, then fades the black cover back OUT to
     * reveal it. This is the same "fade to black, then black fades into
     * the next screen" effect the intro→start transition used to have on
     * its own; centralising it here means every screen change gets it for
     * free instead of each caller re-implementing its own crossfade.
     *
     * {@code swapContent} typically calls {@link #switchToContent} (or
     * {@link #loadCachedScreen}), which replaces sceneHolder's entire
     * children list with just the new screen — that would also wipe out
     * the black cover, so it's deliberately re-added immediately after
     * swapContent runs, still on the FX thread before the next render
     * pulse, so the new (possibly not-yet-laid-out) content is never
     * shown unprotected even for a single frame.
     */
    private void fadeToBlackThenShow(Runnable swapContent) {
        if (primaryStage == null) {
            swapContent.run();
            return;
        }

        Rectangle blackCover = new Rectangle();
        blackCover.setFill(Color.BLACK);
        blackCover.widthProperty().bind(sceneHolder.widthProperty());
        blackCover.heightProperty().bind(sceneHolder.heightProperty());
        blackCover.setMouseTransparent(true);

        Runnable swapThenRevealFromBlack = () -> {
            swapContent.run();
            sceneHolder.getChildren().add(blackCover);
            blackCover.toFront();

            // Wait a couple pulses so the new content has actually laid
            // out before we reveal it — avoids a flash of unstyled/unsized
            // content peeking through as the cover fades away.
            Platform.runLater(() -> Platform.runLater(() -> {
                FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(SCENE_FADE_MS), blackCover);
                fadeFromBlack.setFromValue(1);
                fadeFromBlack.setToValue(0);
                fadeFromBlack.setOnFinished(ev -> sceneHolder.getChildren().remove(blackCover));
                fadeFromBlack.play();
            }));
        };

        if (sceneHolder.getChildren().isEmpty()) {
            // Nothing on screen yet (very first launch) — nothing to fade
            // FROM, so skip straight to black and fade the new content in.
            blackCover.setOpacity(1);
            sceneHolder.getChildren().add(blackCover);
            swapThenRevealFromBlack.run();
            return;
        }

        blackCover.setOpacity(0);
        sceneHolder.getChildren().add(blackCover);
        blackCover.toFront();

        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(SCENE_FADE_MS), blackCover);
        fadeToBlack.setFromValue(0);
        fadeToBlack.setToValue(1);
        fadeToBlack.setOnFinished(e -> swapThenRevealFromBlack.run());
        fadeToBlack.play();
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