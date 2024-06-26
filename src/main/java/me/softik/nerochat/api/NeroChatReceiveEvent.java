package me.softik.nerochat.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Gets executed per player the plugin tries to send a message to.
 */
@SuppressWarnings("unused")
public class NeroChatReceiveEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;

    private final Player sender, receiver;
    private String message;

    public NeroChatReceiveEvent(Player sender, Player receiver, String message) {
        super(true);
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isCancelled = false;
    }

    /**
     * Get the player who sends the message.
     *
     * @return the player who sends the message
     */
    public Player getSender() {
        return sender;
    }

    /**
     * Get the player who receives the message.
     *
     * @return the player who receives the message
     */
    public Player getReceiver() {
        return receiver;
    }

    /**
     * Get the message the player sends.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message that the player sends.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
