package game.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import game.gui.controllers.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            SceneManager.getInstance().initialize(primaryStage);
            SceneManager.getInstance().switchToIntroScreen(); // starts with intro now
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}