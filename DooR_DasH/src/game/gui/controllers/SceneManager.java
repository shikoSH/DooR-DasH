package game.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
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
    private boolean fullScreenPromptShown = false;
    private boolean startScreenShownOnce = false;

    private static final double DEFAULT_WIDTH  = 1280;
    private static final double DEFAULT_HEIGHT = 720;

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
        persistentScene = new Scene(sceneHolder, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        persistentScene.setFill(Color.BLACK);
        // ESC toggles: fullscreen <-> 1280x720 windowed — never exits the game
        persistentScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (primaryStage.isFullScreen()) {
                    primaryStage.setFullScreen(false);
                    primaryStage.setWidth(DEFAULT_WIDTH);
                    primaryStage.setHeight(DEFAULT_HEIGHT);
                    primaryStage.centerOnScreen();
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
            mediaPlayer.setVolume(0.7);
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

    // ===== SCREENS =====

    public void switchToIntroScreen() {
        try {
            URL fxmlUrl = getClass().getResource("/game/gui/views/IntroScreen.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: IntroScreen.fxml not found — going straight to StartScreen");
                switchToStartScreen();
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            switchToContent(root, true);
            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load IntroScreen");
            e.printStackTrace();
            switchToStartScreen();
        }
    }

    public void switchToStartScreen() {
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
            switchToContent(root, !fullScreenPromptShown);

            if (!startScreenShownOnce) {
                startScreenShownOnce = true;
                root.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            } else {
                root.setOpacity(1);
            }

            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load StartScreen");
            e.printStackTrace();
        }
    }

    public void switchToInstructionsScreen() {
        loadCachedScreen("InstructionsScreen", "/game/gui/views/InstructionsScreen.fxml");
    }

    public void startGameScreen(game.engine.Role playerRole) {
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
