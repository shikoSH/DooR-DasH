package game.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import game.engine.cells.*;
import game.engine.monsters.Monster;

public class CellView extends StackPane {
    
    private Cell backendCell;
    private int cellIndex;
    private Rectangle background;
    private Label indexLabel;
    private VBox contentBox;
    private Label typeLabel;
    private Label detailLabel;
    
    public CellView(Cell cell, int index) {
        this.backendCell = cell;
        this.cellIndex = index;
        
        this.setPrefSize(60, 60);
        this.getStyleClass().add("cell-view");
        
        background = new Rectangle(58, 58);
        background.setArcWidth(5);
        background.setArcHeight(5);
        background.getStyleClass().add("cell-background");
        
        indexLabel = new Label(String.valueOf(index));
        indexLabel.getStyleClass().add("cell-index-label");
        StackPane.setAlignment(indexLabel, Pos.TOP_LEFT);
        
        contentBox = new VBox(2);
        contentBox.setAlignment(Pos.CENTER);
        
        typeLabel = new Label();
        typeLabel.getStyleClass().add("cell-type-label");
        typeLabel.setWrapText(true);
        typeLabel.setAlignment(Pos.CENTER);
        
        detailLabel = new Label();
        detailLabel.getStyleClass().add("cell-detail-label");
        
        contentBox.getChildren().addAll(typeLabel, detailLabel);
        
        applyTheme();
        
        this.getChildren().addAll(background, indexLabel, contentBox);
    }
    
    private void applyTheme() {
        if (backendCell instanceof DoorCell) {
            DoorCell door = (DoorCell) backendCell;
            boolean isScarer = door.getRole() == game.engine.Role.SCARER;
            background.getStyleClass().add(isScarer ? "cell-door-scarer" : "cell-door-laugher");
            
            typeLabel.setText("Door");
            detailLabel.setText(door.getEnergy() + " E");
            
            if (door.isActivated()) {
                background.getStyleClass().add("cell-door-active");
            } else {
                background.getStyleClass().add("cell-door-inactive");
            }
        } 
        else if (backendCell instanceof CardCell) {
            background.getStyleClass().add("cell-card");
            typeLabel.setText("Card");
        } 
        else if (backendCell instanceof ConveyorBelt) {
            background.getStyleClass().add("cell-conveyor");
            typeLabel.setText(">> Belt >>");
        } 
        else if (backendCell instanceof ContaminationSock) {
            background.getStyleClass().add("cell-sock");
            typeLabel.setText("! SOCK !");
        } 
        else if (backendCell instanceof MonsterCell) {
            MonsterCell mc = (MonsterCell) backendCell;
            Monster m = mc.getCellMonster();
            background.getStyleClass().add("cell-monster");
            typeLabel.setText(m.getName());
            detailLabel.setText(m.getEnergy() + "E");
            
            Tooltip tooltip = new Tooltip(m.getName() + "\nRole: " + m.getRole() + "\nEnergy: " + m.getEnergy());
            Tooltip.install(this, tooltip);
        } 
        else {
            background.getStyleClass().add("cell-normal");
        }
    }
    
    public void updateState() {
        // Specifically useful for updating Door activated state without full redraw
        if (backendCell instanceof DoorCell) {
            DoorCell door = (DoorCell) backendCell;
            background.getStyleClass().removeAll("cell-door-active", "cell-door-inactive");
            if (door.isActivated()) {
                background.getStyleClass().add("cell-door-active");
            } else {
                background.getStyleClass().add("cell-door-inactive");
            }
        }
    }
    
    }

