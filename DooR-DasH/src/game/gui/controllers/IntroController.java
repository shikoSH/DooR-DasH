package game.gui.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
        PauseTransition logoHold = new PauseTransition(Duration.millis(800));

        // === PHASE 3: Logo moves up while black overlay fades revealing start screen ===
        TranslateTransition logoMoveUp = new TranslateTransition(Duration.millis(1200), logoImage);
        logoMoveUp.setToY(-220);

        // Chain everything
        SequentialTransition fullSequence = new SequentialTransition(
            creditsFadeIn,
            teamHold,
            creditsFadeOut,
            new PauseTransition(Duration.millis(500)),
            logoIntro,
            logoHold
        );

        logoIntro.setOnFinished(e -> SceneManager.getInstance().startMusic());

        // When credits + logo intro done, load start screen behind black overlay
        fullSequence.setOnFinished(e -> revealStartScreen(logoMoveUp));

        fullSequence.play();
    }

    private void revealStartScreen(TranslateTransition logoMoveUp) {
        try {
            // 1. Load start screen content
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/game/gui/views/StartScreen.fxml")
            );
            Parent startRoot = loader.load();
            startRoot.setOpacity(1);

            // 2. Create black overlay rectangle that sits on top of start screen
            Rectangle blackOverlay = new Rectangle(1280, 720);
            blackOverlay.setFill(Color.BLACK);

            // 3. Insert start screen BELOW current content
            // Stack order bottom to top: startRoot → blackOverlay → logoImage → texts
            rootPane.getChildren().add(0, startRoot);        // bottom
            rootPane.getChildren().add(1, blackOverlay);     // above start screen

            // Make sure logo stays on top
            logoImage.toFront();

            // 4. Play logo move up AND black overlay fade simultaneously
            FadeTransition overlayFade = new FadeTransition(Duration.millis(1200), blackOverlay);
            overlayFade.setFromValue(1);
            overlayFade.setToValue(0);

            ParallelTransition reveal = new ParallelTransition(logoMoveUp, overlayFade);

            reveal.setOnFinished(ev -> {
                // Switch to actual start screen scene cleanly
                SceneManager.getInstance().switchToStartScreen();
            });

            reveal.play();

        } catch (Exception e) {
            System.err.println("ERROR: Could not load start screen for reveal");
            e.printStackTrace();
            SceneManager.getInstance().switchToStartScreen();
        }
    }
}