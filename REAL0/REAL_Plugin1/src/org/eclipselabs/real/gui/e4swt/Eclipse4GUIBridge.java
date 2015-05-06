package org.eclipselabs.real.gui.e4swt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipselabs.real.core.util.NamedThreadFactory;

public enum Eclipse4GUIBridge implements IEclipse4Constants {
    INSTANCE;

    protected ExecutorService guiCachedTPExecutor = Executors.newCachedThreadPool(
            new NamedThreadFactory("GUI-CachedTP"));

    protected ExecutorService guiFixedTPExecutor = Executors.newFixedThreadPool(1,
            new NamedThreadFactory("GUI-FixedTP"));

    public ExecutorService getGuiCachedTPExecutor() {
        return guiCachedTPExecutor;
    }

    public ExecutorService getGuiFixedTPExecutor() {
        return guiFixedTPExecutor;
    }
}
