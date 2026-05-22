package game.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import game.engine.Board;
import game.engine.Game;
import game.engine.monsters.Monster;

/**
 * Reads the current {@link Game} state and pushes updates to every label and
 * image on the player / opponent panels and the top status bar.
 *
 * Worker contract: call {@link #updateUI(Game, Label)} after every board
 * refresh, and {@link #updateDeckImage(ImageView)} whenever the deck could
 * have changed. All widget references are injected via the constructor so
 * this class has zero JavaFX @FXML dependencies.
 */
public class UIUpdater {

    // ── Font / scale constants ────────────────────────────────
    private static final String LED        = PanelBuilder.LED;
    private static final double TEXT_SCALE = PanelBuilder.TEXT_SCALE;

    // ── Image source ──────────────────────────────────────────
    private final ImageLoader images;

    // ── Player panel widgets ──────────────────────────────────
    private final ImageView playerPortrait, playerEnergyBar;
    private final Label playerNameLabel, playerTypeLabel, playerOrigRoleLabel;
    private final Label playerCurrRoleLabel, playerIndexLabel, playerEnergyLabel;
    private final Label playerStatusLabel, playerTurnLabel;
    private final HBox  playerSignals;

    // ── Opponent panel widgets ───────────────────────────────
    private final ImageView opponentPortrait, opponentEnergyBar;
    private final Label opponentNameLabel, opponentTypeLabel, opponentOrigRoleLabel;
    private final Label opponentCurrRoleLabel, opponentIndexLabel, opponentEnergyLabel;
    private final Label opponentStatusLabel;
    private final HBox  opponentSignals;

    // =========================================================
    //  CONSTRUCTOR — receives every widget it needs to update
    // =========================================================

    public UIUpdater(
            ImageLoader images,
            // player panel
            ImageView playerPortrait,    ImageView playerEnergyBar,
            Label playerNameLabel,       Label playerTypeLabel,
            Label playerOrigRoleLabel,   Label playerCurrRoleLabel,
            Label playerIndexLabel,      Label playerEnergyLabel,
            Label playerStatusLabel,     Label playerTurnLabel,
            HBox playerSignals,
            // opponent panel
            ImageView opponentPortrait,  ImageView opponentEnergyBar,
            Label opponentNameLabel,     Label opponentTypeLabel,
            Label opponentOrigRoleLabel, Label opponentCurrRoleLabel,
            Label opponentIndexLabel,    Label opponentEnergyLabel,
            Label opponentStatusLabel,   HBox opponentSignals) {

        this.images = images;

        this.playerPortrait      = playerPortrait;
        this.playerEnergyBar     = playerEnergyBar;
        this.playerNameLabel     = playerNameLabel;
        this.playerTypeLabel     = playerTypeLabel;
        this.playerOrigRoleLabel = playerOrigRoleLabel;
        this.playerCurrRoleLabel = playerCurrRoleLabel;
        this.playerIndexLabel    = playerIndexLabel;
        this.playerEnergyLabel   = playerEnergyLabel;
        this.playerStatusLabel   = playerStatusLabel;
        this.playerTurnLabel     = playerTurnLabel;
        this.playerSignals       = playerSignals;

        this.opponentPortrait      = opponentPortrait;
        this.opponentEnergyBar     = opponentEnergyBar;
        this.opponentNameLabel     = opponentNameLabel;
        this.opponentTypeLabel     = opponentTypeLabel;
        this.opponentOrigRoleLabel = opponentOrigRoleLabel;
        this.opponentCurrRoleLabel = opponentCurrRoleLabel;
        this.opponentIndexLabel    = opponentIndexLabel;
        this.opponentEnergyLabel   = opponentEnergyLabel;
        this.opponentStatusLabel   = opponentStatusLabel;
        this.opponentSignals       = opponentSignals;
    }

    // =========================================================
    //  UPDATE UI
    // =========================================================

    /**
     * Refreshes every label and portrait on both side panels.
     *
     * @param game      the live game instance
     * @param turnLabel the large top-center "YOUR TURN" label owned by GameController
     */
    public void updateUI(Game game, Label turnLabel) {
        if (game == null) return;
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        Monster current  = game.getCurrent();

        // ── Player panel ──────────────────────────────────────
        playerPortrait.setImage(images.getMonsterScreenImage(player.getName()));
        playerNameLabel.setText(player.getName());
        playerTypeLabel.setText("Type: " + player.getClass().getSimpleName());
        playerOrigRoleLabel.setText("Orig: " + player.getOriginalRole());
        playerCurrRoleLabel.setText("Curr: " + player.getRole());

        boolean pConfused = !player.getOriginalRole().equals(player.getRole());
        playerCurrRoleLabel.setStyle(
            "-fx-text-fill: " + (pConfused ? "#ff00ff" : "white") + ";" +
            "-fx-font-size: " + (int)(10 * TEXT_SCALE) + "px;" +
            "-fx-font-family: " + LED + ";" +
            (pConfused ? "-fx-font-weight: bold;" : ""));

        playerIndexLabel.setText(String.valueOf(player.getPosition()));
        playerEnergyLabel.setText("Energy: " + player.getEnergy() + "/1000");
        animateEnergyBar(playerEnergyBar, player.getEnergy());
        playerStatusLabel.setText(buildStatusString(player));
        // Populate signal badges for active effects
        if (playerSignals != null) {
            playerSignals.getChildren().clear();
            if (player.isShielded()) playerSignals.getChildren().add(makeSignal("Shield", "#FFD700"));
            if (player.isConfused()) playerSignals.getChildren().add(makeSignal("Confused:" + player.getConfusionTurns() + "t", "#ff00ff"));
            if (player.isFrozen()) playerSignals.getChildren().add(makeSignal("Frozen", "#00ffff"));
            if (player instanceof game.engine.monsters.Dasher) {
                int mt = ((game.engine.monsters.Dasher) player).getMomentumTurns();
                if (mt > 0) playerSignals.getChildren().add(makeSignal("Rush:" + mt + "t", "#ff8800"));
            }
            if (player instanceof game.engine.monsters.MultiTasker) {
                int ft = ((game.engine.monsters.MultiTasker) player).getNormalSpeedTurns();
                if (ft > 0) playerSignals.getChildren().add(makeSignal("Focus:" + ft + "t", "#00ff88"));
            }
        }
        playerTurnLabel.setText(current == player ? "▶ YOUR TURN" : "");

        // ── Opponent panel ────────────────────────────────────
        opponentPortrait.setImage(images.getMonsterScreenImage(opponent.getName()));
        opponentNameLabel.setText(opponent.getName());
        opponentTypeLabel.setText("Type: " + opponent.getClass().getSimpleName());
        opponentOrigRoleLabel.setText("Orig: " + opponent.getOriginalRole());
        opponentCurrRoleLabel.setText("Curr: " + opponent.getRole());

        boolean oConfused = !opponent.getOriginalRole().equals(opponent.getRole());
        opponentCurrRoleLabel.setStyle(
            "-fx-text-fill: " + (oConfused ? "#ff00ff" : "white") + ";" +
            "-fx-font-size: " + (int)(10 * TEXT_SCALE) + "px;" +
            "-fx-font-family: " + LED + ";" +
            (oConfused ? "-fx-font-weight: bold;" : ""));

        opponentIndexLabel.setText(String.valueOf(opponent.getPosition()));
        opponentEnergyLabel.setText("Energy: " + opponent.getEnergy() + "/1000");
        animateEnergyBar(opponentEnergyBar, opponent.getEnergy());
        opponentStatusLabel.setText(buildStatusString(opponent));
        if (opponentSignals != null) {
            opponentSignals.getChildren().clear();
            if (opponent.isShielded()) opponentSignals.getChildren().add(makeSignal("Shield", "#FFD700"));
            if (opponent.isConfused()) opponentSignals.getChildren().add(makeSignal("Confused:" + opponent.getConfusionTurns() + "t", "#ff00ff"));
            if (opponent.isFrozen()) opponentSignals.getChildren().add(makeSignal("Frozen", "#00ffff"));
            if (opponent instanceof game.engine.monsters.Dasher) {
                int mt = ((game.engine.monsters.Dasher) opponent).getMomentumTurns();
                if (mt > 0) opponentSignals.getChildren().add(makeSignal("Rush:" + mt + "t", "#ff8800"));
            }
            if (opponent instanceof game.engine.monsters.MultiTasker) {
                int ft = ((game.engine.monsters.MultiTasker) opponent).getNormalSpeedTurns();
                if (ft > 0) opponentSignals.getChildren().add(makeSignal("Focus:" + ft + "t", "#00ff88"));
            }
        }

        // ── Turn label ────────────────────────────────────────
        turnLabel.setText(current == player
            ? "YOUR TURN — press ROLL!"
            : "OPPONENT'S TURN — press ROLL!");
        turnLabel.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-font-family: " + LED + ";" +
            "-fx-padding: 5 0 3 0; -fx-text-fill: " +
            (current == player ? "#00ff88" : "#ff6666") + ";");
    }

    // =========================================================
    //  DECK IMAGE
    // =========================================================

    /**
     * Updates the deck ImageView to reflect how many cards remain.
     *
     * @param cardDeckView the ImageView showing the deck
     */
    public void updateDeckImage(ImageView cardDeckView) {
        if (cardDeckView == null) return;
        int remaining = Board.cards.size();
        int total     = Board.getOriginalCards().size();
        if (total == 0) return;
        double ratio = (double) remaining / total;
        cardDeckView.setImage(ratio > 0.60 ? images.deckFull
            : ratio > 0.25 ? images.deckMid : images.deckLeast);
    }

    // =========================================================
    //  ENERGY BAR ANIMATION
    // =========================================================

    /**
     * Cross-fades the energy bar to the appropriate tier image.
     * Does nothing if the image is already correct (avoids redundant fades).
     */
    public void animateEnergyBar(ImageView bar, int energy) {
        int pct = (int) Math.min(100, Math.max(0, (energy / 1000.0) * 100));
        Image target;
        if      (pct >= 75) target = images.energy100;
        else if (pct >= 50) target = images.energy75;
        else if (pct >= 25) target = images.energy50;
        else if (pct > 0)   target = images.energy25;
        else                target = images.energy0;

        if (bar.getImage() == target) return;

        FadeTransition out = new FadeTransition(Duration.millis(200), bar);
        out.setFromValue(1);
        out.setToValue(0);
        out.setOnFinished(e -> {
            bar.setImage(target);
            FadeTransition in = new FadeTransition(Duration.millis(200), bar);
            in.setFromValue(0);
            in.setToValue(1);
            in.play();
        });
        out.play();
    }

    // =========================================================
    //  STATUS STRING
    // =========================================================

    /**
     * Produces a compact human-readable status line for a monster
     * (e.g. "[Shield] [Confused:2t]") or "Normal" if nothing is active.
     */
    public static String buildStatusString(Monster m) {
        StringBuilder sb = new StringBuilder();
        if (m.isShielded()) sb.append("[Shield] ");
        if (m.isConfused()) sb.append("[Confused:").append(m.getConfusionTurns()).append("t] ");
        if (m.isFrozen())   sb.append("[Frozen] ");
        if (m instanceof game.engine.monsters.Dasher) {
            int mt = ((game.engine.monsters.Dasher) m).getMomentumTurns();
            if (mt > 0) sb.append("[Rush:").append(mt).append("t] ");
        }
        if (m instanceof game.engine.monsters.MultiTasker) {
            int ft = ((game.engine.monsters.MultiTasker) m).getNormalSpeedTurns();
            if (ft > 0) sb.append("[Focus:").append(ft).append("t] ");
        }
        return sb.length() == 0 ? "Normal" : sb.toString().trim();
    }

    // Helper to create a compact signal badge
    private Label makeSignal(String text, String bgColor) {
        Label l = new Label(text);
        int size = (int)(9 * TEXT_SCALE);
        l.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: " + size + "px;" +
            "-fx-font-family: " + LED + ";" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 2 8 2 8;" +
            "-fx-border-color: rgba(255,255,255,0.12); -fx-border-radius:6;"
        );
        return l;
    }
}
