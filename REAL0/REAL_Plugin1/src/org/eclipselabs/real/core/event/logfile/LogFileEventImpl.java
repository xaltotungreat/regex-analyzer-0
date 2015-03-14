package org.eclipselabs.real.core.event.logfile;

import java.util.UUID;

public class LogFileEventImpl implements ILogFileEvent {

    protected String eventId;

    public LogFileEventImpl() {
        eventId = UUID.randomUUID().toString();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

}
