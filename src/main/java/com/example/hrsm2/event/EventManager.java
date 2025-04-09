package com.example.hrsm2.event;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Singleton event manager for handling application-wide events.
 * This allows controllers to register for events and receive notifications.
 */
public class EventManager {
    
    private static EventManager instance;
    
    private final Map<EventType<? extends Event>, Set<EventHandler<? extends Event>>> handlers = new HashMap<>();
    
    private EventManager() {
        // Private constructor for singleton
    }
    
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }
    
    /**
     * Register a handler for a specific event type.
     * 
     * @param eventType The type of event to listen for
     * @param handler The handler to call when the event occurs
     * @param <T> The type of event
     */
    public <T extends Event> void addEventHandler(EventType<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(handler);
    }
    
    /**
     * Remove a handler for a specific event type.
     * 
     * @param eventType The type of event to stop listening for
     * @param handler The handler to remove
     * @param <T> The type of event
     */
    public <T extends Event> void removeEventHandler(EventType<T> eventType, EventHandler<T> handler) {
        Set<EventHandler<? extends Event>> eventHandlers = handlers.get(eventType);
        if (eventHandlers != null) {
            eventHandlers.remove(handler);
        }
    }
    
    /**
     * Fire an event to all registered handlers.
     * 
     * @param event The event to fire
     * @param <T> The type of event
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void fireEvent(T event) {
        Set<EventHandler<? extends Event>> eventHandlers = handlers.get(event.getEventType());
        if (eventHandlers != null) {
            for (EventHandler<? extends Event> handler : eventHandlers) {
                ((EventHandler<T>) handler).handle(event);
            }
        }
    }
} 