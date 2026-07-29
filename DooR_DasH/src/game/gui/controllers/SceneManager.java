package game.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SceneManager {

    private Stage primaryStage;
    private final StackPane sceneHolder = new StackPane();
    private Scene persistentScene;
    private final HashMap<String, Parent> cachedRoots = new HashMap<>();
    private MediaPlayer mediaPlayer;
    // The track list currently playing. Length 1 = single track, looped
    // forever. Length > 1 = plays in order and wraps back to the start
    // indefinitely (used for the game screen's two-song rotation).
    private String[] currentPlaylist;
    private int playlistIndex = 0;
    private Timeline musicFadeTimeline; // in-flight volume fade, if any
    private double savedMusicVolume = 0.25;
    private boolean fullScreenPromptShown = false;
    private boolean startScreenShownOnce = false;

    private static final double DEFAULT_WIDTH  = 1280;
    private static final double DEFAULT_HEIGHT = 720;

    // How long the fade-to-black / fade-from-black halves of every scene
    // transition take. Same value both ways so a transition always feels
    // symmetric; bump this up/down to make every scene change slower/faster.
    private static final double SCENE_FADE_MS = 350;

    // Only true while the game screen is showing — this lock is scoped
    // to just this screen. It's the only one that needs the window
    // itself to stay 16:9 for its printed control-panel artwork to fit
    // edge-to-edge; other screens use whatever window size they're
    // given correctly on their own.
    private boolean gameScreenActive = false;
    private boolean adjustingForAspect = false; // re-entrancy guard

    private SceneManager() {}

    /** Thread-safe initialization-on-demand holder. */
    private static final class Holder {
        static final SceneManager INSTANCE = new SceneManager();
    }

    public static SceneManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("DooR DasH: Scare vs Laugh Touchdown");

        // Loaded here — the very first thing that runs, before ANY screen
        // is shown — so the font is guaranteed registered before anything
        // could possibly try to use it. Previously this only happened
        // inside switchToStartScreen()'s first-time branch, which worked
        // most of the time but meant every screen's font depended on the
        // Start Screen having been the first thing to trigger it; loading
        // it here removes that ordering dependency entirely.
        //
        // IMPORTANT: Font.loadFont() can fail SILENTLY — it returns null
        // instead of throwing if the resource stream is missing/invalid,
        // so a try/catch alone doesn't tell you anything. This explicitly
        // checks and loudly prints the result, so it's actually possible
        // to tell whether the font registered or not from the console —
        // if you see the WARNING line below in your console output, the
        // font genuinely never loaded (a build/resource problem, not a
        // styling problem), and every -fx-font-family: 'ARCADECLASSIC'
        // in the whole app will fall back to a system font no matter how
        // many individual style rules get fixed.
        

        this.primaryStage.setWidth(DEFAULT_WIDTH);
        this.primaryStage.setHeight(DEFAULT_HEIGHT);
        this.primaryStage.setMinWidth(960);
        this.primaryStage.setMinHeight(640);
        this.primaryStage.setResizable(true);
        this.primaryStage.centerOnScreen();
        // Disable the built-in ESC key so we manage fullscreen toggling ourselves
        this.primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        this.primaryStage.setFullScreenExitHint("");

        sceneHolder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        // Black fill so any content swap never flashes the Scene/StackPane default white
        sceneHolder.setStyle("-fx-background-color: black;");
        persistentScene = new Scene(sceneHolder, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        persistentScene.setFill(Color.BLACK);

        try {
            URL fontURL = getClass().getResource(GameUIConstants.FONT_PATH);

            if (fontURL == null) {
                System.err.println("Font NOT FOUND: " + GameUIConstants.FONT_PATH);
                return;
            }

            System.out.println("Font URL = " + fontURL);

            Font loaded = Font.loadFont(fontURL.toExternalForm(), 14);

            if (loaded == null) {
                System.err.println("Could not parse font.");
                return;
            }

            System.out.println("Loaded family = " + loaded.getFamily());

            persistentScene.getRoot().setStyle(
                    "-fx-font-family:'" + loaded.getFamily() + "';"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        // ── Game-screen-only windowed 16:9 lock ─────────────────────────
        // Reacts to the SCENE's own live size (the real content area,
        // immune to guessing window-chrome thickness) rather than a
        // one-shot guess. Fires on every resize and self-corrects using
        // whatever the Stage-to-Scene delta actually is right now.
        persistentScene.widthProperty().addListener((obs, o, n) -> maintainGameAspectRatio());
        persistentScene.heightProperty().addListener((obs, o, n) -> maintainGameAspectRatio());

        // ESC toggles: fullscreen <-> 1280x720 windowed — never exits the game
        persistentScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (primaryStage.isFullScreen()) {
                    primaryStage.setFullScreen(false);
                    // Deferred: setting size immediately after
                    // setFullScreen(false) can race the OS's own
                    // fullscreen-exit animation on some platforms and
                    // silently get dropped, leaving the Scene's content
                    // laid out at its old (fullscreen) size even though
                    // the window itself is now smaller — this is what
                    // made the start/game-over screens look "stuck" at
                    // fullscreen size and cramped in the smaller window.
                    // Running this on the next pulse lets that
                    // transition finish first.
                    Platform.runLater(() -> {
                        primaryStage.setWidth(DEFAULT_WIDTH);
                        primaryStage.setHeight(DEFAULT_HEIGHT);
                        primaryStage.centerOnScreen();
                        sceneHolder.requestLayout();
                    });
                } else {
                    primaryStage.setFullScreen(true);
                }
                event.consume();
            }
        });
        primaryStage.setScene(persistentScene);
        // Start in fullscreen by default
        primaryStage.setFullScreen(true);
    }

    /**
     * Keeps the SCENE's content area (not the Stage's outer frame) at
     * exactly 16:9 while the game screen is showing and the window
     * isn't fullscreen. Only touches Stage height, computed from the
     * width the Scene just reported plus whatever the Stage-to-Scene
     * "chrome" delta happens to be right now.
     */
    private void maintainGameAspectRatio() {
        if (!gameScreenActive || adjustingForAspect || primaryStage == null) return;
        if (primaryStage.isFullScreen()) return;

        double sceneW = persistentScene.getWidth();
        double sceneH = persistentScene.getHeight();
        if (sceneW <= 0 || sceneH <= 0) return;

        double targetRatio  = DEFAULT_WIDTH / DEFAULT_HEIGHT; // 16:9
        double currentRatio = sceneW / sceneH;
        if (Math.abs(currentRatio - targetRatio) < 0.002) return; // already close enough

        adjustingForAspect = true;
        double chromeH = primaryStage.getHeight() - sceneH;
        double targetSceneH = sceneW / targetRatio;
        primaryStage.setHeight(targetSceneH + Math.max(0, chromeH));
        adjustingForAspect = false;
    }

    // ===== MUSIC =====
    //
    // All music now goes through crossfadeToPlaylist(), which smoothly
    // fades the OLD track's volume down to silence, swaps to the new
    // track(s), and fades back up to the saved volume — instead of the
    // old hard cut-and-replace. A "playlist" of length 1 just loops that
    // one track forever (main theme, win/loss); a playlist of length 2+
    // (the game screen's two songs) plays them in order and wraps back
    // to the start indefinitely, crossfading between EACH song change
    // too, not just on screen transitions.

    private static final String AUDIO_PATH = "/game/resources/audio/";
    // Faster default for everything — theme, game songs, song-to-song
    // rotation. Game-over gets its own longer, more dramatic duration.
    private static final double MUSIC_FADE_MS = 350;
    private static final double MUSIC_FADE_MS_GAME_OVER = 1500;

    /** Main theme — used by the intro and the start screen. */
    public void startMusic() {
        crossfadeToPlaylist(new String[]{ "monsters_inc_theme.mp3" }, MUSIC_FADE_MS);
    }

    /** Game screen — alternates between two songs, looping forever. */
    public void startGameMusic() {
        crossfadeToPlaylist(new String[]{ "GameScreen_Song1.mp3", "GameScreen_Song2.mp3" }, MUSIC_FADE_MS);
    }

    /** Game over screen — different track depending on whether the human player won. */
    public void startGameOverMusic(boolean playerWon) {
        crossfadeToPlaylist(
            new String[]{ playerWon ? "Win_Soundtrack.mp3" : "Loss_Soundtrack.mp3" },
            MUSIC_FADE_MS_GAME_OVER);
    }

    public void stopMusic() {
        if (musicFadeTimeline != null) musicFadeTimeline.stop();
        currentPlaylist = null;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMusicVolume(double v) {
        savedMusicVolume = Math.max(0, Math.min(1, v));
        // Only snap the live player's volume directly if there's no fade
        // in flight — otherwise this would fight the fade and cause a
        // jump. The fade itself always targets savedMusicVolume, so once
        // it finishes the slider value takes effect naturally anyway.
        if (mediaPlayer != null && musicFadeTimeline == null) {
            mediaPlayer.setVolume(savedMusicVolume);
        }
    }

    public double getMusicVolume() {
        return savedMusicVolume;
    }

    /**
     * Crossfades from whatever is currently playing into a new playlist.
     * Fades the old player's volume down to 0, disposes it, then starts
     * the new playlist faded in from 0 up to {@link #savedMusicVolume}.
     * If nothing is currently playing, skips straight to fading the new
     * track in from silence.
     */
    private void crossfadeToPlaylist(String[] tracks, double fadeMs) {
        if (currentPlaylist != null && java.util.Arrays.equals(currentPlaylist, tracks)) {
            // Already playing this exact playlist — nothing to do. This is
            // what makes it safe to call startMusic() every single time we
            // return to the start screen (from the game, from game over,
            // from the intro) instead of only once: if the theme is
            // already playing, this is a no-op instead of a pointless
            // fade-out-then-back-in blip.
            return;
        }
        if (musicFadeTimeline != null) musicFadeTimeline.stop();
        currentPlaylist = tracks;
        playlistIndex = 0;

        MediaPlayer oldPlayer = mediaPlayer;
        if (oldPlayer == null) {
            playCurrentPlaylistTrack(true, fadeMs);
            return;
        }

        fadeVolume(oldPlayer, oldPlayer.getVolume(), 0.0, fadeMs, () -> {
            oldPlayer.stop();
            oldPlayer.dispose();
            playCurrentPlaylistTrack(true, fadeMs);
        });
    }

    /** Loads and plays currentPlaylist[playlistIndex], wiring up auto-advance if it's a multi-track playlist. */
    private void playCurrentPlaylistTrack(boolean fadeIn, double fadeMs) {
        if (currentPlaylist == null || currentPlaylist.length == 0) return;
        String file = currentPlaylist[playlistIndex];
        try {
            URL musicUrl = getClass().getResource(AUDIO_PATH + file);
            if (musicUrl == null) {
                System.err.println("WARNING: Music file not found, skipping: " + file);
                return;
            }
            Media media = new Media(musicUrl.toString());
            MediaPlayer player = new MediaPlayer(media);
            mediaPlayer = player;

            if (currentPlaylist.length == 1) {
                // Single track — loop it forever, no manual advancing needed.
                player.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                // Multi-track playlist — crossfade into the next track once
                // this one ends, wrapping back to the start indefinitely.
                // Always uses the fast default, regardless of how long the
                // fade was that first brought us into this playlist — only
                // the ENTRY transition (e.g. into game-over) should be slow.
                player.setOnEndOfMedia(() -> {
                    playlistIndex = (playlistIndex + 1) % currentPlaylist.length;
                    crossfadeWithinPlaylist();
                });
            }

            player.setVolume(fadeIn ? 0.0 : savedMusicVolume);
            player.play();
            System.out.println("DEBUG: Music started: " + file);
            if (fadeIn) fadeVolume(player, 0.0, savedMusicVolume, fadeMs, null);
        } catch (Exception e) {
            System.err.println("WARNING: Could not play music: " + file + " - " + e.getMessage());
        }
    }

    /** Crossfades from the currently-playing playlist track into the next one (index already advanced). Always fast. */
    private void crossfadeWithinPlaylist() {
        MediaPlayer oldPlayer = mediaPlayer;
        if (oldPlayer == null) { playCurrentPlaylistTrack(true, MUSIC_FADE_MS); return; }
        fadeVolume(oldPlayer, oldPlayer.getVolume(), 0.0, MUSIC_FADE_MS, () -> {
            oldPlayer.stop();
            oldPlayer.dispose();
            playCurrentPlaylistTrack(true, MUSIC_FADE_MS);
        });
    }

    /** Smoothly animates a MediaPlayer's volume — FadeTransition only works on Nodes, so this uses a Timeline instead. */
    private void fadeVolume(MediaPlayer player, double from, double to, double fadeMs, Runnable onFinished) {
        if (musicFadeTimeline != null) musicFadeTimeline.stop();
        if (player == null) {
            if (onFinished != null) onFinished.run();
            return;
        }
        player.setVolume(from);
        Timeline fade = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(player.volumeProperty(), from)),
            new KeyFrame(Duration.millis(fadeMs), new KeyValue(player.volumeProperty(), to))
        );
        fade.setOnFinished(e -> {
            musicFadeTimeline = null;
            if (onFinished != null) onFinished.run();
        });
        musicFadeTimeline = fade;
        fade.play();
    }

    // ===== SCREENS =====

    public void switchToIntroScreen() {
        gameScreenActive = false;
        fadeToBlackThenShow(() -> {
            try {
                URL fxmlUrl = getClass().getResource("/game/gui/views/IntroScreen.fxml");
                if (fxmlUrl == null) {
                    System.err.println("ERROR: IntroScreen.fxml not found — going straight to StartScreen");
                    loadStartScreenContent();
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                switchToContent(root, true);
                showStageIfNeeded();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to load IntroScreen");
                e.printStackTrace();
                loadStartScreenContent();
            }
        });
    }

    public void switchToStartScreen() {
        // Triggered here — before the scene transition even begins — for
        // two reasons: (1) this is what makes returning to the start
        // screen from the game-over screen (or anywhere else) actually
        // switch back to the main theme, since previously only the intro
        // ever called startMusic(); (2) starting the crossfade now lets
        // it run IN PARALLEL with the fade-to-black/fade-in scene
        // transition instead of only starting once the screen is already
        // visible, which is what caused the few-second delay before music
        // was audible on other screens.
        startMusic();
        fadeToBlackThenShow(this::loadStartScreenContent, 0, 500);
    }

    private void loadStartScreenContent() {
        gameScreenActive = false;
        try {
            URL fxmlUrl = getClass().getResource("/game/gui/views/StartScreen.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERROR: StartScreen.fxml not found in classpath!");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            addStylesheetOnce("/game/gui/resources/css/styles.css");
            addStylesheetOnce("/game/gui/resources/css/start-screen.css");

            switchToContent(root, false);
            startScreenShownOnce = true;

            if (!fullScreenPromptShown) {
                Platform.runLater(this::showFullScreenPrompt);
            }
            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load StartScreen");
            e.printStackTrace();
        }
    }

    public void switchToInstructionsScreen() {
        gameScreenActive = false;
        fadeToBlackThenShow(() ->
            loadCachedScreen("InstructionsScreen", "/game/gui/views/InstructionsScreen.fxml"));
    }

    public void startGameScreen(game.engine.Role playerRole) {
        gameScreenActive = true;
        System.out.println("DEBUG: startGameScreen() called with role = " + playerRole);
        // Triggered here, before the fade-to-black even starts, so the
        // crossfade (fade old track out, fade new one in) runs IN
        // PARALLEL with the scene transition instead of only starting
        // once the game screen is already visible — that sequential
        // ordering is what caused the couple-second wait before music
        // was audible after arriving on the game screen.
        startGameMusic();
        fadeToBlackThenShow(() -> {
            try {
                URL fxmlUrl = getClass().getResource("/game/gui/views/GameScreen.fxml");
                if (fxmlUrl == null) {
                    System.err.println("ERROR: GameScreen.fxml not found in classpath!");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                System.out.println("DEBUG: GameScreen.fxml loaded OK");

                GameController controller = loader.getController();
                if (controller == null) {
                    System.err.println("ERROR: GameController is null — check fx:controller in GameScreen.fxml");
                    return;
                }
                controller.startGame(playerRole);
                System.out.println("DEBUG: controller.startGame() called OK");

                addStylesheetOnce("/game/gui/resources/css/styles.css");
                switchToContent(root, false);
                showStageIfNeeded();
                System.out.println("DEBUG: Switched to GameScreen successfully");

            } catch (IOException e) {
                System.err.println("ERROR: IOException loading GameScreen.fxml");
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected exception in startGameScreen()");
                e.printStackTrace();
            }
        }, 1000); // extra hold in black — gives the game screen's layout
                  // time to fully settle before it's revealed
    }

    public void switchToGameOverScreen(
            String winnerName,
            String winnerRole,
            game.engine.Role playerRole,
            String playerName,
            String playerRoleStr,
            int playerEnergy,
            String opponentName,
            String opponentRole,
            int opponentEnergy,
            boolean playerWon) {
        gameScreenActive = false;
        // See startGameScreen() for why this fires before the transition
        // instead of after.
        startGameOverMusic(playerWon);
        fadeToBlackThenShow(() -> {
            try {
                URL fxmlUrl = getClass().getResource("/game/gui/views/GameOverScreen.fxml");
                if (fxmlUrl == null) {
                    System.err.println("ERROR: GameOverScreen.fxml not found!");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();

                GameOverController controller = loader.getController();
                controller.setWinner(
                    winnerName, winnerRole, playerRole,
                    playerName, playerRoleStr, playerEnergy,
                    opponentName, opponentRole, opponentEnergy
                );

                addStylesheetOnce("/game/gui/resources/css/styles.css");
                switchToContent(root, false);
                showStageIfNeeded();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to load GameOverScreen");
                e.printStackTrace();
            }
        }, 0, 500);   // same 900ms fade as switchToStartScreen()
    }

    private void loadCachedScreen(String name, String fxmlPath) {
        try {
            if (!cachedRoots.containsKey(name)) {
                URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl == null) {
                    System.err.println("ERROR: FXML not found: " + fxmlPath);
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                addStylesheetOnce("/game/gui/resources/css/styles.css");
                cachedRoots.put(name, root);
            }
            switchToContent(cachedRoots.get(name), false);
            showStageIfNeeded();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Generic fade-to-black-then-in transition used by EVERY scene switch
     * above. Fades a solid black rectangle IN over whatever's currently
     * showing, then — once fully black — runs {@code swapContent} to load
     * and attach the new screen, then fades the black cover back OUT to
     * reveal it. This is the same "fade to black, then black fades into
     * the next screen" effect the intro→start transition used to have on
     * its own; centralising it here means every screen change gets it for
     * free instead of each caller re-implementing its own crossfade.
     *
     * {@code swapContent} typically calls {@link #switchToContent} (or
     * {@link #loadCachedScreen}), which replaces sceneHolder's entire
     * children list with just the new screen — that would also wipe out
     * the black cover, so it's deliberately re-added immediately after
     * swapContent runs, still on the FX thread before the next render
     * pulse, so the new (possibly not-yet-laid-out) content is never
     * shown unprotected even for a single frame.
     */
    private void fadeToBlackThenShow(Runnable swapContent) {
        fadeToBlackThenShow(swapContent, 0, SCENE_FADE_MS);
    }

    private void fadeToBlackThenShow(Runnable swapContent, double extraHoldMs) {
        fadeToBlackThenShow(swapContent, extraHoldMs, SCENE_FADE_MS);
    }

    private void fadeToBlackThenShow(Runnable swapContent, double extraHoldMs, double fadeMs) {
        if (primaryStage == null) {
            swapContent.run();
            return;
        }

        Rectangle blackCover = new Rectangle();
        blackCover.setFill(Color.BLACK);
        blackCover.widthProperty().bind(sceneHolder.widthProperty());
        blackCover.heightProperty().bind(sceneHolder.heightProperty());
        blackCover.setMouseTransparent(true);

        Runnable swapThenRevealFromBlack = () -> {
            swapContent.run();
            sceneHolder.getChildren().add(blackCover);
            blackCover.toFront();

            Platform.runLater(() -> Platform.runLater(() -> {
                PauseTransition extraHold = new PauseTransition(Duration.millis(Math.max(0, extraHoldMs)));
                extraHold.setOnFinished(pe -> {
                    FadeTransition fadeFromBlack = new FadeTransition(Duration.millis(fadeMs), blackCover);
                    fadeFromBlack.setFromValue(1);
                    fadeFromBlack.setToValue(0);
                    fadeFromBlack.setOnFinished(ev -> sceneHolder.getChildren().remove(blackCover));
                    fadeFromBlack.play();
                });
                extraHold.play();
            }));
        };

        if (sceneHolder.getChildren().isEmpty()) {
            blackCover.setOpacity(1);
            sceneHolder.getChildren().add(blackCover);
            swapThenRevealFromBlack.run();
            return;
        }

        blackCover.setOpacity(0);
        sceneHolder.getChildren().add(blackCover);
        blackCover.toFront();

        FadeTransition fadeToBlack = new FadeTransition(Duration.millis(fadeMs), blackCover);
        fadeToBlack.setFromValue(0);
        fadeToBlack.setToValue(1);
        fadeToBlack.setOnFinished(e -> swapThenRevealFromBlack.run());
        fadeToBlack.play();
    }

    /**
     * Swaps the visible screen inside one persistent {@link Scene} so the stage
     * never calls {@code setScene} again (that resets size and exits fullscreen).
     */
    private void switchToContent(Parent content, boolean showPrompt) {
        if (content == null || primaryStage == null) return;

        releaseRootBindings();
        prepareRootForFill(content);
        sceneHolder.getChildren().setAll(content);

        // Force the new content's bound width/height to resolve to their
        // real, final values RIGHT NOW instead of waiting for the next
        // pulse. Without this, a screen's very first layout computation
        // (e.g. GameController's applyAllLayout(), triggered off its own
        // sceneProperty listener) could run while its own width/height
        // were still 0/stale, silently no-op, and the screen would
        // render briefly at its raw unscaled size before "snapping" into
        // the correctly-scaled layout a moment later — this is what
        // caused the game screen's UI to look oversized for an instant
        // right after arriving before jumping into place.
        sceneHolder.applyCss();
        sceneHolder.layout();

        if (showPrompt) {
            Platform.runLater(this::showFullScreenPrompt);
        }
    }

    /** Unbind size properties from the outgoing screen so the next screen can bind cleanly. */
    private void releaseRootBindings() {
        if (sceneHolder.getChildren().isEmpty()) return;
        javafx.scene.Node old = sceneHolder.getChildren().get(0);
        if (old instanceof Region) {
            Region region = (Region) old;
            try { region.prefWidthProperty().unbind(); }  catch (Exception ignored) {}
            try { region.prefHeightProperty().unbind(); } catch (Exception ignored) {}
        }
    }

    /** Fit the screen root to the stage without forcing a larger minimum size. */
    private void prepareRootForFill(Parent root) {
        if (!(root instanceof Region)) return;
        Region region = (Region) root;
        region.setMinWidth(0);
        region.setMinHeight(0);
        region.setMaxWidth(Double.MAX_VALUE);
        region.setMaxHeight(Double.MAX_VALUE);
        region.prefWidthProperty().bind(sceneHolder.widthProperty());
        region.prefHeightProperty().bind(sceneHolder.heightProperty());
    }

    private void showStageIfNeeded() {
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    /** In-scene toast that slides in from the top, holds, then fades out. No separate window. */
    private void showFullScreenPrompt() {
        if (fullScreenPromptShown || primaryStage == null) return;
        fullScreenPromptShown = true;

        Label toast = new Label("Press  ESC  to toggle fullscreen / windowed");
        toast.setStyle(
            "-fx-background-color: rgba(15,15,25,0.88);" +
            "-fx-background-radius: 30;" +
            "-fx-border-color: rgba(100,200,255,0.55);" +
            "-fx-border-radius: 30;" +
            "-fx-border-width: 1.5;" +
            "-fx-text-fill: #e0f0ff;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-padding: 10 28 10 28;"
        );
        toast.setMouseTransparent(true);

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(18, 0, 0, 0));
        toast.setOpacity(0);
        toast.setTranslateY(-20);

        sceneHolder.getChildren().add(toast);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), toast);
        slideIn.setFromY(-20);
        slideIn.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        ParallelTransition enterAnim = new ParallelTransition(slideIn, fadeIn);

        PauseTransition hold = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> sceneHolder.getChildren().remove(toast));

        new SequentialTransition(enterAnim, hold, fadeOut).play();
    }

    private void addStylesheetOnce(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("WARNING: Stylesheet not found, skipping: " + path);
            return;
        }
        String external = url.toExternalForm();
        if (!persistentScene.getStylesheets().contains(external)) {
            persistentScene.getStylesheets().add(external);
        }
    }
}