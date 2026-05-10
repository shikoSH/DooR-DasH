package game.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import game.engine.cells.*;
import game.engine.monsters.Monster;

public class BoardCellComponent extends StackPane {
    
    private Cell backendCell;
    private int index;
    private Rectangle background;
    private Label indexLabel;
    private Label cellTypeLabel;
    private StackPane monsterLayer;
    
    public BoardCellComponent(Cell cell, int index) {
        this.backendCell = cell;
        this.index = index;
        
        this.setPrefSize(50, 50);
        this.getStyleClass().add("board-cell");
        
        background = new Rectangle(50, 50);
        background.setStroke(Color.DARKGRAY);
        
        indexLabel = new Label(String.valueOf(index + 1)); // 1-indexed for display
        indexLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        indexLabel.setTextFill(Color.WHITE);
        StackPane.setAlignment(indexLabel, Pos.TOP_LEFT);
        
        cellTypeLabel = new Label();
        cellTypeLabel.setFont(Font.font("Arial", 8));
        cellTypeLabel.setTextFill(Color.LIGHTGRAY);
        cellTypeLabel.setWrapText(true);
        cellTypeLabel.setAlignment(Pos.CENTER);
        
        monsterLayer = new StackPane();
        
        determineStyle();
        
        this.getChildren().addAll(background, indexLabel, cellTypeLabel, monsterLayer);
    }
    
    private void determineStyle() {
        if (backendCell instanceof DoorCell) {
            background.setFill(Color.web("#3c096c")); // Dark Purple
            cellTypeLabel.setText("Door");
        } else if (backendCell instanceof CardCell) {
            background.setFill(Color.web("#00509d")); // Blue
            cellTypeLabel.setText("Card");
        } else if (backendCell instanceof ConveyorBelt) {
            background.setFill(Color.web("#2b2b2b")); // Dark gray
            cellTypeLabel.setText("Belt");
        } else if (backendCell instanceof ContaminationSock) {
            background.setFill(Color.web("#c1121f")); // Red
            cellTypeLabel.setText("Sock");
        } else if (backendCell instanceof MonsterCell) {
            background.setFill(Color.web("#5a189a")); // Purple
            cellTypeLabel.setText("Monster");
        } else {
            background.setFill(Color.web("#1a1a2e")); // Normal
            cellTypeLabel.setText("");
        }
    }
    
    public void updateMonster(Monster player, Monster opponent) {
        monsterLayer.getChildren().clear();
        
        boolean hasPlayer = player.getPosition() == index;
        boolean hasOpponent = opponent.getPosition() == index;
        
        if (hasPlayer) {
            Rectangle pToken = new Rectangle(15, 15, Color.web("#00ffcc")); // Neon Cyan
            StackPane.setAlignment(pToken, Pos.CENTER_LEFT);
            monsterLayer.getChildren().add(pToken);
        }
        
        if (hasOpponent) {
            Rectangle oToken = new Rectangle(15, 15, Color.web("#ff006e")); // Neon Pink
            StackPane.setAlignment(oToken, Pos.CENTER_RIGHT);
            monsterLayer.getChildren().add(oToken);
        }
    }
}
