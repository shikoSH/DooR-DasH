package game.gui.controllers;

import javafx.animation.FadeTransition;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;
    private final StackPane sceneHolder = new StackPane();
    private Scene persistentScene;
    private final HashMap<String, Parent> cachedRoots = new HashMap<>();
    private MediaPlayer mediaPlayer;
    private boolean fullScreenPromptShown = false;
    private boolean startScreenShownOnce = false;

    private static final double DEFAULT_WIDTH  = 1280;
    private static final double DEFAULT_HEIGHT = 720;
    private static final Duration PROMPT_FADE_MS = Duration.millis(350);
    private static final Duration PROMPT_VISIBLE = Duration.seconds(2.5);

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
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
        this.primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        this.primaryStage.setFullScreenExitHint("Press ESC to exit fullscreen");

        sceneHolder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        persistentScene = new Scene(sceneHolder, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        persistentScene.setFill(Color.BLACK);
        persistentScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                event.consume();
            }
        });
        primaryStage.setScene(persistentScene);
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

    /** Brief non-blocking tip: fades in, stays visible, then fades out and closes. */
    private void showFullScreenPrompt() {
        if (fullScreenPromptShown || primaryStage == null) return;
        fullScreenPromptShown = true;

        Stage dialog = new Stage();
        dialog.setTitle("Full Screen");
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.NONE);

        Label header = new Label("Make the game cover your entire screen");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

        Label content = new Label("Press ESCAPE to toggle full-screen mode.");
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        VBox layout = new VBox(10, header, content);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(
            "-fx-background-color: rgba(25,25,35,0.96);" +
            "-fx-background-radius: 12;");
        layout.setOpacity(0);

        Scene dialogScene = new Scene(layout, 360, 110);
        dialogScene.setFill(Color.TRANSPARENT);
        dialog.setScene(dialogScene);

        dialog.setOnShown(e -> {
            dialog.setX(primaryStage.getX() + (primaryStage.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(primaryStage.getY() + (primaryStage.getHeight() - dialog.getHeight()) / 2);

            FadeTransition fadeIn = new FadeTransition(PROMPT_FADE_MS, layout);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            PauseTransition hold = new PauseTransition(PROMPT_VISIBLE);

            FadeTransition fadeOut = new FadeTransition(PROMPT_FADE_MS, layout);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> dialog.close());

            new SequentialTransition(fadeIn, hold, fadeOut).play();
        });

        dialog.show();
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
