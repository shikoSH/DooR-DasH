package game.gui.controllers;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;

public class SoundManager {

    private static SoundManager instance;
    private static final String AUDIO = "/game/resources/audio/";
    private static final double DUCK_VOLUME  = 0.15; // music volume while SFX plays
    private static final double NORMAL_VOLUME = 0.7;  // normal music volume

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void playDoorOpening() {
        playSFX("door_opening.mp3");
    }

    public void playCardDraw() {
        playSFX("card_draw.mp3");
    }

    private void playSFX(String fileName) {
        try {
            URL url = getClass().getResource(AUDIO + fileName);
            if (url == null) {
                System.err.println("SFX not found: " + fileName);
                return;
            }

            // Duck the background music
            MediaPlayer music = SceneManager.getInstance().getMediaPlayer();
            if (music != null) music.setVolume(DUCK_VOLUME);

            // Play the sound effect
            Media media = new Media(url.toString());
            MediaPlayer sfx = new MediaPlayer(media);
            sfx.play();

            // Restore music volume when SFX finishes
            sfx.setOnEndOfMedia(() -> {
                if (music != null) music.setVolume(NORMAL_VOLUME);
                sfx.dispose();
            });

            // Safety fallback — restore after 5 seconds in case onEndOfMedia doesn't fire
            sfx.setOnError(() -> {
                if (music != null) music.setVolume(NORMAL_VOLUME);
            });

        } catch (Exception e) {
            System.err.println("Failed to play SFX: " + fileName + " — " + e.getMessage());
        }
    }
}