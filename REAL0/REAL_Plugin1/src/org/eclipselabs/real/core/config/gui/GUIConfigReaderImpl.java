package org.eclipselabs.real.core.config.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.eclipselabs.real.core.config.ConstructionTask;
import org.eclipselabs.real.core.config.IConfigObjectConstructor;
import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.gui.core.GUIConfigController;
import org.eclipselabs.real.gui.core.GUIConfigKey;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public abstract class GUIConfigReaderImpl<U> implements IConfigReader<U> {
    protected List<Lock> modificationLocks = new ArrayList<Lock>();
    protected ListeningExecutorService configReaderExecutor;
    
    public class GUICompletionCallback implements IGUIConfigCompletionCallback {

        @Override
        public void addAll(Map<GUIConfigKey, Object> guiMap) {
            GUIConfigController.INSTANCE.getGUIObjectRepository().addAll(guiMap);
        }
        
    }
    
    public GUIConfigReaderImpl(ListeningExecutorService executor) {
        configReaderExecutor = executor;
        modificationLocks.add(GUIConfigController.INSTANCE.getGUIObjectRepository().getWriteLock());
    }
    
    protected <K, V> ListenableFuture<V> submitConstructionTask(
            IConfigObjectConstructor<K, V> coConstructor,
            IConstructionSource<K> aSource) {
        ConstructionTask<K, V> newTask = new ConstructionTask<K, V>(
                coConstructor, aSource);
        return configReaderExecutor.submit(newTask);
    }

}
