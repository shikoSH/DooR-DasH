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

            MediaPlayer music = SceneManager.getInstance().getMediaPlayer();
            final double volumeBeforeSfx = (music != null) ? music.getVolume() : NORMAL_VOLUME;
            final boolean musicWasAudible = volumeBeforeSfx > 0;

            if (music != null && musicWasAudible) {
                music.setVolume(DUCK_VOLUME);
            }

            Media media = new Media(url.toString());
            MediaPlayer sfx = new MediaPlayer(media);
            sfx.play();

            Runnable restoreMusic = () -> {
                if (music != null && musicWasAudible) {
                    music.setVolume(volumeBeforeSfx);
                }
                sfx.dispose();
            };

            sfx.setOnEndOfMedia(restoreMusic::run);
            sfx.setOnError(restoreMusic::run);

        } catch (Exception e) {
            System.err.println("Failed to play SFX: " + fileName + " — " + e.getMessage());
        }
    }
}