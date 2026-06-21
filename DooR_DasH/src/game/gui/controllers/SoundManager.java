package game.gui.controllers;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {

    private static final String AUDIO = "/game/resources/audio/";
    private static final double DUCK_VOLUME   = 0.15;
    private static final double NORMAL_VOLUME = 0.7;

    private SoundManager() {}

    /** Thread-safe initialization-on-demand holder. */
    private static final class Holder {
        static final SoundManager INSTANCE = new SoundManager();
    }

    public static SoundManager getInstance() {
        return Holder.INSTANCE;
    }

    public void playDoorOpening() {
        playSFX("door_opening.mp3");
    }

    public void playCardDraw() {
        playSFX("card_draw.mp3");
    }

    public void playPowerUp() {
        playSFX("power_up.mp3");
    }

    /**
     * Plays a short SFX clip using AudioClip (fires immediately, no buffering delay)
     * and briefly ducks the background music while it plays.
     */
    private void playSFX(String fileName) {
        try {
            URL url = getClass().getResource(AUDIO + fileName);
            if (url == null) {
                System.err.println("SFX not found: " + AUDIO + fileName);
                return;
            }
            MediaPlayer music = SceneManager.getInstance().getMediaPlayer();
            final double volumeBeforeSfx = (music != null) ? music.getVolume() : NORMAL_VOLUME;
            final boolean musicWasAudible = volumeBeforeSfx > 0;
            if (music != null && musicWasAudible) {
                music.setVolume(DUCK_VOLUME);
            }
            AudioClip clip = new AudioClip(url.toString());
            clip.setVolume(0.85);
            clip.play();
            javafx.animation.PauseTransition restore =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
            restore.setOnFinished(e -> {
                if (music != null && musicWasAudible) {
                    music.setVolume(volumeBeforeSfx);
                }
            });
            restore.play();
        } catch (Exception e) {
            System.err.println("Failed to play SFX: " + fileName + " — " + e.getMessage());
        }
    }
}