package game.gui.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;

public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;
    private HashMap<String, Scene> scenes = new HashMap<>();

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
    }

    public void switchToStartScreen() {
        try {
            if (!scenes.containsKey("StartScreen")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/gui/views/StartScreen.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1280, 720);
                addStylesheet(scene, "/game/gui/resources/css/styles.css");
                addStylesheet(scene, "/game/gui/resources/css/start-screen.css");
                scenes.put("StartScreen", scene);
            }
            primaryStage.setScene(scenes.get("StartScreen"));
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Failed to load scene: StartScreen");
            e.printStackTrace();
        }
    }

    public void switchToInstructionsScreen() {
        loadScene("InstructionsScreen", "/game/gui/views/InstructionsScreen.fxml");
    }

    public void startGameScreen(game.engine.Role playerRole) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/gui/views/GameScreen.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.startGame(playerRole);
            Scene scene = new Scene(root, 1280, 720);
            addStylesheet(scene, "/game/gui/resources/css/styles.css");
            scenes.put("GameScreen", scene);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToGameOverScreen() {
        loadScene("GameOverScreen", "/game/gui/views/GameOverScreen.fxml");
    }

    private void loadScene(String name, String fxmlPath) {
        try {
            if (!scenes.containsKey(name)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1280, 720);
                addStylesheet(scene, "/game/gui/resources/css/styles.css");
                scenes.put(name, scene);
            }
            primaryStage.setScene(scenes.get(name));
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Safely adds a stylesheet to a scene.
     * Prints a warning instead of crashing if the file is not found.
     */
    private void addStylesheet(Scene scene, String path) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        } else {
            System.err.println("WARNING: Stylesheet not found, skipping: " + path);
        }
    }
}