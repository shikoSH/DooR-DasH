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
                java.net.URL fxmlUrl = getClass().getResource("/game/gui/views/StartScreen.fxml");
                if (fxmlUrl == null) {
                    System.err.println("ERROR: StartScreen.fxml not found in classpath!");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
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
            // 1. Check FXML exists
            java.net.URL fxmlUrl = getClass().getResource("/game/gui/views/GameScreen.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: GameScreen.fxml not found in classpath!");
                return;
            }

            // 2. Load FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            System.out.println("DEBUG: GameScreen.fxml loaded OK");

            // 3. Get controller and start game
            GameController controller = loader.getController();
            if (controller == null) {
                System.err.println("ERROR: GameController is null — check fx:controller in GameScreen.fxml");
                return;
            }
            controller.startGame(playerRole);
            System.out.println("DEBUG: controller.startGame() called OK");

            // 4. Build and switch scene
            Scene scene = new Scene(root, 1280, 720);
            addStylesheet(scene, "/game/gui/resources/css/styles.css");
            scenes.put("GameScreen", scene);
            primaryStage.setScene(scene);

            // 5. Ensure stage is visible
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

    public void switchToGameOverScreen() {
        loadScene("GameOverScreen", "/game/gui/views/GameOverScreen.fxml");
    }

    private void loadScene(String name, String fxmlPath) {
        try {
            if (!scenes.containsKey(name)) {
                java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    System.err.println("ERROR: FXML not found: " + fxmlPath);
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Scene scene = new Scene(root, 1280, 720);
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

    private void addStylesheet(Scene scene, String path) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        } else {
            System.err.println("WARNING: Stylesheet not found, skipping: " + path);
        }
    }
}