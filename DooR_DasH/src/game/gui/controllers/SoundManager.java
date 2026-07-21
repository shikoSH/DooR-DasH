package game.gui.controllers;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager {

    private static final String AUDIO = "/game/resources/audio/";
    private static final double DUCK_VOLUME   = 0.15;
    private static final double NORMAL_VOLUME = 0.7;
    private final java.util.Map<String, AudioClip> clipCache = new java.util.HashMap<>();
    private SoundManager() {}

    /** Thread-safe initialization-on-demand holder. */
    private static final class Holder {
        static final SoundManager INSTANCE = new SoundManager();
    }

    public static SoundManager getInstance() {
        return Holder.INSTANCE;
    }

    public void playDoorOpening() {
        playSFX("door_opening.wav");   // was "door_opening.mp3"
    }

    public void playCardDraw() {
        playSFX("card_draw.wav");      // was "card_draw.mp3" — same MP3-decode delay applies here too
    }

    public void playPowerUp() {
        playSFX("power_up.wav");
    }

    public void playDiceRoll() {
        playSFX("dice_roll.wav");
    }

    public void playMovement() {
        playSFX("movement.wav");
    }
    
    public void playInvalidMove() {
        playSFX("invalid_move.wav");
    }
    
    public void playConveyorBelt() {
        playSFX("conveyor_belt.wav");
    }

    public void playContaminationSock() {
        playSFX("contamination_sock.wav");
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
            AudioClip clip = clipCache.computeIfAbsent(fileName, f -> new AudioClip(url.toString()));   // NEW
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
    public void preloadAll() {
        String[] files = {
            "door_opening.wav", "card_draw.wav", "power_up.wav",
            "dice_roll.wav", "movement.wav" ,
            "invalid_move.wav" , "conveyor_belt.wav" , "contamination_sock.wav"
        };
        for (String f : files) {
            URL url = getClass().getResource(AUDIO + f);
            if (url != null) clipCache.putIfAbsent(f, new AudioClip(url.toString()));
        }
    }
}