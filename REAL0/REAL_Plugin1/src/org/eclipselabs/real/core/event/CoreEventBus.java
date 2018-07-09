package org.eclipselabs.real.core.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipselabs.real.core.util.NamedThreadFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * This is the main event bus of the core framework. The clients are registered to
 * both the sync and async event buses but post events to one only. Sometimes the clients
 * need a sync and sometimes async event bus.
 * @author Vadim Great
 *
 */
public enum CoreEventBus {
    INSTANCE;

    /*
     * The order of events is important for this event bus consequently I use a single-threaded executor
     */
    private ExecutorService stExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("ST_CoreEventBus"));
    private AsyncEventBus asyncSingleThreadEventBus = new AsyncEventBus(stExecutor);
    private EventBus syncEventBus = new EventBus();

    /**
     * The listener is registered to receive events from both the sync
     * and the async event bus.
     * @param listener
     */
    public void register(Object listener) {
        asyncSingleThreadEventBus.register(listener);
        syncEventBus.register(listener);
    }

    /**
     * The listener is unregistered from both the sync
     * and the async event bus.
     * @param listener
     */
    public void unregister(Object listener) {
        asyncSingleThreadEventBus.unregister(listener);
        syncEventBus.unregister(listener);
    }

    /**
     * Posts the event to the sync event bus
     * @param event the event to post
     */
    public void postSync(Object event) {
        syncEventBus.post(event);
    }

    /**
     * Posts the event to the async event bus
     * @param event the event to post
     */
    public void postSingleThreadAsync(Object event) {
        asyncSingleThreadEventBus.post(event);
    }


}
