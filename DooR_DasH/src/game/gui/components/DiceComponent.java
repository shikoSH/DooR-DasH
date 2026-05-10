package game.gui.components;

import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class DiceComponent extends StackPane {

    private Rectangle diceShape;
    private Label diceLabel;

    public DiceComponent() {
        this.setAlignment(Pos.CENTER);
        
        diceShape = new Rectangle(60, 60);
        diceShape.setArcWidth(15);
        diceShape.setArcHeight(15);
        diceShape.getStyleClass().add("dice-shape");
        
        diceLabel = new Label("?");
        diceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        diceLabel.setTextFill(Color.WHITE);
        
        this.getChildren().addAll(diceShape, diceLabel);
    }

    public void animateRoll(Runnable onFinished) {
        diceLabel.setText("?");
        
        RotateTransition rt1 = new RotateTransition(Duration.millis(150), this);
        rt1.setByAngle(180);
        
        RotateTransition rt2 = new RotateTransition(Duration.millis(150), this);
        rt2.setByAngle(180);
        
        SequentialTransition seq = new SequentialTransition(rt1, rt2);
        seq.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        seq.play();
    }

    public void setResult(String result) {
        diceLabel.setText(result);
    }
    
    public void disable(boolean disable) {
        this.setDisable(disable);
        if (disable) {
            diceShape.setOpacity(0.5);
        } else {
            diceShape.setOpacity(1.0);
        }
    }
}
