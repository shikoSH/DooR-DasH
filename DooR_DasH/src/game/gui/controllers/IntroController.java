package game.gui.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class IntroController {

    @FXML private StackPane rootPane;
    @FXML private Text teamText;
    @FXML private Text disclaimerText;
    @FXML private ImageView logoImage;

    @FXML
    private void initialize() {
        rootPane.setStyle("-fx-background-color: black;");
        // Pre-load game images in background during intro to prevent OutOfMemoryError
        Platform.runLater(() -> ImageLoader.getInstance().preloadGameImages());
        playIntroSequence();
    }

    private void playIntroSequence() {

        // === PHASE 1: Team text + disclaimer ===
        teamText.setStyle(
            "-fx-fill: white;" +
            "-fx-font-size: 58px;" +
            "-fx-font-family: 'Georgia';" +
            "-fx-font-weight: bold;"
        );
        teamText.setOpacity(0);
        disclaimerText.setOpacity(0);

        FadeTransition teamFadeIn = new FadeTransition(Duration.millis(1500), teamText);
        teamFadeIn.setFromValue(0);
        teamFadeIn.setToValue(1);

        FadeTransition disclaimerFadeIn = new FadeTransition(Duration.millis(1500), disclaimerText);
        disclaimerFadeIn.setFromValue(0);
        disclaimerFadeIn.setToValue(1);
        disclaimerFadeIn.setDelay(Duration.millis(500));

        ParallelTransition creditsFadeIn = new ParallelTransition(teamFadeIn, disclaimerFadeIn);
        PauseTransition teamHold = new PauseTransition(Duration.millis(2000));

        FadeTransition teamFadeOut = new FadeTransition(Duration.millis(1000), teamText);
        teamFadeOut.setFromValue(1);
        teamFadeOut.setToValue(0);

        FadeTransition disclaimerFadeOut = new FadeTransition(Duration.millis(1000), disclaimerText);
        disclaimerFadeOut.setFromValue(1);
        disclaimerFadeOut.setToValue(0);

        ParallelTransition creditsFadeOut = new ParallelTransition(teamFadeOut, disclaimerFadeOut);

        // Music starts as credits fade out
        creditsFadeOut.setOnFinished(e -> SceneManager.getInstance().startMusic());

        // === PHASE 2: Logo appears big and scales down ===
        logoImage.setScaleX(1.8);
        logoImage.setScaleY(1.8);
        logoImage.setOpacity(0);

        FadeTransition logoFadeIn = new FadeTransition(Duration.millis(1500), logoImage);
        logoFadeIn.setFromValue(0);
        logoFadeIn.setToValue(1);

        ScaleTransition logoScale = new ScaleTransition(Duration.millis(1500), logoImage);
        logoScale.setFromX(1.8);
        logoScale.setFromY(1.8);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);

        ParallelTransition logoIntro = new ParallelTransition(logoFadeIn, logoScale);
        PauseTransition logoHold = new PauseTransition(Duration.millis(1200));

        // === PHASE 3: Everything fades to black ===
        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(1000), rootPane);
        fadeToBlack.setFromValue(1);
        fadeToBlack.setToValue(0);

        // === CHAIN ===
        SequentialTransition fullSequence = new SequentialTransition(
            creditsFadeIn,
            teamHold,
            creditsFadeOut,
            new PauseTransition(Duration.millis(500)),
            logoIntro,
            logoHold,
            fadeToBlack
        );

        fullSequence.setOnFinished(e ->
            SceneManager.getInstance().switchToStartScreen()
        );

        fullSequence.play();
    }
}