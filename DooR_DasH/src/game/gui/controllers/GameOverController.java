package game.gui.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GameOverController {

    @FXML private Label      winsLabel;
    @FXML private StackPane  rootPane;
    @FXML private ImageView  backgroundImage;
    @FXML private Button     retryButton;
    @FXML private Button     mainMenuButton;
    @FXML private AnchorPane overlayPane;

    private Label     playerCardTitle, playerCardName, playerCardRole, playerCardEnergy;
    private ImageView playerMonsterImg;
    private Label     opponentCardTitle, opponentCardName, opponentCardRole, opponentCardEnergy;
    private ImageView opponentMonsterImg;

    private game.engine.Role lastPlayerRole;

    private static final String IMG = "/game/gui/resources/images/";

    // Cached background — loaded once for the entire session
    private static Image   BG               = null;
    private static boolean BG_LOAD_ATTEMPTED = false;

    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    private void initialize() {
        // ── Background ────────────────────────────────────────
        if (!BG_LOAD_ATTEMPTED) {
            BG_LOAD_ATTEMPTED = true;
            BG = tryLoadSmall(IMG + "FinalGameOverScreen.png", 1280, 920);
        }
        if (backgroundImage != null && BG != null) {
            backgroundImage.setImage(BG);
            backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        }

        if (overlayPane != null && rootPane != null) {
            overlayPane.prefWidthProperty().bind(rootPane.widthProperty());
            overlayPane.prefHeightProperty().bind(rootPane.heightProperty());
        }

        // ── Style + glow on buttons ───────────────────────────
        styleButton(retryButton,    "#00ff88", "#003322");
        styleButton(mainMenuButton, "#ff6666", "#330011");

        addGlowHover(retryButton,    Color.web("#00ff88"), Color.web("#00ffaa"));
        addGlowHover(mainMenuButton, Color.web("#ff6666"), Color.web("#ff9999"));

        // ── Button position: anchor to bottom, not absolute ───
        // This keeps them at the same relative position regardless of window height.
        // Increase bottomAnchor to move them further from the bottom.
        AnchorPane.setBottomAnchor(retryButton,    220.0);
        AnchorPane.setLeftAnchor(retryButton,      null);
        AnchorPane.setRightAnchor(retryButton,     null);
        // Center horizontally by anchoring left to a fixed offset
        rootPane.widthProperty().addListener((obs, old, w) -> {
            double totalW = w.doubleValue();
            double gap    = 40;
            double btnW   = 240;
            double totalBtnsW = btnW * 2 + gap;
            double startX = (totalW - totalBtnsW) / 2.0;
            AnchorPane.setLeftAnchor(retryButton,    startX);
            AnchorPane.setLeftAnchor(mainMenuButton, startX + btnW + gap);
        });
        AnchorPane.setBottomAnchor(mainMenuButton, 220.0);

        // Wire handlers defensively
        if (retryButton    != null) retryButton.setOnAction(e -> handleReplay());
        if (mainMenuButton != null) mainMenuButton.setOnAction(e -> handleMainMenu());

        buildStatCards();
    }

    // =========================================================
    //  BUTTON STYLING
    // =========================================================
    private void styleButton(Button btn, String textColor, String bgColor) {
        btn.setStyle(
            "-fx-font-family: 'Book Antiqua Bold';" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-border-color: " + textColor + ";" +
            "-fx-border-width: 2.5;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 30;");
    }

    /**
     * Adds a pulsing glow + scale animation on hover.
     * On enter: button scales up slightly and glows with a breathing pulse.
     * On exit:  pulse stops, button returns to normal.
     */
    private void addGlowHover(Button btn, Color glowColor, Color brightColor) {
        // Base drop shadow (always present, subtle)
        DropShadow baseShadow = new DropShadow();
        baseShadow.setColor(glowColor);
        baseShadow.setRadius(8);
        baseShadow.setSpread(0.2);
        btn.setEffect(baseShadow);

        // Hover glow + glow effect
        DropShadow hoverShadow = new DropShadow();
        hoverShadow.setColor(brightColor);
        hoverShadow.setRadius(30);
        hoverShadow.setSpread(0.5);
        Glow glow = new Glow(0.8);
        hoverShadow.setInput(glow);

        // Pulse timeline — animates the glow radius breathing
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(hoverShadow.radiusProperty(), 20),
                new KeyValue(hoverShadow.spreadProperty(), 0.4)),
            new KeyFrame(Duration.millis(600),
                new KeyValue(hoverShadow.radiusProperty(), 40),
                new KeyValue(hoverShadow.spreadProperty(), 0.7)),
            new KeyFrame(Duration.millis(1200),
                new KeyValue(hoverShadow.radiusProperty(), 20),
                new KeyValue(hoverShadow.spreadProperty(), 0.4))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);

        // Scale up on enter
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), btn);
        scaleUp.setToX(1.08); scaleUp.setToY(1.08);

        // Scale back on exit
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), btn);
        scaleDown.setToX(1.0); scaleDown.setToY(1.0);

        btn.setOnMouseEntered(e -> {
            pulse.stop();
            btn.setEffect(hoverShadow);
            pulse.play();
            scaleUp.play();
        });
        btn.setOnMouseExited(e -> {
            pulse.stop();
            btn.setEffect(baseShadow);
            scaleDown.play();
        });
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
        VBox playerCard    = styledCard("#ffcc00",
            playerMonsterImg, playerCardTitle, playerCardName, playerCardRole, playerCardEnergy);

        opponentMonsterImg = makePortrait();
        opponentCardTitle  = makeLabel("OPPONENT", "#ff6666", 13, true);
        opponentCardName   = makeLabel("-", "white", 16, true);
        opponentCardRole   = makeLabel("Role: -", "#00ffff", 12, false);
        opponentCardEnergy = makeLabel("Final Energy: -", "#ff6666", 12, true);
        VBox opponentCard  = styledCard("#ff6666",
            opponentMonsterImg, opponentCardTitle, opponentCardName, opponentCardRole, opponentCardEnergy);

        HBox row = new HBox(50, playerCard, opponentCard);
        row.setAlignment(Pos.CENTER);

        overlayPane.getChildren().add(row);

        // Stat cards sit above the buttons — bottom anchor higher than the buttons
        AnchorPane.setBottomAnchor(row, 320.0);
        AnchorPane.setLeftAnchor(row,   0.0);
        AnchorPane.setRightAnchor(row,  0.0);
    }

    private ImageView makePortrait() {
        ImageView iv = new ImageView();
        iv.setFitWidth(120); iv.setFitHeight(140); iv.setPreserveRatio(true);
        return iv;
    }

    private VBox styledCard(String color, javafx.scene.Node... nodes) {
        VBox card = new VBox(8, nodes);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setStyle(
            "-fx-background-color: rgba(0,0,0,0.75);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2.5;" +
            "-fx-padding: 18;");
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
        playerCardTitle.setStyle("-fx-text-fill:" + (playerWon ? "#ffcc00" : "#888") +
            ";-fx-font-size:13px;-fx-font-weight:bold;");
        playerCardName.setText(playerName);
        playerCardRole.setText("Role: " + playerRoleStr);
        playerCardEnergy.setText("Final Energy: " + playerEnergy);

        Image oImg = loadMonsterPortrait(opponentName);
        if (oImg != null) opponentMonsterImg.setImage(oImg);
        boolean opponentWon = opponentName.equals(winnerName);
        opponentCardTitle.setText(opponentWon ? "★ OPPONENT ★" : "OPPONENT");
        opponentCardTitle.setStyle("-fx-text-fill:" + (opponentWon ? "#ffcc00" : "#888") +
            ";-fx-font-size:13px;-fx-font-weight:bold;");
        opponentCardName.setText(opponentName);
        opponentCardRole.setText("Role: " + opponentRoleStr);
        opponentCardEnergy.setText("Final Energy: " + opponentEnergy);

        animateCardsIn();
    }

    // Fallback 3-arg for cheat key path
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
    @FXML private void handleReplay()   { SceneManager.getInstance().startGameScreen(lastPlayerRole); }
    @FXML private void handleMainMenu() { SceneManager.getInstance().switchToStartScreen(); }

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
            case "james sullivan":          return "Mike_Screen.png";
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
}