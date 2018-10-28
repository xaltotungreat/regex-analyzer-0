package org.eclipselabs.real.core.config.ml.gui;

import java.util.Map;

import org.eclipselabs.real.gui.core.GUIConfigKey;

public interface IGUIConfigCompletionCallback {
    public void addAll(Map<GUIConfigKey, Object> guiMap);
}
