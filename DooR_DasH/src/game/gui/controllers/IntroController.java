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
        // Ensure loading art is cached before the intro finishes (needed immediately after)
        ImageLoader.getInstance().loadImage("Loading_Screen.png");
        // Pre-load remaining game images during intro to prevent OutOfMemoryError
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

        // === PHASE 2: Logo appears big and scales down ===
        // setVisible(false) in addition to opacity 0 — belt-and-suspenders
        // so the logo is guaranteed invisible during Phase 1 (the "A GAME
        // BY TEAM 85" credits), regardless of anything else touching its
        // opacity. It's flipped back to visible right before logoIntro
        // starts (see preLogoPause below), never sooner.
        logoImage.setVisible(false);
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

        PauseTransition preLogoPause = new PauseTransition(Duration.millis(500));
        preLogoPause.setOnFinished(e -> {
            logoImage.setVisible(true);
            // Starts exactly when the logo appears — it was previously
            // triggered earlier (when the credits started fading out),
            // which meant the music began noticeably before the logo
            // was actually visible.
            SceneManager.getInstance().startMusic();
        });

        // === CHAIN ===
        // No fade-out phase here anymore — SceneManager.switchToStartScreen()
        // now fades every scene transition to black generically (see
        // fadeToBlackThenShow()), so it fades in right over the logo at
        // full opacity/scale instead of needing this screen to fade itself
        // out first. That also removes ~700ms of dead time between the
        // logo settling and the start screen appearing.
        SequentialTransition fullSequence = new SequentialTransition(
            creditsFadeIn,
            teamHold,
            creditsFadeOut,
            preLogoPause,
            logoIntro,
            logoHold
        );

        fullSequence.setOnFinished(e ->
            SceneManager.getInstance().switchToStartScreen()
        );

        fullSequence.play();
    }
}