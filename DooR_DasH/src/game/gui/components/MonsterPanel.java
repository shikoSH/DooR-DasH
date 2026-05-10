package game.gui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import game.engine.monsters.Monster;
import game.engine.monsters.Dasher;
import game.engine.monsters.MultiTasker;

public class MonsterPanel extends VBox {
    
    private Label nameLabel;
    private Label roleLabel;
    private Label originalRoleLabel;
    private Label typeLabel;
    private Label energyLabel;
    private ProgressBar energyBar;
    
    private StatusEffectComponent statusEffects;
    private Label positionLabel;

    public MonsterPanel(String title) {
        this.getStyleClass().add("player-panel");
        this.setSpacing(10);
        this.setAlignment(Pos.TOP_CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("panel-title");
        
        // Placeholder for sprite
        Rectangle spritePlaceholder = new Rectangle(120, 120, Color.web("#222"));
        spritePlaceholder.setStroke(Color.web("#5a189a"));
        spritePlaceholder.setStrokeWidth(2);
        
        nameLabel = new Label("Name: ???");
        nameLabel.getStyleClass().add("instruction-text");
        
        roleLabel = new Label("Role: ???");
        roleLabel.getStyleClass().add("instruction-text");
        
        originalRoleLabel = new Label("");
        originalRoleLabel.setStyle("-fx-text-fill: #ff006e; -fx-font-size: 14px;");
        
        typeLabel = new Label("Type: ???");
        typeLabel.getStyleClass().add("instruction-text");
        
        VBox energyBox = new VBox(5);
        energyBox.setAlignment(Pos.CENTER);
        energyLabel = new Label("Energy: 0");
        energyLabel.getStyleClass().add("instruction-text");
        
        energyBar = new ProgressBar(0);
        energyBar.setPrefWidth(200);
        energyBar.getStyleClass().add("energy-bar");
        energyBox.getChildren().addAll(energyLabel, energyBar);
        
        statusEffects = new StatusEffectComponent();
        
        positionLabel = new Label("Position: 0");
        positionLabel.getStyleClass().add("instruction-text");
        
        this.getChildren().addAll(
            titleLabel, 
            spritePlaceholder, 
            nameLabel, 
            typeLabel,
            roleLabel, 
            originalRoleLabel,
            energyBox, 
            positionLabel,
            statusEffects
        );
    }
    
    public void updateMonster(Monster monster) {
        if (monster == null) return;
        
        nameLabel.setText("Name: " + monster.getName());
        
        // Use class name for type (e.g. "Sullivan", "Wazowski" ... actually we don't have explicit subtypes in the base code except subclass names)
        String typeName = monster.getClass().getSimpleName();
        typeLabel.setText("Type: " + typeName);
        
        roleLabel.setText("Role: " + monster.getRole());
        
        if (monster.isConfused()) {
            originalRoleLabel.setText("(Originally: " + monster.getOriginalRole() + ")");
        } else {
            originalRoleLabel.setText("");
        }
        
        energyLabel.setText("Energy: " + monster.getEnergy());
        // Assuming max energy is around 300 for the bar scaling
        double progress = Math.min(1.0, Math.max(0.0, monster.getEnergy() / 300.0));
        energyBar.setProgress(progress);
        
        positionLabel.setText("Position: " + monster.getPosition());
        
        int focusTurns = 0;
        int momentumTurns = 0;
        
        if (monster instanceof MultiTasker) {
            focusTurns = ((MultiTasker) monster).getNormalSpeedTurns();
        } else if (monster instanceof Dasher) {
            momentumTurns = ((Dasher) monster).getMomentumTurns();
        }
        
        statusEffects.updateStatus(monster.isFrozen(), monster.isShielded(), monster.getConfusionTurns(), focusTurns, momentumTurns);
    }
    
    public void setActiveTurn(boolean isActive) {
        if (isActive) {
            this.setStyle("-fx-border-color: #00ffcc; -fx-border-width: 2px; -fx-effect: dropshadow(gaussian, #00ffcc, 15, 0.5, 0, 0);");
        } else {
            this.setStyle("-fx-border-color: #5a189a; -fx-border-width: 1px; -fx-effect: none;");
        }
    }
}
