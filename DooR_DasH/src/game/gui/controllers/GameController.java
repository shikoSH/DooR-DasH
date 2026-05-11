package game.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import javafx.scene.shape.Rectangle;

public class GameController {

	//all of our images
	private Image normalImage =
		    new Image(getClass().getResourceAsStream("NormalCell.png"));

		private Image ScarerdoorImage =
		    new Image(getClass().getResourceAsStream("Scarer_ClosedDoor_Cell2.png"));

		private Image laugherdoorImage =
			    new Image(getClass().getResourceAsStream("Laugher_ClosedDoor_Cell.png"));
		
		
		private Image monsterImage_celia_mae=
		    new Image(getClass().getResourceAsStream("celia mae.png"));

		private Image monsterImage_Fungus=
			    new Image(getClass().getResourceAsStream("Fungus.png"));

		
		private Image monsterImage_Henry_J_Waternoose_III=
			    new Image(getClass().getResourceAsStream("Henry_J._Waternoose_III.png"));
		
		
		private Image monsterImage_James_sullivan=
			    new Image(getClass().getResourceAsStream("James sullivan.png"));
		
		
		private Image monsterImage_Mike_Wazowski=
			    new Image(getClass().getResourceAsStream("Mike_Wazowski.png"));
		

		private Image monsterImage_Randall=
			    new Image(getClass().getResourceAsStream("Randall.png"));
		
		
		private Image monsterImage_Roz=
	    new Image(getClass().getResourceAsStream("Roz.png"));
		
		private Image monsterImage_Yeti=
			    new Image(getClass().getResourceAsStream("Yeti.png"));
			
		
		private Image conveyorImage =
		    new Image(getClass().getResourceAsStream("conveyor.png"));

		private Image contaminationImage =
		    new Image(getClass().getResourceAsStream("sock.png"));

		
		
		//cards
		private Image cardImage =
		    new Image(getClass().getResourceAsStream("card.png"));
	
		
		
    @FXML
    private StackPane boardContainer;
    
    @FXML
    private GridPane grid; 
    
    private ImageView[][] cellViews;
    
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
    	cellViews = new ImageView[Constants.BOARD_ROWS][Constants.BOARD_COLS];

    	for (int row = 0; row < Constants.BOARD_ROWS; row++) {

    	    for (int col = 0; col < Constants.BOARD_COLS; col++) {

    	        ImageView imageView = new ImageView();

    	        imageView.setFitWidth(64);
    	        imageView.setFitHeight(64);

    	        cellViews[row][col] = imageView;

    	        grid.add(imageView, col, row);
    	    }
    	}
    }
    
    
    private void refreshBoard() {

        Board board = game.getBoard();

        Cell[][] cells = board.getBoardCells();

        for (int row = 0; row < Constants.BOARD_ROWS; row++) {

            for (int col = 0; col < Constants.BOARD_COLS; col++) {

                Cell cell = cells[row][col];

                // MONSTER CELL
                if (cell instanceof MonsterCell) {

                    Monster monster =
                            ((MonsterCell) cell).getMonster();

                    if (monster.getName().equals("Celia Mae"))
                        cellViews[row][col].setImage(monsterImage_celia_mae);

                    else if (monster.getName().equals("Fungus"))
                        cellViews[row][col].setImage(monsterImage_Fungus);

                    else if (monster.getName().equals("Henry J. Waternoose III"))
                        cellViews[row][col].setImage(monsterImage_Henry_J_Waternoose_III);

                    else if (monster.getName().equals("James Sullivan"))
                        cellViews[row][col].setImage(monsterImage_James_sullivan);

                    else if (monster.getName().equals("Mike Wazowski"))
                        cellViews[row][col].setImage(monsterImage_Mike_Wazowski);

                    else if (monster.getName().equals("Randall"))
                        cellViews[row][col].setImage(monsterImage_Randall);

                    else if (monster.getName().equals("Roz"))
                        cellViews[row][col].setImage(monsterImage_Roz);

                    else if (monster.getName().equals("Yeti"))
                        cellViews[row][col].setImage(monsterImage_Yeti);

                    else
                        cellViews[row][col].setImage(normalImage);
                }

                // DOOR CELL
                else if (cell instanceof DoorCell) {

                    DoorCell door = (DoorCell) cell;

                    if (door.getRole() == Role.SCARER)
                        cellViews[row][col].setImage(ScarerdoorImage);

                    else
                        cellViews[row][col].setImage(laugherdoorImage);
                }

                // CONVEYOR BELT
                else if (cell instanceof ConveyorBelt) {

                    cellViews[row][col].setImage(conveyorImage);
                }

                // SOCK
                else if (cell instanceof ContaminationSock) {

                    cellViews[row][col].setImage(contaminationImage);
                }

                // CARD CELL
                else if (cell instanceof CardCell) {

                    cellViews[row][col].setImage(cardImage);
                }

                // NORMAL CELL
                else {

                    cellViews[row][col].setImage(normalImage);
                }
            }
        }
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

        Board board = game.getBoard();

        Cell[][] cells = board.getBoardCells();

        for (int row = 0; row < Constants.BOARD_ROWS; row++) {

            for (int col = 0; col < Constants.BOARD_COLS; col++) {

                Cell cell = cells[row][col];

                // NORMAL CELL
                if (cell instanceof MonsterCell) {

                    Monster monster =
                            ((MonsterCell) cell).getMonster();

                    if (monster.getName().equals("Celia Mae"))
                        cellViews[row][col].setImage(monsterImage_celia_mae);

                    else if (monster.getName().equals("Fungus"))
                        cellViews[row][col].setImage(monsterImage_Fungus);

                    else if (monster.getName().equals("Henry J. Waternoose III"))
                        cellViews[row][col].setImage(monsterImage_Henry_J_Waternoose_III);

                    else if (monster.getName().equals("James Sullivan"))
                        cellViews[row][col].setImage(monsterImage_James_sullivan);

                    else if (monster.getName().equals("Mike Wazowski"))
                        cellViews[row][col].setImage(monsterImage_Mike_Wazowski);

                    else if (monster.getName().equals("Randall"))
                        cellViews[row][col].setImage(monsterImage_Randall);

                    else if (monster.getName().equals("Roz"))
                        cellViews[row][col].setImage(monsterImage_Roz);

                    else if (monster.getName().equals("Yeti"))
                        cellViews[row][col].setImage(monsterImage_Yeti);

                    else
                        cellViews[row][col].setImage(normalImage);
                }

                // DOOR CELLS
                else if (cell instanceof DoorCell) {

                    DoorCell door = (DoorCell) cell;

                    if (door.getRole() == Role.SCARER)
                        cellViews[row][col].setImage(ScarerdoorImage);

                    else
                        cellViews[row][col].setImage(laugherdoorImage);
                }

                // CONVEYOR
                else if (cell instanceof ConveyorBelt) {

                    cellViews[row][col].setImage(conveyorImage);
                }

                // SOCK
                else if (cell instanceof ContaminationSock) {

                    cellViews[row][col].setImage(contaminationImage);
                }

                // CARD
                else if (cell instanceof CardCell) {

                    cellViews[row][col].setImage(cardImage);
                }

                // NORMAL
                else {

                    cellViews[row][col].setImage(normalImage);
                }
            }
        }
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
