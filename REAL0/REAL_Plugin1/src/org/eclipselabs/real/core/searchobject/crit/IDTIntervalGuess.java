package org.eclipselabs.real.core.searchobject.crit;

import org.eclipselabs.real.core.regex.IRealRegex;

public interface IDTIntervalGuess extends IAcceptanceGuess {

    public IRealRegex getFirstRecord();
    public void setFirstRecord(IRealRegex fstRec);
    public IRealRegex getLastRecord();
    public void setLastRecord(IRealRegex lstRec);
}
