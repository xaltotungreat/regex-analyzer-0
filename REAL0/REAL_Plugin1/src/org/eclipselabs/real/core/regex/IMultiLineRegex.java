package org.eclipselabs.real.core.regex;

import java.util.List;

public interface IMultiLineRegex extends IRealRegex {
    public List<String> getRegexStrings();
    public void setRegexStrings(List<String> regexStrings);
}
