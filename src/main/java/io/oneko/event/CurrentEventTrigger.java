package io.oneko.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class CurrentEventTrigger {

    private final ThreadLocal<EventTrigger> triggerPerThread = new ThreadLocal<>();

    public Optional<EventTrigger> currentTrigger() {
        return Optional.ofNullable(triggerPerThread.get());
    }

    public void setCurrentTrigger(EventTrigger trigger) {
        currentTrigger().ifPresent(t -> log.warn("There is already a thread local event trigger {}", t));
        this.triggerPerThread.set(trigger);
    }

    /**
     * Sets the current event trigger for the try block as a resource.<br/>
     * Usage:
     * <pre>
     *     try (var ignore = currentEventTrigger.forTryBlock(anEventTrigger)) {
     *          //do stuff that create events
     *     }
     * </pre>
     * This is equivalent to
     * <pre>
     *     currentEventTrigger.setCurrentTrigger(anEventTrigger);
     *     try {
     *         //do stuff that create events
     *     } finally {
     *         currentEventTrigger.unset();
     *     }
     * </pre>
     */
    public BlockWiseEventTrigger forTryBlock(EventTrigger trigger) {
        return new BlockWiseEventTrigger(trigger, this);
    }

    public void unset() {
        if (currentTrigger().isEmpty()) {
            log.info("Trying to unset the current event trigger, but none is set.");
        }
        this.triggerPerThread.remove();
    }
}
