package game.gui.components;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Bounds;
import game.engine.Board;
import game.engine.cells.Cell;
import game.engine.monsters.Monster;

public class BoardView extends StackPane {
    
    private Board backendBoard;
    private CellView[] cellViews;
    private GridPane grid;
    private Pane tokenOverlay;
    
    private Rectangle playerToken;
    private Rectangle opponentToken;
    
    public BoardView(Board board) {
        this.backendBoard = board;
        this.cellViews = new CellView[100]; 
        
        grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.getStyleClass().add("board-grid");
        
        tokenOverlay = new Pane();
        tokenOverlay.setPickOnBounds(false); // Let clicks pass through
        
        playerToken = new Rectangle(20, 20, Color.web("#00ffcc"));
        playerToken.getStyleClass().add("token-player");
        
        opponentToken = new Rectangle(20, 20, Color.web("#ff006e"));
        opponentToken.getStyleClass().add("token-opponent");
        
        tokenOverlay.getChildren().addAll(playerToken, opponentToken);
        
        this.getChildren().addAll(grid, tokenOverlay);
        
        initializeBoard();
    }
    
    private void initializeBoard() {
        grid.getChildren().clear();
        
        // Use the same row/col mapping as backend indexToRowCol
        // But for visual display, we want row 0 (start) at the bottom (GridPane row 9)
        // Backend indexToRowCol:
        // row = index / 10
        // col = index % 10
        // if (row % 2 == 1) col = 9 - col
        
        for (int index = 0; index < 100; index++) {
            // Re-implementing the mapping to match visual grid
            int row = index / 10;
            int col = index % 10;
            if (row % 2 == 1) {
                col = 9 - col;
            }
            
            // Map backend row to visual row (bottom-up)
            int visualRow = 9 - row;
            int visualCol = col;
            
            // The backend Board class doesn't have an easy public getter for cell by index,
            // we have to access the 2D array.
            Cell cell = backendBoard.getBoardCells()[row][col];
            
            CellView cellView = new CellView(cell, index);
            cellViews[index] = cellView;
            
            grid.add(cellView, visualCol, visualRow);
        }
    }
    
    public void updateBoardState(Monster player, Monster opponent) {
        for (int i = 0; i < 100; i++) {
            cellViews[i].updateState();
        }
        
        // Snap tokens to their current cells
        snapTokenToCell(playerToken, player.getPosition(), -10);
        snapTokenToCell(opponentToken, opponent.getPosition(), 10);
    }
    
    public void snapTokenToCell(Rectangle token, int cellIndex, int offsetX) {
        if (cellIndex < 0 || cellIndex > 99) return;
        CellView target = cellViews[cellIndex];
        
        // Wait for layout to calculate bounds
        javafx.application.Platform.runLater(() -> {
            Bounds b = target.getBoundsInParent();
            token.setLayoutX(b.getMinX() + (b.getWidth() / 2) - (token.getWidth() / 2) + offsetX);
            token.setLayoutY(b.getMinY() + (b.getHeight() / 2) - (token.getHeight() / 2));
        });
    }
    
    public CellView getCellView(int index) {
        if (index < 0 || index > 99) return null;
        return cellViews[index];
    }
    
    public Rectangle getPlayerToken() {
        return playerToken;
    }
    
    public Rectangle getOpponentToken() {
        return opponentToken;
    }
}
