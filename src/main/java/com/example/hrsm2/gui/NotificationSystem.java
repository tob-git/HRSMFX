package com.example.hrsm2.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Utility class for displaying notification messages within the application.
 * Displays temporary, styled notifications that automatically disappear after a set duration.
 */
public class NotificationSystem {

    /**
     * Type of notification, affecting the styling and icon
     */
    public enum Type {
        INFO(Color.rgb(70, 130, 180), Color.WHITE),    // Steel Blue
        SUCCESS(Color.rgb(60, 179, 113), Color.WHITE), // Medium Sea Green
        WARNING(Color.rgb(255, 165, 0), Color.BLACK),  // Orange
        ERROR(Color.rgb(220, 20, 60), Color.WHITE);    // Crimson

        private final Color backgroundColor;
        private final Color textColor;

        Type(Color backgroundColor, Color textColor) {
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        public Color getTextColor() {
            return textColor;
        }
    }

    /**
     * Shows a notification message in the provided StackPane container
     * 
     * @param container The StackPane to display the notification in
     * @param message The message to display
     * @param type The type of notification (influences styling)
     * @param durationInSeconds How long the notification should remain visible
     */
    public static void showNotification(StackPane container, String message, Type type, int durationInSeconds) {
        if (container == null) {
            System.err.println("Notification container is null");
            return;
        }

        // Create notification pane
        StackPane notification = new StackPane();
        notification.setMaxWidth(400);
        notification.setMinHeight(60);
        notification.setPadding(new Insets(15));
        notification.setBackground(new Background(
                new BackgroundFill(type.getBackgroundColor(), new CornerRadii(5), Insets.EMPTY)));
        notification.setOpacity(0.9);
        
        // Create message label
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(type.getTextColor());
        messageLabel.setWrapText(true);
        
        notification.getChildren().add(messageLabel);
        StackPane.setAlignment(notification, Pos.TOP_CENTER);
        
        // Add the notification to the container
        container.getChildren().add(notification);
        
        // Create animations for showing and hiding
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(notification.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(200), new KeyValue(notification.opacityProperty(), 0.9))
        );
        
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.millis(durationInSeconds * 1000 - 200), 
                        new KeyValue(notification.opacityProperty(), 0.9)),
                new KeyFrame(Duration.millis(durationInSeconds * 1000), 
                        new KeyValue(notification.opacityProperty(), 0))
        );
        
        // Play animations and then remove the notification
        fadeIn.play();
        fadeOut.setOnFinished(e -> container.getChildren().remove(notification));
        fadeOut.play();
    }

    /**
     * Shows a success notification with default duration.
     */
    public static void showSuccess(StackPane container, String message) {
        showNotification(container, message, Type.SUCCESS, 3);
    }
    
    /**
     * Shows an error notification with default duration.
     */
    public static void showError(StackPane container, String message) {
        showNotification(container, message, Type.ERROR, 4);
    }
    
    /**
     * Shows a warning notification with default duration.
     */
    public static void showWarning(StackPane container, String message) {
        showNotification(container, message, Type.WARNING, 3);
    }
    
    /**
     * Shows an info notification with default duration.
     */
    public static void showInfo(StackPane container, String message) {
        showNotification(container, message, Type.INFO, 3);
    }
}