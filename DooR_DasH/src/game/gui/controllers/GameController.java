package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import game.engine.Game;
import game.engine.Role;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;
import game.gui.components.DiceComponent;
import game.engine.monsters.Monster;
import game.gui.components.BoardView;
import game.gui.components.CardDisplayComponent;
import game.engine.Board;
import game.engine.cards.Card;
import javafx.scene.shape.Rectangle;

public class GameController {

    @FXML
    private StackPane boardContainer;
    
    @FXML
    private javafx.scene.layout.VBox playerPanelContainer;
    
    @FXML
    private javafx.scene.layout.VBox opponentPanelContainer;
    
    @FXML
    private javafx.scene.layout.VBox actionLogContainer;
    
    @FXML
    private javafx.scene.layout.VBox diceContainer;

    private Game game;
    private game.gui.components.BoardView boardView;
    private game.gui.components.MonsterPanel playerPanel;
    private game.gui.components.MonsterPanel opponentPanel;
    private game.gui.components.ActionLogComponent actionLog;
    private DiceComponent dice;
    private CardDisplayComponent cardDisplay;
    private boolean isAnimating = false;

    @FXML
    private void initialize() {
        // Will be called when FXML is loaded
    }

    public void startGame(Role playerRole) {
        try {
            this.game = new Game(playerRole);
            initializeBoardUI();
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeBoardUI() {
        boardContainer.getChildren().clear();
        boardView = new BoardView(game.getBoard());
        boardContainer.getChildren().add(boardView);
        
        playerPanel = new game.gui.components.MonsterPanel("PLAYER 1");
        opponentPanel = new game.gui.components.MonsterPanel("OPPONENT");
        
        playerPanelContainer.getChildren().clear();
        playerPanelContainer.getChildren().add(playerPanel);
        
        opponentPanelContainer.getChildren().clear();
        opponentPanelContainer.getChildren().add(opponentPanel);
        
        actionLog = new game.gui.components.ActionLogComponent();
        actionLogContainer.getChildren().add(actionLog);
        actionLog.log("Game initialized. " + game.getPlayer().getName() + " vs " + game.getOpponent().getName());
        
        dice = new DiceComponent();
        diceContainer.getChildren().add(dice);
        
        cardDisplay = new CardDisplayComponent();
        boardContainer.getChildren().add(cardDisplay);
        
        // Hide card display on click
        cardDisplay.setOnMouseClicked(e -> cardDisplay.hideCard());
    }

    private void updateUI() {
        if (boardView != null && game != null) {
            boardView.updateBoardState(game.getPlayer(), game.getOpponent());
            playerPanel.updateMonster(game.getPlayer());
            opponentPanel.updateMonster(game.getOpponent());
            
            boolean isPlayerTurn = game.getCurrent() == game.getPlayer();
            playerPanel.setActiveTurn(isPlayerTurn);
            opponentPanel.setActiveTurn(!isPlayerTurn);
        }
    }

    @FXML
    private void handleRollDice() {
        if (isAnimating) return;
        
        try {
            int oldPos = game.getCurrent().getPosition();
            int oldOpponentPos = game.getCurrent() == game.getPlayer() ? game.getOpponent().getPosition() : game.getPlayer().getPosition();
            int oldCurrentEnergy = game.getCurrent().getEnergy();
            int oldOpponentEnergy = game.getCurrent() == game.getPlayer() ? game.getOpponent().getEnergy() : game.getPlayer().getEnergy();
            
            Monster currentPlayer = game.getCurrent();
            Monster opponentPlayer = game.getCurrent() == game.getPlayer() ? game.getOpponent() : game.getPlayer();
            
            // Peek at top card to see if it gets drawn
            if (Board.cards.isEmpty()) {
                Board.reloadCards();
            }
            Card topCard = Board.cards.isEmpty() ? null : Board.cards.get(0);
            
            // Execute backend logic first
            game.playTurn();
            
            boolean cardWasDrawn = topCard != null && (Board.cards.isEmpty() || Board.cards.get(0) != topCard);
            
            // We deduce the roll for the log, but display it nicely after animation
            int newPos = currentPlayer.getPosition();
            int newOpponentPos = opponentPlayer.getPosition();
            int newCurrentEnergy = currentPlayer.getEnergy();
            int newOpponentEnergy = opponentPlayer.getEnergy();
            
            int moved = newPos - oldPos;
            if (moved < 0) moved += 100; // Wrapped around
            final int displayMove = moved;
            
            isAnimating = true;
            dice.disable(true);
            
            dice.animateRoll(() -> {
                dice.setResult(String.valueOf(displayMove)); 
                actionLog.logEvent(currentPlayer.getName() + " moved " + displayMove + " spaces.");
                
                if (cardWasDrawn) {
                    actionLog.logEvent(currentPlayer.getName() + " drew a card: " + topCard.getName());
                    cardDisplay.showCard(topCard);
                }
                
                // Track board effects
                if (newPos == oldOpponentPos && newOpponentPos == oldPos && oldPos != oldOpponentPos) {
                    actionLog.logEvent("SWAP! Players swapped positions!");
                } else if (newPos == 0 && oldPos != 0 && displayMove != 0) {
                    actionLog.logError("START OVER! " + currentPlayer.getName() + " was sent back to start!");
                }
                
                if (newCurrentEnergy != oldCurrentEnergy) {
                    int diff = newCurrentEnergy - oldCurrentEnergy;
                    actionLog.logEvent(currentPlayer.getName() + " energy " + (diff > 0 ? "+" : "") + diff);
                    AnimationController.pulseEnergy(playerPanelContainer);
                }
                if (newOpponentEnergy != oldOpponentEnergy) {
                    int diff = newOpponentEnergy - oldOpponentEnergy;
                    actionLog.logEvent(opponentPlayer.getName() + " energy " + (diff > 0 ? "+" : "") + diff);
                    AnimationController.pulseEnergy(opponentPanelContainer);
                }
                
                Rectangle activeToken = (currentPlayer == game.getPlayer()) ? boardView.getPlayerToken() : boardView.getOpponentToken();
                int offsetX = (currentPlayer == game.getPlayer()) ? -10 : 10;
                
                AnimationController.animateMovement(boardView, activeToken, oldPos, newPos, offsetX, () -> {
                    // Check if opponent moved too (swapper)
                    if (newOpponentPos != oldOpponentPos) {
                        Rectangle oppToken = (opponentPlayer == game.getPlayer()) ? boardView.getPlayerToken() : boardView.getOpponentToken();
                        int oppOffsetX = (opponentPlayer == game.getPlayer()) ? -10 : 10;
                        AnimationController.animateMovement(boardView, oppToken, oldOpponentPos, newOpponentPos, oppOffsetX, () -> {
                            finishTurn(cardWasDrawn, topCard);
                        });
                    } else {
                        finishTurn(cardWasDrawn, topCard);
                    }
                });
            });
            
        } catch (Exception e) {
            showErrorAlert("Roll Error", e.getMessage());
            actionLog.logError("Error: " + e.getMessage());
            isAnimating = false;
            dice.disable(false);
        }
    }
    
    private void finishTurn(boolean cardWasDrawn, Card topCard) {
        if (cardWasDrawn) {
            Monster previousPlayer = game.getCurrent() == game.getPlayer() ? game.getOpponent() : game.getPlayer();
            actionLog.logEvent(previousPlayer.getName() + " drew a card: " + topCard.getName());
            cardDisplay.showCard(topCard);
        }
        
        updateUI();
        dice.disable(false);
        isAnimating = false;
        
        if (game.getWinner() != null) {
            SceneManager.getInstance().switchToGameOverScreen();
        }
    }

    @FXML
    private void handlePowerUp() {
        if (isAnimating) return;

        boolean confirmed = showConfirmDialog("Use Powerup", "Activate Powerup?",
                "This will consume energy. Do you want to proceed?");

        if (confirmed) {
            try {
                String name = game.getCurrent().getName();
                game.usePowerup();
                actionLog.logEvent(name + " used Powerup!");
                updateUI();
            } catch (Exception e) {
                showErrorAlert("Powerup Failed", e.getMessage());
                actionLog.logError("Powerup Failed: " + e.getMessage());
            }
        }
    }

    /**
     * Shows a confirmation dialog using a plain Stage (compatible with JavaFX 8).
     * Returns true if the user clicked OK.
     */
    private boolean showConfirmDialog(String title, String header, String content) {
        final boolean[] result = {false};

        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label headerLabel = new Label(header);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        okButton.setOnAction(e -> {
            result[0] = true;
            dialog.close();
        });
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, okButton, cancelButton);
        buttons.setAlignment(javafx.geometry.Pos.CENTER);

        VBox layout = new VBox(12, headerLabel, contentLabel, buttons);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        dialog.setScene(new Scene(layout, 320, 160));
        dialog.showAndWait();

        return result[0];
    }

    /**
     * Shows an error dialog using a plain Stage (compatible with JavaFX 8).
     */
    private void showErrorAlert(String title, String message) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label headerLabel = new Label("Invalid Action");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: red;");

        Label contentLabel = new Label(message);
        contentLabel.setWrapText(true);

        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(okButton);
        buttons.setAlignment(javafx.geometry.Pos.CENTER);

        VBox layout = new VBox(12, headerLabel, contentLabel, buttons);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        dialog.setScene(new Scene(layout, 320, 140));
        dialog.showAndWait();
    }
}
