package game.gui.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ActionLogComponent extends ScrollPane {

    private VBox logContainer;

    public ActionLogComponent() {
        this.getStyleClass().add("action-log-scrollpane");
        this.setFitToWidth(true);
        this.setPrefViewportHeight(100);
        this.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);

        logContainer = new VBox(5);
        logContainer.setPadding(new Insets(10));
        logContainer.getStyleClass().add("action-log-container");

        this.setContent(logContainer);
    }

    public void log(String message) {
        log(message, "log-message-normal");
    }

    public void logEvent(String message) {
        log(message, "log-message-event");
    }

    public void logError(String message) {
        log(message, "log-message-error");
    }

    private void log(String message, String styleClass) {
        Label logLabel = new Label("> " + message);
        logLabel.setWrapText(true);
        logLabel.getStyleClass().addAll("log-message", styleClass);

        logContainer.getChildren().add(logLabel);

        // Auto-scroll to bottom
        Platform.runLater(() -> this.setVvalue(1.0));
    }

    public void clear() {
        logContainer.getChildren().clear();
    }
}
