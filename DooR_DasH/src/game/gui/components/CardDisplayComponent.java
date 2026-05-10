package game.gui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import game.engine.cards.Card;

public class CardDisplayComponent extends StackPane {

    private Rectangle cardBg;
    private Label titleLabel;
    private Label descLabel;
    private Label typeLabel;

    public CardDisplayComponent() {
        this.getStyleClass().add("card-display");
        
        cardBg = new Rectangle(200, 300);
        cardBg.setArcWidth(15);
        cardBg.setArcHeight(15);
        cardBg.getStyleClass().add("card-background");
        
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        
        titleLabel = new Label();
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        
        typeLabel = new Label();
        typeLabel.getStyleClass().add("card-type");
        
        descLabel = new Label();
        descLabel.getStyleClass().add("card-desc");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        
        content.getChildren().addAll(titleLabel, typeLabel, descLabel);
        
        this.getChildren().addAll(cardBg, content);
        
        // Hidden by default
        this.setVisible(false);
        this.setManaged(false);
    }
    
    public void showCard(Card card) {
        if (card == null) return;
        
        titleLabel.setText(card.getName().toUpperCase());
        descLabel.setText(card.getDescription());
        
        if (card.isLucky()) {
            typeLabel.setText("LUCKY");
            cardBg.setStyle("-fx-fill: linear-gradient(to bottom, #00ffcc, #118ab2); -fx-stroke: #00ffcc; -fx-stroke-width: 3;");
        } else {
            typeLabel.setText("UNLUCKY");
            cardBg.setStyle("-fx-fill: linear-gradient(to bottom, #ff006e, #c1121f); -fx-stroke: #ff006e; -fx-stroke-width: 3;");
        }
        
        this.setVisible(true);
        this.setManaged(true);
        AnimationController.fadeOutAndIn(this);
    }
    
    public void hideCard() {
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), this);
        st.setToX(0);
        st.setToY(0);
        st.setOnFinished(e -> {
            this.setVisible(false);
            this.setManaged(false);
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });
        st.play();
    }
}
