package org.eclipselabs.real.core.config.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.eclipselabs.real.core.config.ConstructionTask;
import org.eclipselabs.real.core.config.IConfigObjectConstructor;
import org.eclipselabs.real.core.config.IConfigReader;
import org.eclipselabs.real.core.config.IConstructionSource;
import org.eclipselabs.real.core.util.NamedLock;
import org.eclipselabs.real.gui.core.GUIConfigController;
import org.eclipselabs.real.gui.core.GUIConfigKey;

public abstract class GUIConfigReaderImpl<U> implements IConfigReader<U, Integer> {
    protected List<NamedLock> modificationLocks = new ArrayList<>();
    protected ExecutorService configReaderExecutor;

    public class GUICompletionCallback implements IGUIConfigCompletionCallback {

        @Override
        public void addAll(Map<GUIConfigKey, Object> guiMap) {
            GUIConfigController.INSTANCE.getGUIObjectRepository().addAll(guiMap);
        }

    }

    public GUIConfigReaderImpl(ExecutorService executor) {
        configReaderExecutor = executor;
        modificationLocks.add(
                new NamedLock(GUIConfigController.INSTANCE.getGUIObjectRepository().getWriteLock(), "GUIObj Repo write lock"));
    }

    protected <K, V> CompletableFuture<V> submitConstructionTask(
            IConfigObjectConstructor<K, V> coConstructor,
            IConstructionSource<K> aSource) {
        ConstructionTask<K, V> newTask = new ConstructionTask<>(
                coConstructor, aSource);
        return CompletableFuture.supplyAsync(newTask, configReaderExecutor);//configReaderExecutor.submit(newTask);
    }

}
