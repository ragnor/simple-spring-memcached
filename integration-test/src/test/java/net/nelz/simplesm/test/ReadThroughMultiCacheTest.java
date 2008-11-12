package net.nelz.simplesm.test;

import static org.testng.AssertJUnit.*;
import net.nelz.simplesm.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
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
public class ReadThroughMultiCacheTest {
	private ApplicationContext context;

	@BeforeClass
	public void beforeClass() {
		context = new ClassPathXmlApplicationContext("/test-context.xml");
	}

	@Test
	public void test() {
		final List<Long> subset = new ArrayList<Long>();
		final List<Long> superset = new ArrayList<Long>();
		final List<Long> jumbleset = new ArrayList<Long>();

		for (Long ix = 1L; ix < 35; ix++) {
			if (ix % 3 == 0) {
				subset.add(ix);
			}
			superset.add(ix);
			jumbleset.add(ix);
		}
		Collections.shuffle(jumbleset);

		final TestSvc test = (TestSvc) context.getBean("testSvc");

		// Get all the results for the subset ids.
		// Ensure the ids line up with the results, and have the same timestamp.
		final List<String> subsetResult = test.getTimestampValues(subset);
		assertEquals(subset.size(), subsetResult.size());
		String subsetTime = null;
		for (int ix = 0; ix < subset.size(); ix++) {
			final Long key = subset.get(ix);
			final String value = subsetResult.get(ix);
			System.out.println("Subset: " + value);
			final String[] parts = value.split("-X-");
			if (subsetTime == null) {
				subsetTime = parts[0];
			} else {
				assertEquals(subsetTime, parts[0]);
			}
			assertEquals(key.toString(), parts[1]);
		}

		// Now call the full list.
		// Ensure id's line up, and that results from ids that got passed in the subset
		// have the older time stamp.
		final List<String> supersetResult = test.getTimestampValues(superset);
		assertEquals(superset.size(), supersetResult.size());
		String supersetTime = null;
		for (int ix = 0; ix < superset.size(); ix++) {
			final Long key = superset.get(ix);
			final String value = supersetResult.get(ix);
			System.out.println("Superset: " + value);
			final String[] parts = value.split("-X-");
			final boolean inSubset = subset.contains(key);
			if (!inSubset && supersetTime == null) {
				supersetTime = parts[0];
			} else if (inSubset) {
				assertEquals(subsetTime, parts[0]);
			} else {
				assertEquals(supersetTime, parts[0]);
			}
			assertEquals(key.toString(), parts[1]);
		}

		// Now call for the results again, but with a randomized
		// set of keys.  This is to ensure the proper values line up with
		// the given keys.
		final List<String> jumblesetResult = test.getTimestampValues(jumbleset);
		assertEquals(jumbleset.size(), jumblesetResult.size());
		for (int ix = 0; ix < jumbleset.size(); ix++) {
			final Long key = jumbleset.get(ix);
			final String value = jumblesetResult.get(ix);
			System.out.println("Jumbleset: " + value);
			final String[] parts = value.split("-X-");
			final boolean inSubset = subset.contains(key);
			if (inSubset) {
				assertEquals(subsetTime, parts[0]);
			} else {
				assertEquals(supersetTime, parts[0]);
			}
			assertEquals(key.toString(), parts[1]);
		}

	}

}
