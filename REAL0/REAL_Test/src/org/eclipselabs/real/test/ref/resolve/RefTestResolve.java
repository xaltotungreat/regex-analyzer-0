package org.eclipselabs.real.test.ref.resolve;

import java.util.HashMap;
import java.util.Map;

import org.eclipselabs.real.test.ref.RefTestBase;
import org.junit.Test;

public class RefTestResolve extends RefTestBase {

    public RefTestResolve() {
        super("test_config/ref/resolve/RefTestResolve.xml");
    }

	@Test
	public void test() {
	    Map<String,String> tagsMap = new HashMap<>();
	    tagsMap.put("Role", "User Object");
	    tagsMap.put("Interval", "TRUE");
		assertSOExists("!I:LogInterval AML", "GlobalTest", tagsMap);
	}

}
