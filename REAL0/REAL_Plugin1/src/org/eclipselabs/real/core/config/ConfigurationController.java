package org.eclipselabs.real.core.config;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipselabs.real.core.config.gui.xml.GUIXmlConfigFileReader;
import org.eclipselabs.real.core.config.regex.xml.RegexXmlConfigFileReader;
import org.eclipselabs.real.core.util.NamedThreadFactory;

public enum ConfigurationController {
    INSTANCE;

    protected ExecutorService configReaderExecutor = Executors.newFixedThreadPool(3,
            new NamedThreadFactory("ReadConfigThread"));

    public CompletableFuture<Integer> initXmlRegexConfiguration(InputStream configFileStream) {
        IConfigReader<InputStream> regexCFReader = new RegexXmlConfigFileReader(configReaderExecutor);
        return regexCFReader.read(configFileStream);
    }

    public CompletableFuture<Integer> initXmlGUIConfiguration(InputStream configFileStream) {
        IConfigReader<InputStream> guiCFReader = new GUIXmlConfigFileReader(configReaderExecutor);
        return guiCFReader.read(configFileStream);
    }


}
