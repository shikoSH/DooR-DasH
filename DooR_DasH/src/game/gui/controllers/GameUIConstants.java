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
    public static final String FONT      = "ArcadeClassic";
    public static final String FONT_PATH =
            "/game/gui/resources/fonts/ARCADECLASSIC.TTF";

    // =========================================================
    //  TEXT SIZES  (px)
    // =========================================================
    public static final int TXT_PLAYER_NAME   = 12;
    public static final int TXT_PLAYER_TYPE   = 11;
    public static final int TXT_PLAYER_ROLE   = 11;
    public static final int TXT_PLAYER_POS    = 10;
    public static final int TXT_PLAYER_ENERGY = 20;
    public static final int TXT_PLAYER_STATUS = 10;
    public static final int TXT_PLAYER_TURN   = 15;
    public static final int TXT_ACTION_LOG    = 3;
    public static final int TXT_TOP_LABEL     = 14;
    public static final int TXT_DICE_RESULT   = 20;
    public static final int TXT_CARD_NAME     = 20;
    public static final int TXT_CARD_BODY     = 16;
    public static final int TXT_CELL_INDEX    =  7;
    public static final int TXT_DOOR_ENERGY   =  7;

    // =========================================================
    //  LAYOUT MULTIPLIERS  (fractions of scene width / height)
    // =========================================================
    public static final double BOARD_SIZE_MULT   = 0.7;
    // Measured directly from ControlPanel.png: the printed side panel
    // occupies 18.56% of the image width on the left, 17.33% on the
    // right (verified consistent at 5 different heights). The old value
    // of 0.108 made the dynamic-content column only ~60% as wide as the
    // actual printed panel — since content is centered within that too-
    // narrow column while the column itself is pinned flush to the
    // outer window edge, its visual center sat closer to the border
    // than the printed panel's true center. That's what caused the
    // portrait/profile card to look shifted toward the outer edges.
    public static final double SIDE_PANEL_W      = 0.17;
    public static final double PORTRAIT_W_MULT   = 0.7;
    public static final double ENERGY_BAR_W_MULT = 0.22;
    public static final double PANEL_TOP_PAD     = 0.04;
    public static final double LIGHT_SIZE_MULT   = 0.14;

    // Moves ONLY the status-lights row within each side panel, leaving
    // the portrait/profile/energy bar/status text below it untouched.
    // Positive X = right, positive Y = down. 0/0 = default position.
    public static final double REF_LIGHTS_OFFSET_X = 20;
    public static final double REF_LIGHTS_OFFSET_Y = 0;

    // =========================================================
    //  PER-ELEMENT MOVE OFFSETS — GAME SCREEN
    //  ---------------------------------------------------------
    //  Every single UI element on the game screen can be nudged
    //  independently of everything else, without disturbing anything
    //  else's position. Change any X/Y pair below and rebuild — that's
    //  the whole workflow. All units are reference pixels (1280x720
    //  scale, same as everywhere else); positive X = right, positive
    //  Y = down. 0/0 = default (no change from the normal layout).
    //
    //  These are applied as translateX/translateY, which is a purely
    //  visual offset — it never affects layout calculations, so moving
    //  one element can't push its neighbors around.
    // =========================================================

    // ── Top-level pieces (each is its own independently anchored box) ──
    public static final double OFFSET_X_TOP_LABEL       = 0, OFFSET_Y_TOP_LABEL       = 0; // turn status text
    public static final double OFFSET_X_PLAYER_PANEL    = 0, OFFSET_Y_PLAYER_PANEL    = 0; // whole left panel
    public static final double OFFSET_X_OPPONENT_PANEL  = 0, OFFSET_Y_OPPONENT_PANEL  = 0; // whole right panel
    public static final double OFFSET_X_ACTION_LOG      = 0, OFFSET_Y_ACTION_LOG      = 25; // whole action log block
    public static final double OFFSET_X_BOARD           = 0, OFFSET_Y_BOARD           = -60; // whole board
    public static final double OFFSET_X_CONTROL_BAR     = 0, OFFSET_Y_CONTROL_BAR     = 0; // whole bottom bar
    public static final double OFFSET_X_CARD_DECK       = 0, OFFSET_Y_CARD_DECK       = 0; // deck image
    public static final double OFFSET_X_DICE            = 0, OFFSET_Y_DICE            = 0; // dice image
    public static final double OFFSET_X_POWERUP_BTN     = 0, OFFSET_Y_POWERUP_BTN     = 0;
    public static final double OFFSET_X_ROLL_BTN        = 0, OFFSET_Y_ROLL_BTN        = 0;

    // ── Inside each side panel (SAME offset applies to player + opponent,
    //     so both panels stay visually mirrored) ────────────────────────
    // Lights row already has its own dedicated pair above:
    //   REF_LIGHTS_OFFSET_X / REF_LIGHTS_OFFSET_Y
    public static final double OFFSET_X_PORTRAIT   = 10, OFFSET_Y_PORTRAIT   = 0; // portrait + position badge
    public static final double OFFSET_X_POS_LABEL_PLAYER   = 50, OFFSET_Y_POS_LABEL_PLAYER   = -97; // player's position number text
    public static final double OFFSET_X_POS_LABEL_OPPONENT = 50, OFFSET_Y_POS_LABEL_OPPONENT = -97; // opponent's position number text
    public static final double OFFSET_X_PROFILE    = 10, OFFSET_Y_PROFILE   = 0; // profile card image + its text
    public static final double OFFSET_X_NAME_LBL   = 32, OFFSET_Y_NAME_LBL  = 32; // monster name text alone
    public static final double OFFSET_X_TYPE_LBL   = 0, OFFSET_Y_TYPE_LBL  = 15; // "CLASS" value text alone
    public static final double OFFSET_X_ROLE_LBL   = 0, OFFSET_Y_ROLE_LBL  = 1; // "FACTION" value text alone
    public static final double OFFSET_X_ENERGY_NUM = 20, OFFSET_Y_ENERGY_NUM = 0; // energy number label
    public static final double OFFSET_X_ENERGY_BAR_PLAYER   = 75, OFFSET_Y_ENERGY_BAR_PLAYER   = -20; // player's energy canister image
    public static final double OFFSET_X_ENERGY_BAR_OPPONENT = 75, OFFSET_Y_ENERGY_BAR_OPPONENT = -20; // opponent's energy canister image
    public static final double OFFSET_X_STATUS_LBL = 30, OFFSET_Y_STATUS_LBL = -8; // status text
    public static final double OFFSET_X_TURN_LBL   = 0, OFFSET_Y_TURN_LBL   = 0; // "YOUR TURN" label (player only)
    public static final double PROFILE_W_MULT    = 0.7;
    public static final double ACTION_LOG_W_MULT = 1;   // was 0.90 — bump up/down to resize the action log
    public static final double CONTROL_BAR_H     = 0.1;
    public static final double DECK_LEFT         = 0.25;
    public static final double DECK_TOP_FRAC     = -1.8;
    public static final double DECK_W            = 0.1584;
    public static final double DECK_H            = 0.3456;
    public static final double DICE_SIZE         = 0.25;
    public static final double DICE_TOP_FRAC     = -1.6;
    public static final double BTN_W             = 0.17;
    public static final double BTN_H             = 0.17;
    public static final double BTN_TOP_FRAC      = -1.17;
    public static final double ROLL_RIGHT        = 0.185;
    public static final double POWERUP_RIGHT     = 0.284;
    public static final double PARALLAX_X        = 28;
    public static final double PARALLAX_Y        = 18;
    public static final double BG_OVERSIZE       = 1.08;
    public static final double CARD_W_MULT       = 0.38;
    public static final double CARD_H_MULT       = 0.50;

    // =========================================================
    //  ACTION LOG — TEXT OVERLAY
    // =========================================================
    // All expressed as fractions of the *rendered* Action_Log.png size
    // (recomputed every layout pass), so the three log lines always sit
    // inside the image's screen area and scale together with it instead of
    // drifting at different window sizes like fixed-pixel padding would.
    // Nudge these to match your artwork once you see it rendered.
    public static final double ACTION_LOG_TOP_PAD_FRAC    = 0.11;
    public static final double ACTION_LOG_LEFT_PAD_FRAC   = 0.2;
    public static final double ACTION_LOG_RIGHT_PAD_FRAC  = 0.05;
    public static final double ACTION_LOG_BOTTOM_PAD_FRAC = 0.05;
    public static final double ACTION_LOG_FONT_FRAC       = 0.055;

    public static final String ACTION_LOG_LINE1_COLOR = "white";
    public static final String ACTION_LOG_LINE2_COLOR = "#aaffaa";
    public static final String ACTION_LOG_LINE3_COLOR = "#aaaaff";

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
    public static final String IMG_POWERUP_BTN_HALF    = "halfpressed_powerup_button.png";
    public static final String IMG_POWERUP_BTN_PRESSED = "pressed_powerup_button.png";
    public static final String IMG_ROLL_BTN_HALF        = "halfpressed_roll_button.png";
    public static final String IMG_ROLL_BTN_PRESSED     = "pressed_roll_button.png";

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
    public static final String IMG_CARD_BACK    = "card_back_design.png";

    // =========================================================
    //  TEXT-SIZE FRACTIONS (relative to side-panel / board width)
    //  Fixes text not scaling across different screen resolutions.
    //  The TXT_* fields above stayed literal pixel values that never
    //  rescaled with the window — fine on the PC they were tuned on,
    //  broken on any other resolution or monitor. These fractions are
    //  derived from those same pixel values at the design reference
    //  size (1280x720), so the look is unchanged there, but now the
    //  text scales proportionally everywhere else.
    // =========================================================
    public static final double REFERENCE_PANEL_W = 1280 * SIDE_PANEL_W;    // 138.24
    public static final double REFERENCE_BOARD_W = 720  * BOARD_SIZE_MULT; // 604.8

    public static final double TXT_PLAYER_NAME_FRAC       = TXT_PLAYER_NAME   / REFERENCE_PANEL_W;
    public static final double TXT_PLAYER_TYPE_FRAC       = TXT_PLAYER_TYPE   / REFERENCE_PANEL_W;
    public static final double TXT_PLAYER_ROLE_FRAC       = TXT_PLAYER_ROLE   / REFERENCE_PANEL_W;
    public static final double TXT_PLAYER_POS_FRAC        = TXT_PLAYER_POS    / REFERENCE_PANEL_W;
    public static final double TXT_PLAYER_ENERGY_FRAC     = TXT_PLAYER_ENERGY / REFERENCE_PANEL_W;
    public static final double TXT_PLAYER_STATUS_FRAC     = TXT_PLAYER_STATUS / REFERENCE_PANEL_W;
    public static final double TXT_PLAYER_TURN_FRAC       = TXT_PLAYER_TURN   / REFERENCE_PANEL_W;

    public static final double TXT_CARD_NAME_FRAC = TXT_CARD_NAME / REFERENCE_BOARD_W;
    public static final double TXT_CARD_BODY_FRAC = TXT_CARD_BODY / REFERENCE_BOARD_W;

    // =========================================================
    //  UNIFORM-SCALE LAYOUT  (fixes aspect-ratio distortion)
    //  ---------------------------------------------------------
    //  The old layout sized things as INDEPENDENT fractions of the
    //  window's width and height (e.g. BTN_W = W*0.17, BTN_H = H*0.17).
    //  Since W and H change at different rates once the window's aspect
    //  ratio departs from the art's native ratio, W*0.17 and H*0.17
    //  stop being proportional to each other — a button meant to render
    //  as a square instead rendered as a rectangle, and the printed
    //  control-panel graphic (stretched independently on each axis) no
    //  longer lined up with the dice/cards/buttons sitting on top of it.
    //
    //  Fix: treat the whole HUD (board + side panels + control bar) as
    //  ONE fixed 1280x720 composition. Compute a SINGLE scale factor and
    //  multiply every reference-pixel value below by that SAME number on
    //  both axes, then center the whole composition in the window (any
    //  leftover space becomes symmetric letterbox/pillarbox bars instead
    //  of stretching anything). Every square stays a square and every
    //  alignment stays locked, on any resolution or aspect ratio.
    // =========================================================
    public static final double REF_W = 1280;
    public static final double REF_H = 720;

    public static final double REF_BOARD_SIZE    = REF_H * BOARD_SIZE_MULT;   // 604.8
    public static final double REF_BAR_H         = REF_H * CONTROL_BAR_H;     // 115.2
    public static final double REF_PANEL_W       = REF_W * SIDE_PANEL_W;      // 138.24
    public static final double REF_PANEL_TOP_PAD = REF_H * PANEL_TOP_PAD;     // 28.8

    public static final double REF_DECK_LEFT = REF_W * DECK_LEFT;             // 320.0
    public static final double REF_DECK_W    = REF_W * DECK_W;                // 202.75
    public static final double REF_DECK_H    = REF_H * DECK_H;                // 248.83
    public static final double REF_DECK_TOP  = REF_BAR_H * DECK_TOP_FRAC;     // -207.36

    public static final double REF_DICE_SIZE = REF_H * DICE_SIZE;             // 180.0
    public static final double REF_DICE_TOP  = REF_BAR_H * DICE_TOP_FRAC;     // -184.32

    public static final double REF_BTN_W           = REF_W * BTN_W;           // 217.6
    public static final double REF_BTN_H           = REF_H * BTN_H;           // 122.4
    public static final double REF_BTN_TOP         = REF_BAR_H * BTN_TOP_FRAC;// -161.28
    public static final double REF_ROLL_RIGHT      = REF_W * ROLL_RIGHT;      // 236.8
    public static final double REF_POWERUP_RIGHT   = REF_W * POWERUP_RIGHT;   // 363.52

    // =========================================================
    //  OUTER ANCHOR-PANE LAYOUT
    //  ---------------------------------------------------------
    //  Previously masterLayout was a BorderPane: the center region
    //  (the board) auto-sized to "whatever's left" after the top/left/
    //  right/bottom regions claimed their space. If a side-panel VBox's
    //  measured content changed size even slightly (e.g. font metrics
    //  rendering a hair different on another machine), the BorderPane
    //  could quietly re-negotiate the center region's box, nudging the
    //  board. AnchorPane removes that: every child below gets a literal
    //  numeric position/size derived only from `scale`, never from a
    //  sibling's rendered content.
    // =========================================================

    // Height reserved for the top status label row.
    public static final double REF_TOP_LABEL_H = 30;

    // Approximate height of the opponent panel's own stacked content
    // (lights + portrait + profile card + energy label + energy bar).
    // The action log sits directly below this. Nudge this value if the
    // action log doesn't line up under the opponent panel on your
    // artwork.
    public static final double REF_OPPONENT_PANEL_H = 380;
    public static final double REF_ACTION_LOG_GAP   = 8;
}