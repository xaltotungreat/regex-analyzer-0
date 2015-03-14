package org.eclipselabs.real.gui.e4swt;

import java.util.concurrent.Executors;

import org.eclipselabs.real.core.util.NamedThreadFactory;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public enum Eclipse4GUIBridge implements IEclipse4Constants {
    INSTANCE;
    
    protected ListeningExecutorService guiCachedTPExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(
            new NamedThreadFactory("GUI-CachedTP")));
    
    protected ListeningExecutorService guiFixedTPExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1,
            new NamedThreadFactory("GUI-FixedTP")));

    public ListeningExecutorService getGuiCachedTPExecutor() {
        return guiCachedTPExecutor;
    }
    
    public ListeningExecutorService getGuiFixedTPExecutor() {
        return guiFixedTPExecutor;
    }
}
