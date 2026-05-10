package game.gui.controllers;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import game.gui.components.BoardView;

public class AnimationController {

    public static void animateMovement(BoardView boardView, Rectangle token, int oldPos, int newPos, int offsetX, Runnable onFinish) {
        if (oldPos == newPos || oldPos < 0 || newPos < 0 || newPos > 99) {
            if (onFinish != null) onFinish.run();
            return;
        }

        SequentialTransition seq = new SequentialTransition();
        
        // If moving backwards due to start over, just jump or slide directly
        if (newPos < oldPos) {
            TranslateTransition jump = createSlideToCell(boardView, token, newPos, offsetX);
            seq.getChildren().add(jump);
        } else {
            // Move cell by cell
            for (int i = oldPos + 1; i <= newPos; i++) {
                TranslateTransition slide = createSlideToCell(boardView, token, i, offsetX);
                seq.getChildren().add(slide);
            }
        }

        seq.setOnFinished(e -> {
            if (onFinish != null) onFinish.run();
        });
        seq.play();
    }
    
    private static TranslateTransition createSlideToCell(BoardView boardView, Rectangle token, int cellIndex, int offsetX) {
        Node targetCell = boardView.getCellView(cellIndex);
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), token);
        
        // Wait for layout to settle if needed, but since we are mid-game, bounds are valid
        javafx.geometry.Bounds b = targetCell.getBoundsInParent();
        double targetX = b.getMinX() + (b.getWidth() / 2) - (token.getWidth() / 2) + offsetX;
        double targetY = b.getMinY() + (b.getHeight() / 2) - (token.getHeight() / 2);
        
        tt.setToX(targetX);
        tt.setToY(targetY);
        return tt;
    }
    
    public static void pulseEnergy(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setByX(0.2);
        st.setByY(0.2);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }
    
    public static void fadeOutAndIn(Node node) {
        FadeTransition ftOut = new FadeTransition(Duration.millis(200), node);
        ftOut.setFromValue(1.0);
        ftOut.setToValue(0.2);
        
        FadeTransition ftIn = new FadeTransition(Duration.millis(200), node);
        ftIn.setFromValue(0.2);
        ftIn.setToValue(1.0);
        
        SequentialTransition st = new SequentialTransition(ftOut, ftIn);
        st.play();
    }
}
