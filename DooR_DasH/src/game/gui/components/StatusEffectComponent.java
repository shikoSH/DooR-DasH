package game.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusEffectComponent extends HBox {

    private Label freezeIcon;
    private Label shieldIcon;
    private Label confuseIcon;
    private Label confuseTurns;
    
    private Label focusIcon;
    private Label momentumIcon;

    public StatusEffectComponent() {
        this.setSpacing(5);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("status-effects-container");

        freezeIcon = createIcon("❄", "status-icon-freeze", "Frozen");
        shieldIcon = createIcon("🛡", "status-icon-shield", "Shielded");
        
        HBox confuseBox = new HBox(2);
        confuseBox.setAlignment(Pos.CENTER);
        confuseIcon = createIcon("🌀", "status-icon-confuse", "Confused");
        confuseTurns = new Label("");
        confuseTurns.getStyleClass().add("status-turns-label");
        confuseBox.getChildren().addAll(confuseIcon, confuseTurns);
        
        focusIcon = createIcon("🎯", "status-icon-focus", "Focus Mode");
        momentumIcon = createIcon("⚡", "status-icon-momentum", "Momentum Rush");

        this.getChildren().addAll(freezeIcon, shieldIcon, confuseBox, focusIcon, momentumIcon);
        
        hideAll();
    }

    private Label createIcon(String text, String styleClass, String tooltipText) {
        Label icon = new Label(text);
        icon.getStyleClass().addAll("status-icon", styleClass);
        // We can add tooltips here later if needed
        return icon;
    }

    private void hideAll() {
        freezeIcon.setVisible(false);
        freezeIcon.setManaged(false);
        
        shieldIcon.setVisible(false);
        shieldIcon.setManaged(false);
        
        confuseIcon.getParent().setVisible(false);
        confuseIcon.getParent().setManaged(false);
        
        focusIcon.setVisible(false);
        focusIcon.setManaged(false);
        
        momentumIcon.setVisible(false);
        momentumIcon.setManaged(false);
    }

    public void updateStatus(boolean isFrozen, boolean isShielded, int confusionTurnsRemaining, int focusTurns, int momentumTurnsRemaining) {
        freezeIcon.setVisible(isFrozen);
        freezeIcon.setManaged(isFrozen);

        shieldIcon.setVisible(isShielded);
        shieldIcon.setManaged(isShielded);

        boolean isConfused = confusionTurnsRemaining > 0;
        confuseIcon.getParent().setVisible(isConfused);
        confuseIcon.getParent().setManaged(isConfused);
        if (isConfused) {
            confuseTurns.setText(String.valueOf(confusionTurnsRemaining));
        }
        
        boolean hasFocus = focusTurns > 0;
        focusIcon.setVisible(hasFocus);
        focusIcon.setManaged(hasFocus);
        
        boolean hasMomentum = momentumTurnsRemaining > 0;
        momentumIcon.setVisible(hasMomentum);
        momentumIcon.setManaged(hasMomentum);
    }
}
