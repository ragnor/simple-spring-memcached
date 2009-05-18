package net.nelz.simplesm.test;

import net.nelz.simplesm.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

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
public class UpdateMultiCacheTest {
	private ApplicationContext context;

	@BeforeClass
	public void beforeClass() {
		context = new ClassPathXmlApplicationContext("/test-context.xml");
	}

	@Test
	public void test() {
		final Long rawNow = System.currentTimeMillis();
		final Long now = (rawNow / 1000) * 10000;
		final List<Long> subset = new ArrayList<Long>();
		final List<Long> superset = new ArrayList<Long>();

		for (Long ix = 1 + now; ix < 35 + now; ix++) {
			if (ix % 3 == 0) {
				subset.add(ix);
			}
			superset.add(ix);
		}

		final Map<Long, String> originalResults = new HashMap<Long, String>();
		final Map<Long, String> expectedResults = new HashMap<Long, String>();

		final TestSvc test = (TestSvc) context.getBean("testSvc");

		final List<String> r1List = test.getTimestampValues(superset);
		for (int ix = 0; ix < r1List.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = r1List.get(ix);

			originalResults.put(key, value);
			if (!subset.contains(key)) {
				expectedResults.put(key, value);
			}
		}

		final List<String> subsetUpdateResult = test.updateTimestamValues(subset);
		for (int ix = 0; ix < subset.size(); ix++) {
			final Long key = subset.get(ix);
			final String value = subsetUpdateResult.get(ix);
			assertFalse(originalResults.get(key).equals(value));
			assertTrue(value.indexOf("-M-") != -1);
			expectedResults.put(key, value);
		}

		final List<String> r2List = test.getTimestampValues(superset);
		for (int ix = 0; ix < r2List.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = r2List.get(ix);
			System.out.println(value);
			assertEquals(expectedResults.get(key), value);
		}
	}

    @Test
    public void testDataIndex() {
        final Map<Long, String> expectedResults = new HashMap<Long, String>();
        final Long rawNow = System.currentTimeMillis();
        final Long now = (rawNow / 1000) * 10000;
        final List<Long> subset = new ArrayList<Long>();
        final List<String> overrideValues = new ArrayList<String>();
        final List<Long> superset = new ArrayList<Long>();

        for (Long ix = 1 + now; ix < 35 + now; ix++) {
            if (ix % 3 == 0) {
                subset.add(ix);
                final String overrideValue = "big-fat-override-value-" + ix;
                expectedResults.put(ix, overrideValue);
                overrideValues.add(overrideValue);
            }
            superset.add(ix);
        }

        final Map<Long, String> originalResults = new HashMap<Long, String>();

        final TestSvc test = (TestSvc) context.getBean("testSvc");

        final List<String> r1List = test.getTimestampValues(superset);
        for (int ix = 0; ix < r1List.size(); ix++) {
            final Long key = superset.get(ix);
            final String value = r1List.get(ix);

            originalResults.put(key, value);
            if (!subset.contains(key)) {
                expectedResults.put(key, value);
            }
        }

        test.overrideTimestampValues(42, subset, "Nada", overrideValues);

        final List<String> r2List = test.getTimestampValues(superset);
        for (int ix = 0; ix < r2List.size(); ix++) {
            final Long key = superset.get(ix);
            final String value = r2List.get(ix);
            System.out.println(value);
            assertEquals(expectedResults.get(key), value);
        }
    }
}
