package org.eclipselabs.real.core.config.ml.gui.constructor;

public interface IGUIConstructorFactory<K> {
    public ISOTreeConstructor<K> getSOTReeConstructor();
    public IPropertyConstructor<K> getPropertyConstructor();
    public IDisplaySOSelectorConstructor<K> getSelectorConstructor();
    public ISortRequestKeyConstructor<K> getSortRequestKeyConstructor();
}
