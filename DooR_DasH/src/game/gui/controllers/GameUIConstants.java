package game.gui.controllers;

/**
 * GameUIConstants
 * ---------------
 * Centralises every magic number and image-path string used by the game GUI.
 * All other controller helpers read from here so changes (font, layout ratios,
 * asset filenames) need only be made in one place.
 *
 * Owner: UI / Assets team member
 */
public final class GameUIConstants {

    private GameUIConstants() {}   // utility class — no instances

    // =========================================================
    //  FONT
    // =========================================================
    public static final String FONT      = "ARCADECLASSIC";
    public static final String FONT_PATH = "/game/gui/resources/fonts/ARCADECLASSIC.TTF";

    // =========================================================
    //  TEXT SIZES  (px)
    // =========================================================
    public static final int TXT_PLAYER_NAME   = 20;
    public static final int TXT_PLAYER_TYPE   = 18;
    public static final int TXT_PLAYER_ROLE   = 18;
    public static final int TXT_PLAYER_POS    = 20;
    public static final int TXT_PLAYER_ENERGY = 20;
    public static final int TXT_PLAYER_STATUS = 20;
    public static final int TXT_PLAYER_TURN   = 15;
    public static final int TXT_ACTION_LOG    = 20;
    public static final int TXT_TOP_LABEL     = 14;
    public static final int TXT_DICE_RESULT   = 20;
    public static final int TXT_CARD_NAME     = 20;
    public static final int TXT_CARD_BODY     = 16;
    public static final int TXT_CELL_INDEX    =  7;
    public static final int TXT_DOOR_ENERGY   =  7;

    // =========================================================
    //  LAYOUT MULTIPLIERS  (fractions of scene width / height)
    // =========================================================
    public static final double BOARD_SIZE_MULT   = 0.70;
    public static final double SIDE_PANEL_W      = 0.155;
    public static final double PORTRAIT_W_MULT   = 0.88;
    public static final double ENERGY_BAR_W_MULT = 0.2;
    public static final double PANEL_TOP_PAD     = 0.08;
    public static final double LIGHT_SIZE_MULT   = 0.16;
    public static final double PROFILE_W_MULT    = 0.90;
    public static final double ACTION_LOG_W_MULT = 0.90;
    public static final double CONTROL_BAR_H     = 0.16;
    public static final double DECK_LEFT         = 0.25;
    public static final double DECK_TOP_FRAC     = -1.8;
    public static final double DECK_W            = 0.1584;
    public static final double DECK_H            = 0.3456;
    public static final double DICE_SIZE         = 0.25;
    public static final double DICE_TOP_FRAC     = -1.6;
    public static final double BTN_W             = 0.17;
    public static final double BTN_H             = 0.17;
    public static final double BTN_TOP_FRAC      = -1.4;
    public static final double ROLL_RIGHT        = 0.185;
    public static final double POWERUP_RIGHT     = 0.284;
    public static final double PARALLAX_X        = 28;
    public static final double PARALLAX_Y        = 18;
    public static final double BG_OVERSIZE       = 1.08;
    public static final double CARD_W_MULT       = 0.38;
    public static final double CARD_H_MULT       = 0.50;

    // =========================================================
    //  IMAGE ROOT PATH
    // =========================================================
    public static final String IMG = "/game/gui/resources/images/";

    // ── Cell / board images ───────────────────────────────────
    public static final String IMG_NORMAL       = "NormalCell.png";
    public static final String IMG_DOOR_SC      = "Scarer_ClosedDoor_Cell2.png";
    public static final String IMG_DOOR_LC      = "Laugher_ClosedDoor_Cell.png";
    public static final String IMG_DOOR_SO      = "Scarer_OpenDoor_Cell.png";
    public static final String IMG_DOOR_LO      = "Laugher_OpenDoor_Cell.png";
    public static final String IMG_MONSTER_CELL = "MonsterCell_Grey.png";
    public static final String IMG_CONVEYOR     = "Conveyor_belt_cell.png";
    public static final String IMG_SOCK         = "contamination_sock_cell.png";
    public static final String IMG_CARD_CELL    = "CardCell.png";

    // ── Scene / chrome images ─────────────────────────────────
    public static final String IMG_BACKGROUND   = "background_Game.png";
    public static final String IMG_CONTROL      = "ControlPanel.png";
    public static final String IMG_BOARD_HOLDER = "Board_Holder.png";
    public static final String IMG_BOARD        = "images.png";
    public static final String IMG_POWERUP_BTN  = "Power_Up_Button.png";
    public static final String IMG_ROLL_BTN     = "Roll_Button.png";

    // ── Card-deck images ──────────────────────────────────────
    public static final String IMG_DECK_FULL    = "CardsDeck_Full.png";
    public static final String IMG_DECK_MID     = "CardsDeck_Mid.png";
    public static final String IMG_DECK_LEAST   = "CardsDeck_Least.png";

    // ── Status-light images ───────────────────────────────────
    public static final String IMG_TURN_OFF     = "turn_off.png";
    public static final String IMG_TURN_ON      = "turn_on.png";
    public static final String IMG_CONF_OFF     = "confusion_off.png";
    public static final String IMG_CONF_ON      = "confusion_on.png";
    public static final String IMG_FREEZE_OFF   = "freezed_off.png";
    public static final String IMG_FREEZE_ON    = "freezed_on.png";
    public static final String IMG_SHIELD_OFF   = "shield_off.png";
    public static final String IMG_SHIELD_ON    = "shield_on.png";
    public static final String IMG_POWER_OFF    = "powerup_off.png";
    public static final String IMG_POWER_ON     = "powerup_on.png";

    // ── Panel decoration images ───────────────────────────────
    public static final String IMG_PROFILE      = "monster_profile.png";
    public static final String IMG_ACTION_LOG   = "Action_Log.png";
    public static final String IMG_CARD_BACK    = "card_back_design.jpg";
}
