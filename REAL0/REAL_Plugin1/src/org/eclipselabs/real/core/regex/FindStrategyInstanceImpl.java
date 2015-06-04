package org.eclipselabs.real.core.regex;

abstract class FindStrategyInstanceImpl extends FindStrategyImpl {

    protected Integer mainInstanceNumber;
    protected int currentInstanceNumber = 0;

    public FindStrategyInstanceImpl(FindStrategyType aType, String text, Integer inst) {
        super(aType, text);
        if (inst != null) {
            mainInstanceNumber = inst;
        } else {
            mainInstanceNumber = 0;
        }
    }

    @Override
    public void region(int rgStart, int rgEnd) {
        super.region(rgStart, rgEnd);
        currentInstanceNumber = 0;
    }

}
