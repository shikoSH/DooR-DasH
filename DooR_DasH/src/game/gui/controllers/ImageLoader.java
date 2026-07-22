package game.gui.controllers;

import game.gui.controllers.GameUIConstants;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Centralized image loader with lazy loading and caching.
 * Pre-loads images during intro screen to prevent OutOfMemoryError during game screen loading.
 */
public class ImageLoader {
    
    private final HashMap<String, Image> cache = new HashMap<>();
    private ImageLoader() {}

    /** Thread-safe initialization-on-demand holder. */
    private static final class Holder {
        static final ImageLoader INSTANCE = new ImageLoader();
    }

    public static ImageLoader getInstance() {
        return Holder.INSTANCE;
    }
    
    /**
     * Load an image with lazy loading and caching.
     * @param filename The image filename (without path)
     * @return The loaded Image, or null if not found
     */
    public Image loadImage(String filename) {
        String key = GameUIConstants.IMG + filename;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        
        InputStream stream = getClass().getResourceAsStream(key);
        if (stream == null) {
            System.err.println("WARNING: Image not found: " + key);
            cache.put(key, null);
            return null;
        }
        
        try {
            Image image = new Image(stream);
            cache.put(key, image);
            return image;
        } catch (Exception e) {
            System.err.println("ERROR loading image: " + key);
            e.printStackTrace();
            cache.put(key, null);
            return null;
        }
    }
    
    /**
     * Pre-load all game images in the background.
     * Call this during intro screen to avoid memory pressure during game loading.
     */
    public void preloadGameImages() {
    	System.out.println("DEBUG: Starting pre-load of game images...");
        
        // Loading screen — needed first, right after the intro finishes
        loadImage("Loading_Screen.png");
        
        // Cell images
        loadImage("NormalCell.png");
        loadImage("Scarer_ClosedDoor_Cell2.png");
        loadImage("Laugher_ClosedDoor_Cell.png");
        loadImage("Scarer_OpenDoor_Cell.png");
        loadImage("Laugher_OpenDoor_Cell.png");
        loadImage("MonsterCell_Grey.png");
        loadImage("Conveyor_belt_cell.png");
        loadImage("contamination_sock_cell.png");
        loadImage("CardCell.png");
        
        // Background and UI images
        loadImage("background_Game.png");
        loadImage("ControlPanel.png");
        loadImage("Board_Holder.png");
        loadImage("images.png");
        loadImage("Power_Up_Button.png");
        loadImage("Roll_Button.png");
        loadImage("CardsDeck_Full.png");
        loadImage("CardsDeck_Mid.png");
        loadImage("CardsDeck_Least.png");
        
        // Status indicator images
        loadImage("turn_off.png");
        loadImage("turn_on.png");
        loadImage("confusion_off.png");
        loadImage("confusion_on.png");
        loadImage("freezed_off.png");
        loadImage("freezed_on.png");
        loadImage("shield_off.png");
        loadImage("shield_on.png");
        loadImage("powerup_off.png");
        loadImage("powerup_on.png");
        
        // Profile and UI elements
        loadImage("monster_profile.png");
        loadImage("Action_Log.png");
        loadImage("card_back_design.jpg");
        
        // Dice images
        for (int i = 1; i <= 6; i++) {
            loadImage("Dice_on_" + i + ".png");
            loadImage("Glowing_dice_on_" + i + ".png");
        }
        
        // Monster portraits - Scarer
        loadImage("celia mae.png");
        loadImage("Fungus.png");
        loadImage("Henry_J._Waternoose_III.png");
        loadImage("James sullivan.png");
        loadImage("Mike_Wazowski.png");
        loadImage("Randall.png");
        loadImage("Roz.png");
        loadImage("Yeti.png");
        
        // Monster portraits - Laugher
        loadImage("Celia_Mae_Screen.png");
        loadImage("FungusScreen.png");
        loadImage("Henry_Screen.png");
        loadImage("James_Screen.png");
        loadImage("Mike_Screen.png");
        loadImage("Randal_Screen.png");
        loadImage("Rose_Screen.png");
        loadImage("Yeti_Screen.png");
        
        // Energy bar images
        loadImage("0_Energy_Player.png");
        loadImage("25_Energy_Player.png");
        loadImage("50_Energy_Player.png");
        loadImage("75_Energy_Player.png");
        loadImage("100_Energy_Player.png");
        
        // Card images
        loadImage("2319_alert.png");
        loadImage("contamination_code.png");
        loadImage("mega_drain.png");
        loadImage("mind_scramble.png");
        loadImage("position_swap.png");
        loadImage("small_snatcher.png");
        loadImage("sneaky_theif.png");
        loadImage("super_shield.png");
        loadImage("total_confusion.png");
        
        System.out.println("DEBUG: Game images pre-load complete. Cache size: " + cache.size());
    }
    
    /**
     * Clear the image cache to free memory.
     * Use this when switching away from game screen to free resources.
     */
    public void clearCache() {
        cache.clear();
        System.out.println("DEBUG: Image cache cleared");
    }
    
    /**
     * Get current cache size for debugging.
     */
    public int getCacheSize() {
        return cache.size();
    }
}
