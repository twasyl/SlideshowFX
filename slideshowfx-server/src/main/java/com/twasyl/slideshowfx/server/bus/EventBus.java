package com.twasyl.slideshowfx.server.bus;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a simple event bus that can be used in the whole SlideshowFX application in order to share events.
 * In order to share events, {@link Actor actors} must register to end points.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class EventBus {

    private static EventBus singleton = null;

    private final Map<String, Set<Actor>> endPointsAndActorsMapping = new ConcurrentHashMap<>();

    /**
     * Private constructor of the EventBus in order to implement the singleton pattern.
     *
     * @see #getInstance()
     */
    private EventBus() {
    }

    /**
     * Get an instance of {@link EventBus}. This method takes care of having only one single instance of {@link EventBus}.
     *
     * @return The singleton instance of the {@link EventBus}.
     */
    public static synchronized EventBus getInstance() {
        if (singleton == null) {
            singleton = new EventBus();
        }

        return singleton;
    }

    /**
     * Check if a given endpoint is valid or not. The endpoint is considered invalid if it is {@code null} or its
     * trimmed value is empty.
     *
     * @param endPoint The endpoint to check.
     * @throws NullPointerException     If the endpoint is {@code null}
     * @throws IllegalArgumentException If the trimmed endpoint is empty.
     */
    private void checkEndPointIsValid(final String endPoint) {
        if (endPoint == null) throw new NullPointerException("The endPoint can not be null");

        final String trimmedEndPoint = endPoint.trim();
        if (trimmedEndPoint.isEmpty()) throw new IllegalArgumentException("The endPoint can not be empty");
    }

    /**
     * Check if a given {@link Actor actor} is valid or not. The {@link Actor actor} is considered valid if it is not
     * {@code null}.
     *
     * @param actor The {@link Actor actor} to check.
     */
    private void checkActorIsValid(final Actor actor) {
        if (actor == null) throw new NullPointerException("The actor can not be null");
    }

    /**
     * Subscribe a given {@link Actor actor} to a given endpoint. Both endpoint and actor can not be  {@code null}.
     *
     * @param endPoint The endpoint to register the actor on.
     * @param actor    The actor to subscribe.
     * @return This instance of {@link EventBus}.
     * @throws NullPointerException     If either the endPoint or the actor is {@code null}.
     * @throws IllegalArgumentException If the endpoint is empty.
     */
    public synchronized EventBus subscribe(final String endPoint, final Actor actor) {
        checkActorIsValid(actor);
        checkEndPointIsValid(endPoint);

        final String trimmedEndPoint = endPoint.trim();
        endPointsAndActorsMapping.computeIfAbsent(trimmedEndPoint, endpoint -> endPointsAndActorsMapping.put(endpoint, new HashSet<>()));
        this.endPointsAndActorsMapping.get(trimmedEndPoint).add(actor);

        return this;
    }

    /**
     * Unsubscribe a given {@link Actor actor} from a given endpoint. Both endpoint and actor can not be {@code null}.
     *
     * @param endPoint The endpoint to unsubscribe the actor for.
     * @param actor    The actor to unsubscribe.
     * @return This instance of {@link EventBus}.
     * @throws NullPointerException     If either the endPoint or the actor is {@code null}.
     * @throws IllegalArgumentException If the endpoint is empty.
     */
    public synchronized EventBus unsubscribe(final String endPoint, final Actor actor) {
        checkActorIsValid(actor);
        checkEndPointIsValid(endPoint);

        final String trimmedEndPoint = endPoint.trim();
        if (endPointsAndActorsMapping.containsKey(trimmedEndPoint)) {
            endPointsAndActorsMapping.get(trimmedEndPoint).remove(actor);
        }

        return this;
    }

    /**
     * Removes the given endpoint if it is registered to this {@link EventBus bus}. If the given endpoint isn't
     * registered to this bus, nothing is performed.
     *
     * @param endPoint The endpoint to remove.
     * @return This instance of {@link EventBus}.
     * @throws NullPointerException     If either the endPoint or the actor is {@code null}.
     * @throws IllegalArgumentException If the endpoint is empty.
     */
    public synchronized EventBus removeEndpoint(final String endPoint) {
        checkEndPointIsValid(endPoint);

        final String trimmedEndPoint = endPoint.trim();
        endPointsAndActorsMapping.remove(trimmedEndPoint);

        return this;
    }

    /**
     * Broadcast a given message to all subscribers of a given endpoint.
     *
     * @param endPoint The endpoint to send the message to.
     * @param message  The message to send.
     * @throws NullPointerException     If the endPoint is {@code null}.
     * @throws IllegalArgumentException If the endpoint is empty.
     */
    public void broadcast(final String endPoint, final Object message) {
        checkEndPointIsValid(endPoint);

        final Set<Actor> actors = this.endPointsAndActorsMapping.get(endPoint.trim());

        if (actors != null) {
            actors.forEach(actor -> {
                if (actor.supportsMessage(message)) {
                    final Runnable runnable = () -> actor.onMessage(message);
                    new Thread(runnable).start();
                }
            });
        }
    }
}
