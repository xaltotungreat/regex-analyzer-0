package org.eclipselabs.real.core.config.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipselabs.real.core.config.ConstructionTask;
import org.eclipselabs.real.core.config.IConfigObjectConstructor;
import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.util.NamedLock;
import org.eclipselabs.real.gui.core.GUIConfigController;
import org.eclipselabs.real.gui.core.GUIConfigKey;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

public abstract class GUIConfigReaderImpl<U> implements IConfigReader<U> {
    protected List<NamedLock> modificationLocks = new ArrayList<NamedLock>();
    protected ListeningExecutorService configReaderExecutor;

    public class GUICompletionCallback implements IGUIConfigCompletionCallback {

        @Override
        public void addAll(Map<GUIConfigKey, Object> guiMap) {
            GUIConfigController.INSTANCE.getGUIObjectRepository().addAll(guiMap);
        }

    }

    public GUIConfigReaderImpl(ListeningExecutorService executor) {
        configReaderExecutor = executor;
        modificationLocks.add(
                new NamedLock(GUIConfigController.INSTANCE.getGUIObjectRepository().getWriteLock(), "GUIObj Repo write lock"));
    }

    protected <K, V> ListenableFuture<V> submitConstructionTask(
            IConfigObjectConstructor<K, V> coConstructor,
            IConstructionSource<K> aSource) {
        ConstructionTask<K, V> newTask = new ConstructionTask<K, V>(
                coConstructor, aSource);
        return configReaderExecutor.submit(newTask);
    }

}
