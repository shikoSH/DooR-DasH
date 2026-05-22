package game.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
    private final HashMap<String, Scene> scenes = new HashMap<>();
    private MediaPlayer mediaPlayer;
    private boolean fullScreenPromptShown = false;

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
        this.primaryStage.setWidth(1280);
        this.primaryStage.setHeight(920);
        this.primaryStage.setResizable(true);
        this.primaryStage.centerOnScreen();
        this.primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        this.primaryStage.setFullScreenExitHint("Press ESC to exit fullscreen");
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
            Scene scene = new Scene(root);
            scene.setFill(Color.BLACK);
            registerScene(scene, true);
            scenes.put("IntroScreen", scene);
            primaryStage.setScene(scene);
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
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

            root.setOpacity(0);
            Scene scene = new Scene(root);
            scene.setFill(Color.BLACK);
            registerScene(scene, true);
            addStylesheet(scene, "/game/gui/resources/css/styles.css");
            addStylesheet(scene, "/game/gui/resources/css/start-screen.css");
            scenes.put("StartScreen", scene);
            primaryStage.setScene(scene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load StartScreen");
            e.printStackTrace();
        }
    }

    public void switchToInstructionsScreen() {
        loadScene("InstructionsScreen", "/game/gui/views/InstructionsScreen.fxml");
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

            Scene scene = new Scene(root);
            scene.setFill(Color.BLACK);
            registerScene(scene, false);
            addStylesheet(scene, "/game/gui/resources/css/styles.css");
            scenes.put("GameScreen", scene);
            primaryStage.setScene(scene);

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
            System.out.println("DEBUG: Switched to GameScreen successfully");

        } catch (IOException e) {
            System.err.println("ERROR: IOException loading GameScreen.fxml");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception in startGameScreen()");
            e.printStackTrace();
        }
    }

    // Updated signature to match the 9-parameter call in GameController.checkWinner()
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

            Scene scene = new Scene(root);
            scene.setFill(Color.BLACK);
            registerScene(scene, false);
            addStylesheet(scene, "/game/gui/resources/css/styles.css");
            scenes.put("GameOverScreen", scene);
            primaryStage.setScene(scene);

            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load GameOverScreen");
            e.printStackTrace();
        }
    }

    private void loadScene(String name, String fxmlPath) {
        try {
            if (!scenes.containsKey(name)) {
                URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    System.err.println("ERROR: FXML not found: " + fxmlPath);
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.setFill(Color.BLACK);
                registerScene(scene, false);
                addStylesheet(scene, "/game/gui/resources/css/styles.css");
                scenes.put(name, scene);
            }
            primaryStage.setScene(scenes.get(name));
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void registerScene(Scene scene, boolean showPrompt) {
        if (scene == null) return;
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                event.consume();
            }
        });
        if (showPrompt) {
            Platform.runLater(this::showFullScreenPrompt);
        }
    }

    // Replaced Alert (which fails to resolve in JavaFX 8 Eclipse projects)
    // with a plain Stage dialog — identical blocking behavior, no Alert import needed.
    private void showFullScreenPrompt() {
        if (fullScreenPromptShown || primaryStage == null) return;
        fullScreenPromptShown = true;

        Stage dialog = new Stage();
        dialog.setTitle("Full Screen");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);

        Label header = new Label("Make the game cover your entire screen");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label content = new Label("Press ESCAPE to toggle full-screen mode.");
        content.setWrapText(true);

        Button ok = new Button("OK");
        ok.setOnAction(e -> dialog.close());

        VBox layout = new VBox(12, header, content, ok);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        dialog.setScene(new Scene(layout, 340, 140));
        dialog.showAndWait();
    }

    private void addStylesheet(Scene scene, String path) {
        URL url = getClass().getResource(path);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        } else {
            System.err.println("WARNING: Stylesheet not found, skipping: " + path);
        }
    }
}
