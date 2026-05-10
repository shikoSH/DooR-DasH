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
        this.primaryStage.setResizable(false);
    }

    public void switchToStartScreen() {
        loadScene("StartScreen", "/game/gui/views/StartScreen.fxml");
    }

    public void switchToInstructionsScreen() {
        loadScene("InstructionsScreen", "/game/gui/views/InstructionsScreen.fxml");
    }

    public void startGameScreen(game.engine.Role playerRole) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/gui/views/GameScreen.fxml"));
            Parent root = loader.load();
            
            game.gui.controllers.GameController controller = loader.getController();
            controller.startGame(playerRole);
            
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(getClass().getResource("/game/gui/resources/css/styles.css").toExternalForm());
            
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
                scene.getStylesheets().add(getClass().getResource("/game/gui/resources/css/styles.css").toExternalForm());
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
}
