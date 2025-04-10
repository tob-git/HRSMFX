package com.example.hrsm2.gui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A notification system that displays messages within the current window
 * instead of using alert dialogs that interrupt the workflow.
 */
public class NotificationSystem {
    
    public enum Type {
        SUCCESS, ERROR, WARNING, INFO
    }
    
    /**
     * Shows a notification message that automatically fades away after a specified duration.
     * 
     * @param container The StackPane where the notification will be shown
     * @param message The text message to display
     * @param type The type of notification (SUCCESS, ERROR, WARNING, INFO)
     * @param durationSeconds How long the notification should remain visible
     */
    public static void showNotification(StackPane container, String message, Type type, double durationSeconds) {
        // Create the notification label
        Label notification = new Label(message);
        notification.setMaxWidth(400);
        notification.setWrapText(true);
        notification.setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
        notification.setAlignment(Pos.CENTER);
        
        // Style the notification based on type
        String style = "-fx-background-radius: 5; -fx-border-radius: 5;";
        
        switch (type) {
            case SUCCESS:
                notification.setStyle(style + "-fx-background-color: rgba(76, 175, 80, 0.9); -fx-text-fill: white;");
                break;
            case ERROR:
                notification.setStyle(style + "-fx-background-color: rgba(244, 67, 54, 0.9); -fx-text-fill: white;");
                break;
            case WARNING:
                notification.setStyle(style + "-fx-background-color: rgba(255, 152, 0, 0.9); -fx-text-fill: white;");
                break;
            case INFO:
                notification.setStyle(style + "-fx-background-color: rgba(33, 150, 243, 0.9); -fx-text-fill: white;");
                break;
        }
        
        // Position at bottom center with some margin
        StackPane.setAlignment(notification, Pos.BOTTOM_CENTER);
        StackPane.setMargin(notification, new javafx.geometry.Insets(0, 0, 20, 0));
        
        // Start with zero opacity (invisible)
        notification.setOpacity(0);
        
        // Add to the container
        container.getChildren().add(notification);
        
        // Create fade-in animation
        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(notification.opacityProperty(), 0)),
            new KeyFrame(Duration.seconds(0.3), new KeyValue(notification.opacityProperty(), 1))
        );
        
        // Create fade-out animation
        Timeline fadeOut = new Timeline(
            new KeyFrame(Duration.seconds(durationSeconds), new KeyValue(notification.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(durationSeconds + 0.5), event -> {
                container.getChildren().remove(notification);
            }, new KeyValue(notification.opacityProperty(), 0))
        );
        
        // Play the animations
        fadeIn.play();
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