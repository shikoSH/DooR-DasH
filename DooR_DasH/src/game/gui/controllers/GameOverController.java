package game.gui.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class GameOverController {

    @FXML private Label      winsLabel;
    @FXML private StackPane  rootPane;
    @FXML private ImageView  backgroundImage;
    @FXML private ImageView  retryButton;
    @FXML private ImageView  mainMenuButton;
    @FXML private AnchorPane overlayPane;

    private Label     playerCardTitle, playerCardName, playerCardRole, playerCardEnergy;
    private ImageView playerMonsterImg;
    private Label     opponentCardTitle, opponentCardName, opponentCardRole, opponentCardEnergy;
    private ImageView opponentMonsterImg;

    private game.engine.Role lastPlayerRole;

    private HBox actionBtnRow;
    private HBox statCardsRow;
    private VBox playerCard;
    private VBox opponentCard;
    private Button playAgainBtn;
    private Button mainMenuBtn;
    private static final double REF_H = 720; // design reference height, used elsewhere in the game
    private static final double REF_W = 1280;

    private static final String IMG = "/game/gui/resources/images/";

    // Cached background — loaded once for the entire session
    private static Image   BG                = null;
    private static boolean BG_LOAD_ATTEMPTED = false;
    private AudioClip winClip;
    private AudioClip loseClip;
    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {
        // ── Background ────────────────────────────────────────
        if (!BG_LOAD_ATTEMPTED) {
            BG_LOAD_ATTEMPTED = true;
            BG = tryLoadSmall(IMG + "FinalGameOverScreen.png", 1280, 720);
        }
        if (backgroundImage != null && BG != null) {
            backgroundImage.setImage(BG);
            // Background fills the ACTUAL window exactly — no scale
            // transform, same treatment as the other screens' backdrop.
            backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        }

        // overlayPane is built at a FIXED 1280x720 reference size —
        // literally the same numbers regardless of window size — and
        // then scaled as one rigid unit via a real Scale transform
        // (applyResponsivePositions()). This guarantees zero relative
        // drift between elements: it's JavaFX's own transform math on a
        // single subtree, not manual per-element arithmetic. Enlarging
        // really is now "just making the picture bigger."
        if (overlayPane != null) {
            overlayPane.setMinWidth(REF_W);  overlayPane.setPrefWidth(REF_W);  overlayPane.setMaxWidth(REF_W);
            overlayPane.setMinHeight(REF_H); overlayPane.setPrefHeight(REF_H); overlayPane.setMaxHeight(REF_H);
        }

        // Keep winner text centered under baked-in "GAME OVER" — fixed
        // reference position/size now, scaled along with everything else.
        if (winsLabel != null) {
            AnchorPane.setTopAnchor(winsLabel, REF_H * 0.1);
            AnchorPane.setLeftAnchor(winsLabel, 0.0);
            AnchorPane.setRightAnchor(winsLabel, 0.0);
            // FXML set this to a fixed 48px that never rescaled before —
            // now it's a fixed reference value like everything else,
            // and the scale transform handles the actual on-screen size.
            //
            // Font-FAMILY is now set explicitly here too — the FXML used
            // to hardcode <font name="Book Antiqua Bold" .../> on this
            // label, which is a real JavaFX gotcha: once a Label's font
            // is set directly (FXML's <font> tag counts as this, same as
            // calling setFont() in code), a later -fx-font-size-only
            // style like the old one here doesn't touch font-family, so
            // this label kept quietly rendering in Book Antiqua forever
            // regardless of anything else in the app trying to apply the
            // arcade font. Removed the FXML override and set both here.
            winsLabel.setStyle(
                "-fx-font-family: '" + GameUIConstants.FONT + "';" +
                "-fx-font-size: " + (REF_H * 0.067) + "px;");
        }

        // ── Glow image buttons ────────────────────────────────
        setupActionButtons();

        // Load a short win sound if present at /game/resources/audio/win.wav
        try {
            java.net.URL url = getClass().getResource("/game/resources/audio/win.mp3");
            if (url != null) winClip = new AudioClip(url.toString());
        } catch (Exception ex) {
            System.err.println("Could not load win sound: " + ex.getMessage());
        }
        try {
            java.net.URL loseUrl = getClass().getResource("/game/resources/audio/lose.wav");
            if (loseUrl != null) loseClip = new AudioClip(loseUrl.toString());
        } catch (Exception ex) {
            System.err.println("Could not load lose sound: " + ex.getMessage());
        }

        buildStatCards();

        // Apply the scale transform now, and keep it in sync on resize.
        if (rootPane != null) {
            rootPane.widthProperty().addListener((obs, o, n) -> applyResponsivePositions());
            rootPane.heightProperty().addListener((obs, o, n) -> applyResponsivePositions());
            applyResponsivePositions();
        }
    }

    // =========================================================
    //  ACTION BUTTONS  (real JavaFX Buttons — no image dependency)
    // =========================================================
    private void setupActionButtons() {
        // The FXML still declares retryButton and mainMenuButton as ImageView fields,
        // but we hide them and inject proper Buttons into the overlayPane instead,
        // so we never depend on play_again_glow.png / main_menu_glow_button.png.
        if (retryButton    != null) retryButton.setVisible(false);
        if (mainMenuButton != null) mainMenuButton.setVisible(false);

        playAgainBtn = buildGameOverButton("▶  PLAY AGAIN",
            "#00ff88", "#001a0d", "#00ff88");
        mainMenuBtn  = buildGameOverButton("⌂  MAIN MENU",
            "#ff6666", "#1a0000", "#ff6666");

        playAgainBtn.setOnAction(e -> {
            if (lastPlayerRole != null)
                SceneManager.getInstance().startGameScreen(lastPlayerRole);
        });
        mainMenuBtn.setOnAction(e -> SceneManager.getInstance().switchToStartScreen());

        HBox btnRow = new HBox(28, playAgainBtn, mainMenuBtn);
        btnRow.setAlignment(Pos.CENTER);
        actionBtnRow = btnRow;

        overlayPane.getChildren().add(btnRow);
        AnchorPane.setLeftAnchor(btnRow,   0.0);
        AnchorPane.setRightAnchor(btnRow,  0.0);
        AnchorPane.setBottomAnchor(btnRow, REF_BTN_ROW_BOTTOM);
    }

    // Reference offsets tuned at the 1280x720 reference size — same
    // reference used throughout the rest of the game's UI.
    private static final double REF_BTN_ROW_BOTTOM   = 60;
    private static final double REF_STATS_ROW_BOTTOM = 220;
    private static final double REF_PORTRAIT_W       = 120;
    private static final double REF_PORTRAIT_H       = 140;
    private static final double REF_CARD_W           = 200;

    /**
     * Applies the single scale transform to overlayPane, keeping it in
     * sync with the actual window size. All internal positions/sizes
     * are fixed reference values (set once, at build time) — this is
     * the only place that touches the window's real dimensions.
     */
    private void applyResponsivePositions() {
        double w = rootPane.getWidth();
        double h = rootPane.getHeight();
        if (w == 0 || h == 0 || overlayPane == null) return;

        double scale = Math.min(w / REF_W, h / REF_H);
        overlayPane.setScaleX(scale);
        overlayPane.setScaleY(scale);
    }

    /**
     * Creates a polished arcade-style button for the game-over screen.
     * Uses inline CSS only — no image files required.
     */
    private Button buildGameOverButton(String text,
                                        String borderColor,
                                        String bgColor,
                                        String glowColor) {
        Button btn = new Button(text);
        String baseStyle =
            "-fx-font-family: 'ARCADECLASSIC', 'Courier New', monospace;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + borderColor + ";" +
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 2.5;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 14 38;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(three-pass-box, " + borderColor + ", 10, 0.3, 0, 0);";
        btn.setStyle(baseStyle);
        btn.setPrefWidth(220);
        btn.setPrefHeight(56);

        String hoverStyle =
            "-fx-font-family: 'ARCADECLASSIC', 'Courier New', monospace;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + bgColor + ";" +
            "-fx-background-color: " + borderColor + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 2.5;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 14 38;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(three-pass-box, " + borderColor + ", 22, 0.6, 0, 0);";

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(120), btn);
        scaleUp.setToX(1.07); scaleUp.setToY(1.07);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(120), btn);
        scaleDown.setToX(1.0); scaleDown.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            scaleDown.stop(); scaleUp.playFromStart();
            btn.setStyle(hoverStyle);
        });
        btn.setOnMouseExited(e -> {
            scaleUp.stop(); scaleDown.playFromStart();
            btn.setStyle(baseStyle);
        });
        btn.setOnMousePressed(e  -> btn.setOpacity(0.80));
        btn.setOnMouseReleased(e -> btn.setOpacity(1.00));

        return btn;
    }

    // =========================================================
    //  STAT CARDS
    // =========================================================
    private void buildStatCards() {
        playerMonsterImg   = makePortrait();
        playerCardTitle    = makeLabel("YOUR MONSTER", "#ffcc00", 13, true);
        playerCardName     = makeLabel("-", "white", 16, true);
        playerCardRole     = makeLabel("Role: -", "#00ffff", 12, false);
        playerCardEnergy   = makeLabel("Final Energy: -", "#00ff88", 12, true);
        playerCard         = styledCard("#ffcc00",
            playerMonsterImg, playerCardTitle, playerCardName, playerCardRole, playerCardEnergy);

        opponentMonsterImg = makePortrait();
        opponentCardTitle  = makeLabel("OPPONENT", "#ff6666", 13, true);
        opponentCardName   = makeLabel("-", "white", 16, true);
        opponentCardRole   = makeLabel("Role: -", "#00ffff", 12, false);
        opponentCardEnergy = makeLabel("Final Energy: -", "#ff6666", 12, true);
        opponentCard       = styledCard("#ff6666",
            opponentMonsterImg, opponentCardTitle, opponentCardName, opponentCardRole, opponentCardEnergy);

        HBox row = new HBox(50, playerCard, opponentCard);
        row.setAlignment(Pos.CENTER);
        statCardsRow = row;

        overlayPane.getChildren().add(row);

        AnchorPane.setLeftAnchor(row,   0.0);
        AnchorPane.setRightAnchor(row,  0.0);
        AnchorPane.setBottomAnchor(row, REF_STATS_ROW_BOTTOM);

        // Re-apply the scale transform now that overlayPane has its
        // full set of children — harmless if called again on resize.
        applyResponsivePositions();
    }

    private ImageView makePortrait() {
        ImageView iv = new ImageView();
        iv.setFitWidth(REF_PORTRAIT_W); iv.setFitHeight(REF_PORTRAIT_H); iv.setPreserveRatio(true);
        return iv;
    }

    private VBox styledCard(String color, javafx.scene.Node... nodes) {
        VBox card = new VBox(8, nodes);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(REF_CARD_W);
        card.setStyle(
            "-fx-background-color: rgba(0,0,0,0.75);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2.5;" +
            "-fx-padding: 18;");
        card.getStyleClass().add("stat-card");
        return card;
    }

    // =========================================================
    //  SET WINNER — full 9-arg version
    // =========================================================
    public void setWinner(String winnerName, String winnerRole,
                          game.engine.Role playerRole,
                          String playerName,   String playerRoleStr,   int playerEnergy,
                          String opponentName, String opponentRoleStr, int opponentEnergy) {

        this.lastPlayerRole = playerRole;

        winsLabel.setText(winnerName + " WINS!");
        animateWinsBanner();

        boolean playerWon = playerName.equals(winnerName);

        Image pImg = loadMonsterPortrait(playerName);
        if (pImg != null) playerMonsterImg.setImage(pImg);
        playerCardTitle.setText(playerWon ? "★ YOUR MONSTER ★" : "YOUR MONSTER");
        playerCardTitle.getStyleClass().remove("winner");
        if (playerWon) playerCardTitle.getStyleClass().add("winner");
        playerCardName.setText(playerName);
        playerCardRole.setText("Role: " + playerRoleStr);
        playerCardEnergy.setText("Final Energy: " + playerEnergy);

        Image oImg = loadMonsterPortrait(opponentName);
        if (oImg != null) opponentMonsterImg.setImage(oImg);
        boolean opponentWon = opponentName.equals(winnerName);
        opponentCardTitle.setText(opponentWon ? "★ OPPONENT ★" : "OPPONENT");
        opponentCardTitle.getStyleClass().remove("winner");
        if (opponentWon) opponentCardTitle.getStyleClass().add("winner");
        opponentCardName.setText(opponentName);
        opponentCardRole.setText("Role: " + opponentRoleStr);
        opponentCardEnergy.setText("Final Energy: " + opponentEnergy);

        animateCardsIn();
        playResultSound(playerWon);      // NEW
        if (playerWon) playConfetti();   // NEW — confetti only for an actual win
    }

    // Fallback 3-arg for any other call path
    public void setWinner(String winnerName, String winnerRole, game.engine.Role playerRole) {
        this.lastPlayerRole = playerRole;
        winsLabel.setText(winnerName + " WINS!");
        animateWinsBanner();
    }

    // =========================================================
    //  ANIMATIONS
    // =========================================================
    private void animateWinsBanner() {
        if (winsLabel == null) return;
        winsLabel.setOpacity(0); winsLabel.setScaleX(0.5); winsLabel.setScaleY(0.5);
        ScaleTransition s = new ScaleTransition(Duration.millis(500), winsLabel);
        s.setFromX(0.5); s.setToX(1.0); s.setFromY(0.5); s.setToY(1.0);
        FadeTransition f = new FadeTransition(Duration.millis(400), winsLabel);
        f.setFromValue(0); f.setToValue(1);
        s.play(); f.play();
    }

    private void animateCardsIn() {
        if (overlayPane.getChildren().isEmpty()) return;
        javafx.scene.Node row = overlayPane.getChildren().get(overlayPane.getChildren().size() - 1);
        row.setOpacity(0); row.setTranslateY(60);
        TranslateTransition t = new TranslateTransition(Duration.millis(500), row);
        t.setDelay(Duration.millis(400)); t.setFromY(60); t.setToY(0);
        FadeTransition f = new FadeTransition(Duration.millis(450), row);
        f.setDelay(Duration.millis(400)); f.setFromValue(0); f.setToValue(1);
        t.play(); f.play();
    }

    // =========================================================
    //  BUTTON HANDLERS
    // =========================================================
    @FXML private void handleReplay(MouseEvent e) {
        if (lastPlayerRole != null) {
            SceneManager.getInstance().startGameScreen(lastPlayerRole);
        }
    }

    @FXML private void handleMainMenu(MouseEvent e) {
        SceneManager.getInstance().switchToStartScreen();
    }

    // =========================================================
    //  IMAGE LOADING
    // =========================================================
    private Image tryLoadSmall(String fullPath, int w, int h) {
        try {
            java.io.InputStream s = getClass().getResourceAsStream(fullPath);
            if (s == null) { System.err.println("BG not found: " + fullPath); return null; }
            return new Image(s, w, h, false, true);
        } catch (OutOfMemoryError oom) {
            System.err.println("OOM loading background — add -Xmx512m to VM args");
            return null;
        } catch (Exception e) {
            System.err.println("Failed to load BG: " + e.getMessage());
            return null;
        }
    }

    private Image loadMonsterPortrait(String name) {
        if (name == null) return null;
        String file = getPortraitFile(name.trim().toLowerCase());
        if (file == null) file = getSpriteFile(name.trim().toLowerCase());
        if (file == null) return null;
        try {
            java.io.InputStream s = getClass().getResourceAsStream(IMG + file);
            if (s == null) return null;
            return new Image(s);
        } catch (Exception e) { return null; }
    }

    private String getPortraitFile(String key) {
        switch (key) {
            case "celia mae":               return "Celia_Mae_Screen.png";
            case "fungus":                  return "FungusScreen.png";
            case "henry j. waternoose":
            case "henry j. waternoose iii": return "Henry_Screen.png";
            case "james p. sullivan":
            case "james sullivan":          return "James_Screen.png";
            case "mike wazowski":           return "Mike_Screen.png";
            case "randall boggs":
            case "randall":                 return "Randal_Screen.png";
            case "roz":                     return "Rose_Screen.png";
            case "yeti":                    return "Yeti_Screen.png";
            default:                        return null;
        }
    }

    private String getSpriteFile(String key) {
        switch (key) {
            case "celia mae":               return "celia mae.png";
            case "fungus":                  return "Fungus.png";
            case "henry j. waternoose":
            case "henry j. waternoose iii": return "Henry_J._Waternoose_III.png";
            case "james p. sullivan":
            case "james sullivan":          return "James sullivan.png";
            case "mike wazowski":           return "Mike_Wazowski.png";
            case "randall boggs":
            case "randall":                 return "Randall.png";
            case "roz":                     return "Roz.png";
            case "yeti":                    return "Yeti.png";
            default:                        return null;
        }
    }

    private Label makeLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + color + ";-fx-font-size:" + size + "px;" +
            (bold ? "-fx-font-weight:bold;" : ""));
        l.setWrapText(true); l.setMaxWidth(180); l.setAlignment(Pos.CENTER);
        return l;
    }

    // =========================================================
    //  CELEBRATION / CONFETTI
    // =========================================================
    private void playConfetti() {
        if (overlayPane == null || rootPane == null) return;
        Group confetti = new Group();
        Random rand = new Random();
        double centerX = REF_W / 2.0;
        for (int i = 0; i < 28; i++) {
            Circle c = new Circle(6 + rand.nextInt(6));
            c.setFill(Color.hsb(rand.nextDouble() * 360.0, 0.85, 0.95));
            double startX = centerX + (rand.nextDouble() * 480 - 240);
            double startY = 120 + rand.nextDouble() * 40;
            c.setTranslateX(startX);
            c.setTranslateY(startY);
            confetti.getChildren().add(c);

            double dur = 1.25 + rand.nextDouble() * 0.9;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(dur), c);
            tt.setByY(360 + rand.nextDouble() * 220);
            tt.setByX((rand.nextDouble() * 600 - 300));
            tt.setInterpolator(Interpolator.EASE_OUT);

            RotateTransition rt = new RotateTransition(Duration.seconds(dur), c);
            rt.setByAngle(rand.nextDouble() * 720 - 360);

            FadeTransition ft = new FadeTransition(Duration.seconds(dur), c);
            ft.setFromValue(1.0); ft.setToValue(0.0);

            ParallelTransition pt = new ParallelTransition(tt, rt, ft);
            pt.setDelay(Duration.millis(i * 18));
            pt.setOnFinished(ev -> confetti.getChildren().remove(c));
            pt.play();
        }
        overlayPane.getChildren().add(confetti);
        PauseTransition remove = new PauseTransition(Duration.seconds(3.5));
        remove.setOnFinished(e -> overlayPane.getChildren().remove(confetti));
        remove.play();
    }

    private void playResultSound(boolean playerWon) {
        try {
            if (playerWon) {
                if (winClip != null) winClip.play();
                else playSynthWinTone();
            } else {
                if (loseClip != null) loseClip.play();
                else playSynthWinTone();
            }
        } catch (Exception e) {
            System.err.println("Failed to play result sound: " + e.getMessage());
        }
    }

    private void playSynthWinTone() {
        new Thread(() -> {
            final float  sampleRate = 44100f;
            final int    durationMs = 450;
            final double freq       = 880.0;
            final int    samples    = (int) (durationMs * sampleRate / 1000);
            final byte[] buffer     = new byte[samples * 2];

            for (int i = 0; i < samples; i++) {
                double t   = i / sampleRate;
                double env = Math.min(1.0, t * 12.0) * Math.exp(-3.0 * t);
                short  val = (short) (Math.sin(2.0 * Math.PI * freq * t) * 32767.0 * env);
                buffer[2 * i]     = (byte) (val & 0xff);
                buffer[2 * i + 1] = (byte) ((val >> 8) & 0xff);
            }

            AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, false);
            try (SourceDataLine line = (SourceDataLine)
                    javax.sound.sampled.AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, af))) {
                line.open(af);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.stop();
            } catch (Exception ex) {
                // silently ignore audio failures
            }
        }, "win-tone-player").start();
    }
}