package game.gui.controllers;

import javafx.scene.image.Image;

/**
 * Loads and caches every image used by the game screen.
 * Pass one instance of this class to every helper that needs images —
 * each file is read from disk only once thanks to IMAGE_CACHE.
 *
 * Worker contract: construct once in GameController.initialize(), then
 * call loadAll() before accessing any image field.
 */
public class ImageLoader {

    private static final String IMG = "/game/gui/resources/images/";

    // Image cache — avoids reloading the same file multiple times
    private static final java.util.HashMap<String, Image> IMAGE_CACHE = new java.util.HashMap<>();

    // ── Cell images ───────────────────────────────────────────
    public Image normalImage, ScarerdoorImage, laugherdoorImage;
    public Image scarerOpenDoorImage, laugherOpenDoorImage;
    public Image monsterCellGreyImage, conveyorImage, contaminationImage, cardCellImage;

    // ── Monster board sprites ─────────────────────────────────
    public Image monsterImage_celia_mae, monsterImage_Fungus;
    public Image monsterImage_Henry_J_Waternoose_III, monsterImage_James_sullivan;
    public Image monsterImage_Mike_Wazowski, monsterImage_Randall, monsterImage_Roz, monsterImage_Yeti;

    // ── Monster screen/panel portraits ────────────────────────
    public Image screenImage_celia_mae, screenImage_Fungus, screenImage_Henry;
    public Image screenImage_Mike, screenImage_Randall, screenImage_Roz, screenImage_Yeti;
    public Image screenImage_James;

    // ── Energy bar images ─────────────────────────────────────
    public Image energy0, energy25, energy50, energy75, energy100;

    // ── Deck images ───────────────────────────────────────────
    public Image deckFull, deckMid, deckLeast;

    // ── Dice images ───────────────────────────────────────────
    public Image[] diceImages = new Image[6];

    // ── Card images ───────────────────────────────────────────
    public Image cardBackImage, card_2319Alert, cardContaminationCode;
    public Image cardMegaDrain, cardMindScramble, cardPositionSwap;
    public Image cardSmallSnatcher, cardSneakyThief, cardSuperShield, cardTotalConfusion;

    // ── UI button / background images (returned for external use) ─
    public Image powerUpButtonImage, rollButtonImage;
    public Image backgroundGameImage, controlPanelImage, boardHolderImage;

    // =========================================================
    //  LOAD ALL
    // =========================================================

    /**
     * Must be called once before any image field is accessed.
     * Safe to call multiple times — cached results are returned immediately.
     */
    public void loadAll() {
        backgroundGameImage  = load(IMG + "background_Game.png");
        controlPanelImage    = load(IMG + "ControlPanel.png");
        boardHolderImage     = load(IMG + "Board_Holder.png");

        normalImage                         = load(IMG + "NormalCell.png");
        ScarerdoorImage                     = load(IMG + "Scarer_ClosedDoor_Cell2.png");
        laugherdoorImage                    = load(IMG + "Laugher_ClosedDoor_Cell.png");
        scarerOpenDoorImage                 = load(IMG + "Scarer_OpenDoor_Cell.png");
        laugherOpenDoorImage                = load(IMG + "Laugher_OpenDoor_Cell.png");
        monsterCellGreyImage                = load(IMG + "MonsterCell_Grey.png");
        conveyorImage                       = load(IMG + "Conveyor_belt_cell.png");
        contaminationImage                  = load(IMG + "contamination_sock_cell.png");
        cardCellImage                       = load(IMG + "CardCell.png");

        monsterImage_celia_mae              = load(IMG + "celia mae.png");
        monsterImage_Fungus                 = load(IMG + "Fungus.png");
        monsterImage_Henry_J_Waternoose_III = load(IMG + "Henry_J._Waternoose_III.png");
        monsterImage_James_sullivan         = load(IMG + "James sullivan.png");
        monsterImage_Mike_Wazowski          = load(IMG + "Mike_Wazowski.png");
        monsterImage_Randall                = load(IMG + "Randall.png");
        monsterImage_Roz                    = load(IMG + "Roz.png");
        monsterImage_Yeti                   = load(IMG + "Yeti.png");

        screenImage_James     = load(IMG + "James_Screen.png");
        screenImage_celia_mae = load(IMG + "Celia_Mae_Screen.png");
        screenImage_Fungus    = load(IMG + "FungusScreen.png");
        screenImage_Henry     = load(IMG + "Henry_Screen.png");
        screenImage_Mike      = load(IMG + "Mike_Screen.png");
        screenImage_Randall   = load(IMG + "Randal_Screen.png");
        screenImage_Roz       = load(IMG + "Rose_Screen.png");
        screenImage_Yeti      = load(IMG + "Yeti_Screen.png");

        energy0   = load(IMG + "0_Energy_Player.png");
        energy25  = load(IMG + "25_Energy_Player.png");
        energy50  = load(IMG + "50_Energy_Player.png");
        energy75  = load(IMG + "75_Energy_Player.png");
        energy100 = load(IMG + "100_Energy_Player.png");

        deckFull  = load(IMG + "CardsDeck_Full.png");
        deckMid   = load(IMG + "CardsDeck_Mid.png");
        deckLeast = load(IMG + "CardsDeck_Least.png");

        for (int i = 1; i <= 6; i++)
            diceImages[i - 1] = load(IMG + "Dice_on_" + i + ".png");

        cardBackImage         = load(IMG + "card_back_design.jpg");
        card_2319Alert        = load(IMG + "2319_alert.png");
        cardContaminationCode = load(IMG + "contamination_code.png");
        cardMegaDrain         = load(IMG + "mega_drain.png");
        cardMindScramble      = load(IMG + "mind_scramble.png");
        cardPositionSwap      = load(IMG + "position_swap.png");
        cardSmallSnatcher     = load(IMG + "small_snatcher.png");
        cardSneakyThief       = load(IMG + "sneaky_theif.png");
        cardSuperShield       = load(IMG + "super_shield.png");
        cardTotalConfusion    = load(IMG + "total_confusion.png");

        powerUpButtonImage = load(IMG + "Power_Up_Button.png");
        rollButtonImage    = load(IMG + "Roll_Button.png");
    }

    // =========================================================
    //  IMAGE LOOKUP HELPERS
    // =========================================================

    /** Returns the board-sprite image for the given monster name (case-insensitive). */
    public Image getMonsterImage(String name) {
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":               return monsterImage_celia_mae;
            case "fungus":                  return monsterImage_Fungus;
            case "henry j. waternoose":
            case "henry j. waternoose iii": return monsterImage_Henry_J_Waternoose_III;
            case "james p. sullivan":
            case "james sullivan":          return monsterImage_James_sullivan;
            case "mike wazowski":           return monsterImage_Mike_Wazowski;
            case "randall boggs":
            case "randall":                 return monsterImage_Randall;
            case "roz":                     return monsterImage_Roz;
            case "yeti":                    return monsterImage_Yeti;
            default:                        return null;
        }
    }

    /** Returns the side-panel portrait image for the given monster name. */
    public Image getMonsterScreenImage(String name) {
        if (name == null) return null;
        switch (name.trim().toLowerCase()) {
            case "celia mae":               return screenImage_celia_mae;
            case "fungus":                  return screenImage_Fungus;
            case "henry j. waternoose":
            case "henry j. waternoose iii": return screenImage_Henry;
            case "mike wazowski":           return screenImage_Mike;
            case "randall boggs":
            case "randall":                 return screenImage_Randall;
            case "roz":                     return screenImage_Roz;
            case "yeti":                    return screenImage_Yeti;
            case "james p. sullivan":
            case "james sullivan":          return screenImage_James;
            default:                        return getMonsterImage(name);
        }
    }

    /** Returns the card face image for the given card name. */
    public Image getCardImage(String cardName) {
        switch (cardName) {
            case "2319 Alert":         return card_2319Alert;
            case "Contamination Code": return cardContaminationCode;
            case "Mega Drain":         return cardMegaDrain;
            case "Mind Scramble":      return cardMindScramble;
            case "Position Swap":      return cardPositionSwap;
            case "Small Snatcher":     return cardSmallSnatcher;
            case "Sneaky Thief":       return cardSneakyThief;
            case "Super Shield":       return cardSuperShield;
            case "Total Confusion":    return cardTotalConfusion;
            default:                   return cardBackImage;
        }
    }

    // =========================================================
    //  INTERNAL LOADER
    // =========================================================

    /**
     * Loads an image from the classpath, caching the result so each file is
     * read from disk only once. Large background/panel images are capped at
     * 1280×920 to reduce heap usage; all other images are loaded at full size.
     * Add {@code -Xmx512m} to VM args if you still see OutOfMemoryErrors.
     */
    public Image load(String path) {
        if (IMAGE_CACHE.containsKey(path))
            return IMAGE_CACHE.get(path);

        java.io.InputStream s = getClass().getResourceAsStream(path);
        if (s == null) {
            System.err.println("WARNING: not found: " + path);
            IMAGE_CACHE.put(path, null);
            return null;
        }
        try {
            boolean isBackground = path.contains("background") || path.contains("ControlPanel")
                || path.contains("BoardHolder") || path.contains("Board.png");
            Image img = isBackground
                ? new Image(s, 1280, 920, false, true)
                : new Image(s);
            IMAGE_CACHE.put(path, img);
            return img;
        } catch (OutOfMemoryError oom) {
            System.err.println("OOM loading: " + path + " — add -Xmx512m to VM args");
            IMAGE_CACHE.put(path, null);
            return null;
        } catch (Exception e) {
            System.err.println("ERROR loading image: " + path + " — " + e.getMessage());
            IMAGE_CACHE.put(path, null);
            return null;
        }
    }
}
