package game.gui.controllers;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.util.Duration;
import game.engine.cards.Card;

/**
 * Responsible for constructing and populating every UI panel on the game screen:
 *  - Player side panel
 *  - Opponent side panel
 *  - Action log
 *  - Dice label panel
 *  - Card-draw overlay
 *
 * It also owns the {@link #ledLabel} factory used uniformly across the UI.
 *
 * Worker contract: after construction, call each build* method once from
 * GameController.initialize(). The public label / ImageView fields are then
 * read back by GameController to wire them into UIUpdater.
 */
public class PanelBuilder {

    // ── Font / scale constants (mirrored from GameController) ─
    /** Change to "\"Arial\", sans-serif" for a modern look. */
    static final String LED        = "\"Courier New\", monospace";
    /** 1.0 = normal, 1.4 = 40% bigger. */
    static final double TEXT_SCALE = 1.4;

    // ── Shared image loader ───────────────────────────────────
    private final ImageLoader images;
    private double panelWidth = 120;

    
    // ── Player panel widgets (read by UIUpdater / GameController) ─
    public final ImageView playerPortrait    = new ImageView();
    public final ImageView playerEnergyBar   = new ImageView();
    public Label playerNameLabel, playerTypeLabel, playerOrigRoleLabel;
    public Label playerCurrRoleLabel, playerIndexLabel, playerEnergyLabel;
    public Label playerStatusLabel, playerTurnLabel;

    public HBox playerSignals;

    // ── Opponent panel widgets ───────────────────────────────
    public final ImageView opponentPortrait  = new ImageView();
    public final ImageView opponentEnergyBar = new ImageView();
    public Label opponentNameLabel, opponentTypeLabel, opponentOrigRoleLabel;
    public Label opponentCurrRoleLabel, opponentIndexLabel, opponentEnergyLabel;
    public Label opponentStatusLabel;

    public HBox opponentSignals;

    // ── Action log widgets ────────────────────────────────────
    public Label actionLine1, actionLine2, actionLine3;

    // ── Card overlay widgets ──────────────────────────────────
    public javafx.scene.layout.VBox      cardOverlay;
    public ImageView cardOverlayBack, cardOverlayFace;
    public Label     cardOverlayName, cardOverlayDesc, cardOverlayEffect;

    
    // =========================================================
    //  CONSTRUCTOR
    // =========================================================

    public PanelBuilder(ImageLoader images) {
        this.images = images;
    }

    // =========================================================
    //  PLAYER PANEL
    // =========================================================

    public void buildPlayerPanel(VBox container) {
        container.setAlignment(Pos.TOP_CENTER);
        playerPortrait.setPreserveRatio(true);
        playerEnergyBar.setPreserveRatio(true);
        VBox.setMargin(playerPortrait,  new Insets(11, -30, 0, 0));
        VBox.setMargin(playerEnergyBar, new Insets(0,  -30, 0, 0));

        playerNameLabel     = ledLabel("-",              "white",   12, true);
        playerTypeLabel     = ledLabel("Type: -",        "#aaaaaa", 10, false);
        playerOrigRoleLabel = ledLabel("Orig: -",        "white",   10, false);
        playerCurrRoleLabel = ledLabel("Curr: -",        "white",   10, false);
        playerIndexLabel    = ledLabel("0",              "#00ff88", 11, true);
        playerIndexLabel.setStyle(playerIndexLabel.getStyle() +
            "-fx-text-fill: white;" +
            "-fx-background-color: rgba(0,0,0,0.92);" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(255,255,255,0.18);" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 3 8 3 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 6, 0, 0, 2);"
        );
        StackPane playerPortraitPane = new StackPane(playerPortrait, playerIndexLabel);
        StackPane.setAlignment(playerIndexLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(playerIndexLabel, new Insets(0, 0, 8, 8));

        playerSignals = new HBox(6);
        playerSignals.setAlignment(Pos.CENTER_LEFT);
        playerSignals.setMaxWidth(180);

        playerEnergyLabel   = ledLabel("Energy: -/1000", "#00ff88", 11, true);
        playerStatusLabel   = ledLabel("Normal",         "#aaaaaa", 10, false);
        playerTurnLabel     = ledLabel("",               "#ffcc00", 12, true);

        container.getChildren().addAll(
            playerPortraitPane, playerNameLabel, playerTypeLabel,
            playerOrigRoleLabel, playerCurrRoleLabel,
            playerSignals, playerEnergyLabel,
            playerEnergyBar, playerStatusLabel, playerTurnLabel
        );
    }

    // =========================================================
    //  OPPONENT PANEL
    // =========================================================

    public void buildOpponentPanel(VBox container) {
        container.setAlignment(Pos.TOP_CENTER);
        opponentPortrait.setPreserveRatio(true);
        opponentEnergyBar.setPreserveRatio(true);

        opponentNameLabel     = ledLabel("-",              "white",   12, true);
        opponentTypeLabel     = ledLabel("Type: -",        "#aaaaaa", 10, false);
        opponentOrigRoleLabel = ledLabel("Orig: -",        "white",   10, false);
        opponentCurrRoleLabel = ledLabel("Curr: -",        "white",   10, false);
        opponentIndexLabel    = ledLabel("0",              "#ff6666", 11, true);
        opponentIndexLabel.setStyle(opponentIndexLabel.getStyle() +
            "-fx-text-fill: white;" +
            "-fx-background-color: rgba(0,0,0,0.92);" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(255,255,255,0.18);" +
            "-fx-border-radius: 6;" +
            "-fx-padding: 3 8 3 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 6, 0, 0, 2);"
        );
        StackPane opponentPortraitPane = new StackPane(opponentPortrait, opponentIndexLabel);
        StackPane.setAlignment(opponentIndexLabel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(opponentIndexLabel, new Insets(0, 0, 8, 8));

        opponentSignals = new HBox(6);
        opponentSignals.setAlignment(Pos.CENTER_LEFT);
        opponentSignals.setMaxWidth(180);

        opponentEnergyLabel   = ledLabel("Energy: -/1000", "#ff6666", 11, true);
        opponentStatusLabel   = ledLabel("Normal",         "#aaaaaa", 10, false);

        container.getChildren().addAll(
            opponentPortraitPane, opponentNameLabel, opponentTypeLabel,
            opponentOrigRoleLabel, opponentCurrRoleLabel,
            opponentSignals, opponentEnergyLabel,
            opponentEnergyBar, opponentStatusLabel
        );
    }

    // =========================================================
    //  ACTION LOG
    // =========================================================

    public void buildActionLog(VBox container) {
        container.setStyle("-fx-padding: 6;");
        Label title = ledLabel("ACTION LOG", "#ffcc00", 10, true);
        actionLine1 = ledLabel("", "white",   10, false);
        actionLine2 = ledLabel("", "#aaffaa", 10, false);
        actionLine3 = ledLabel("", "#aaaaff", 10, false);
        for (Label l : new Label[]{actionLine1, actionLine2, actionLine3}) {
            l.setWrapText(true);
            l.setMaxWidth(190);
        }
        container.getChildren().addAll(title, actionLine1, actionLine2, actionLine3);
    }

    // =========================================================
    //  DICE PANEL
    // =========================================================

    public void buildDicePanel(VBox container) {
        container.setStyle("-fx-padding: 4;");
        container.setAlignment(Pos.CENTER);
        Label title = ledLabel("LAST ROLL", "#ffcc00", 10, true);
        container.getChildren().add(title);
    }

    // =========================================================
    //  CARD OVERLAY
    // =========================================================

    /**
     * Builds the card-draw overlay and adds it to {@code boardContainer}.
     * After this call, {@link #cardOverlay} and related fields are ready.
     *
     * @param boardContainer the StackPane that holds the game board
     */
    public void buildCardOverlay(javafx.scene.layout.StackPane boardContainer) {
        cardOverlay = new VBox(10);
        cardOverlay.setAlignment(Pos.CENTER);
        cardOverlay.setStyle(
            "-fx-background-color: rgba(0,0,0,0.82);" +
            "-fx-padding: 28; -fx-background-radius: 16;");
        cardOverlay.setMaxWidth(270);
        cardOverlay.setMaxHeight(460);
        cardOverlay.setVisible(false);
        cardOverlay.setOpacity(0);

        cardOverlayBack = new ImageView(images.cardBackImage);
        cardOverlayBack.setFitWidth(180);
        cardOverlayBack.setFitHeight(220);
        cardOverlayBack.setPreserveRatio(true);

        cardOverlayFace = new ImageView();
        cardOverlayFace.setFitWidth(180);
        cardOverlayFace.setFitHeight(220);
        cardOverlayFace.setPreserveRatio(true);
        cardOverlayFace.setVisible(false);

        cardOverlayName = new Label();
        cardOverlayName.setStyle(
            "-fx-text-fill: #ffcc00; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-font-family: " + LED + ";");
        cardOverlayName.setWrapText(true);
        cardOverlayName.setMaxWidth(230);
        cardOverlayName.setAlignment(Pos.CENTER);

        cardOverlayDesc = new Label();
        cardOverlayDesc.setStyle(
            "-fx-text-fill: white; -fx-font-size: 11px;" +
            "-fx-font-family: " + LED + ";");
        cardOverlayDesc.setWrapText(true);
        cardOverlayDesc.setMaxWidth(230);
        cardOverlayDesc.setAlignment(Pos.CENTER);

        cardOverlayEffect = new Label();
        cardOverlayEffect.setStyle(
            "-fx-text-fill: #00ffff; -fx-font-size: 11px;" +
            "-fx-font-weight: bold; -fx-font-family: " + LED + ";");
        cardOverlayEffect.setWrapText(true);
        cardOverlayEffect.setMaxWidth(230);
        cardOverlayEffect.setAlignment(Pos.CENTER);

        Label hint = new Label("tap to continue");
        hint.setStyle("-fx-text-fill: #666; -fx-font-size: 10px; -fx-font-style: italic;");

        cardOverlay.getChildren().addAll(
            cardOverlayBack, cardOverlayFace,
            cardOverlayName, cardOverlayDesc, cardOverlayEffect, hint);

        boardContainer.getChildren().add(cardOverlay);
        javafx.scene.layout.StackPane.setAlignment(cardOverlay, Pos.CENTER);
    }

    // =========================================================
    //  CARD OVERLAY — show / dismiss
    // =========================================================

    /**
     * Populates and fades in the card overlay for the drawn card.
     *
     * @param card        the card that was just drawn
     * @param onDismiss   called when the player taps to dismiss
     */
    public void showCardOverlay(Card card, Runnable onDismiss) {
        cardOverlayName.setText(card.getName());
        cardOverlayDesc.setText(card.getDescription());
        cardOverlayEffect.setText("Effect: " + getCardEffectType(card.getName()));
        cardOverlayFace.setImage(images.getCardImage(card.getName()));
        cardOverlayBack.setVisible(true);
        cardOverlayFace.setVisible(false);
        cardOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), cardOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        PauseTransition hold = new PauseTransition(Duration.millis(800));
        hold.setOnFinished(e -> {
            cardOverlayBack.setVisible(false);
            cardOverlayFace.setVisible(true);
        });
        new SequentialTransition(fadeIn, hold).play();

        // Wire tap-to-dismiss; caller provides the post-dismiss Runnable
        cardOverlay.setOnMouseClicked(e -> dismissCardOverlay(onDismiss));
    }

    /** Fades out the overlay, then runs {@code onDismiss}. */
    public void dismissCardOverlay(Runnable onDismiss) {
        FadeTransition out = new FadeTransition(Duration.millis(200), cardOverlay);
        out.setFromValue(1);
        out.setToValue(0);
        out.setOnFinished(e -> {
            cardOverlay.setVisible(false);
            if (onDismiss != null) onDismiss.run();
        });
        out.play();
    }

    // =========================================================
    //  CARD EFFECT TYPE
    // =========================================================

    public static String getCardEffectType(String cardName) {
        switch (cardName) {
            case "Position Swap":      return "SWAP POSITIONS";
            case "2319 Alert":         return "OPPONENT → START";
            case "Contamination Code": return "PLAYER → START";
            case "Small Snatcher":     return "STEAL 50 ENERGY";
            case "Sneaky Thief":       return "STEAL 100 ENERGY";
            case "Mega Drain":         return "STEAL 150 ENERGY";
            case "Super Shield":       return "SHIELD ACTIVATED";
            case "Mind Scramble":      return "CONFUSION 2 TURNS";
            case "Total Confusion":    return "CONFUSION 3 TURNS";
            default:                   return "SPECIAL EFFECT";
        }
    }

    // =========================================================
    //  LED LABEL FACTORY
    // =========================================================

    /**
     * Creates a styled label using the LED font.
     * {@link #TEXT_SCALE} at the top of this file resizes all labels uniformly.
     */
    public static Label ledLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        int scaledSize = (int)(size * TEXT_SCALE);
        l.setStyle(
            "-fx-text-fill: " + color + ";" +
            "-fx-font-size: " + scaledSize + "px;" +
            "-fx-font-family: " + LED + ";" +
            (bold ? "-fx-font-weight: bold;" : ""));
        l.setWrapText(true);
        l.setMaxWidth(200);
        return l;
    }
    
    public void applyPanelFontSize(double newPanelWidth) {
        this.panelWidth = newPanelWidth;
        int base = (int) Math.max(8, newPanelWidth * 0.09);
        applyStyle(playerNameLabel,      "white",   base + 2, true);
        applyStyle(playerTypeLabel,      "#aaaaaa", base,     false);
        applyStyle(playerOrigRoleLabel,  "white",   base,     false);
        applyStyle(playerCurrRoleLabel,  "white",   base,     false);
        applyStyle(playerIndexLabel,     "#00ff88", base + 1, true);
        applyStyle(playerEnergyLabel,    "#00ff88", base + 1, true);
        applyStyle(playerStatusLabel,    "#aaaaaa", base,     false);
        applyStyle(playerTurnLabel,      "#ffcc00", base + 2, true);
        applyStyle(opponentNameLabel,    "white",   base + 2, true);
        applyStyle(opponentTypeLabel,    "#aaaaaa", base,     false);
        applyStyle(opponentOrigRoleLabel,"white",   base,     false);
        applyStyle(opponentCurrRoleLabel,"white",   base,     false);
        applyStyle(opponentIndexLabel,   "#ff6666", base + 1, true);
        applyStyle(opponentEnergyLabel,  "#ff6666", base + 1, true);
        applyStyle(opponentStatusLabel,  "#aaaaaa", base,     false);
    }

    private void applyStyle(Label l, String color, int size, boolean bold) {
        if (l == null) return;
        l.setStyle(
            "-fx-text-fill: " + color + ";" +
            "-fx-font-size: " + size + "px;" +
            "-fx-font-family: " + LED + ";" +
            (bold ? "-fx-font-weight: bold;" : ""));
    }

    /** Scales the card-draw overlay with the game board when the window is resized. */
    public void scaleCardOverlay(double boardSide) {
        if (cardOverlay == null || boardSide <= 0) return;
        double cardW = boardSide * 0.42;
        double cardH = boardSide * 0.52;
        cardOverlayBack.setFitWidth(cardW);
        cardOverlayBack.setFitHeight(cardH);
        cardOverlayFace.setFitWidth(cardW);
        cardOverlayFace.setFitHeight(cardH);
        cardOverlay.setMaxWidth(boardSide * 0.88);
        cardOverlay.setMaxHeight(boardSide * 1.05);
        int nameSize = (int) Math.max(11, boardSide * 0.028);
        int bodySize = (int) Math.max(9, boardSide * 0.022);
        cardOverlayName.setStyle(
            "-fx-text-fill: #ffcc00; -fx-font-size: " + nameSize + "px;" +
            "-fx-font-weight: bold; -fx-font-family: " + LED + ";");
        cardOverlayDesc.setStyle(
            "-fx-text-fill: white; -fx-font-size: " + bodySize + "px;" +
            "-fx-font-family: " + LED + ";");
        cardOverlayEffect.setStyle(
            "-fx-text-fill: #00ffff; -fx-font-size: " + bodySize + "px;" +
            "-fx-font-weight: bold; -fx-font-family: " + LED + ";");
        cardOverlayName.setMaxWidth(boardSide * 0.75);
        cardOverlayDesc.setMaxWidth(boardSide * 0.75);
        cardOverlayEffect.setMaxWidth(boardSide * 0.75);
    }
}
