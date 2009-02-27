package net.nelz.simplesm.test;

import org.springframework.context.*;
import org.springframework.context.support.*;
import org.testng.annotations.*;
import static org.testng.AssertJUnit.*;
import net.nelz.simplesm.test.svc.*;

import java.util.*;

/**
Copyright (c) 2008  Nelson Carpentier

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
public class UpdateSingleCacheTest {
	private ApplicationContext context;

	@BeforeClass
	public void beforeClass() {
		context = new ClassPathXmlApplicationContext("/test-context.xml");
	}

	@Test
	public void test() {
		final Long rawNow = new Date().getTime();
		final Long now = (rawNow / 1000) * 10000;
		final List<Long> subset = new ArrayList<Long>();
		final List<Long> superset = new ArrayList<Long>();

		for (Long ix = 1 + now; ix < 35 + now; ix++) {
			if (ix % 3 == 0) {
                // Add every 3rd generated key to the 'subset'
                subset.add(ix);
			}
			superset.add(ix);
		}

		final Map<Long, String> originalResults = new HashMap<Long, String>();
		final Map<Long, String> expectedResults = new HashMap<Long, String>();

		final TestSvc test = (TestSvc) context.getBean("testSvc");

        // This should hit the DAO, filling every value with the same first part,
        // followed by an "X", followed by the key.
        final List<String> r1List = test.getTimestampValues(superset);
		for (int ix = 0; ix < r1List.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = r1List.get(ix);

			originalResults.put(key, value);
			if (!subset.contains(key)) {
				expectedResults.put(key, value);
			}
		}

        // Go thru each key in the 'subset', and replace the cache
        // value with a single value, followed by a "U", followed by the key.
        for (final Long key : subset) {
            final String value = test.updateTimestampValue(key);
			assertFalse(originalResults.get(key).equals(value));
			expectedResults.put(key, value);
		}

        // Now get the list out of the cache, and make sure all the values
        // are as we expect. I.e. every 3rd value (in the 'subset') has a value with a "U"
        // rather than the original "X"
        final List<String> r2List = test.getTimestampValues(superset);
		for (int ix = 0; ix < r2List.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = r2List.get(ix);
			System.out.println(value);
			assertEquals(expectedResults.get(key), value);
		}
	}

}
