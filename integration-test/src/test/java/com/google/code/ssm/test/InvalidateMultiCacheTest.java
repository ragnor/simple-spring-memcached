package com.google.code.ssm.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.code.ssm.test.svc.TestSvc;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
Copyright (c) 2008, 2009  Nelson Carpentier

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
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:META-INF/test-context.xml", "classpath*:simplesm-context.xml" })
public class InvalidateMultiCacheTest {
	@Autowired
	private TestSvc test;

    @Test
    public void test() throws InterruptedException {
        //final TestSvc test = (TestSvc) context.getBean("testSvc");

        // The full list of ids
        final List<Long> allIds = new ArrayList<Long>();

        // The list of ids whose values we do not expect to change.
        final List<Long> noChangeIds = new ArrayList<Long>();

        // The first set of ids whose values we do expect to change
        final List<Long> firstChangeIds = new ArrayList<Long>();

        // The second set of ids whose values we do expect to change
        final List<Long> secondChangeIds = new ArrayList<Long>();

        // Create the overall list, and distribute the keys to the different types
        final Long base = RandomUtils.nextLong();
        for (int ix = 0; ix < 30; ix++) {
            final Long key = base + (ix * 100);
            allIds.add(key);
            if (ix % 3 == 0) { noChangeIds.add(key); }
            if (ix % 3 == 1) { firstChangeIds.add(key); }
            if (ix % 3 == 2) { secondChangeIds.add(key); }
        }

        // Pull the generated results from the svc/dao. This is expected to be NOT cached.
        final Map<Long, String> originalMap = createMap(allIds, test.getRandomStrings(allIds));

        Thread.sleep(1000);
        // Make sure successive calls to the svc/dao all generate the same results.
        assertEquals(originalMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(originalMap, createMap(allIds, test.getRandomStrings(allIds)));

        // Invalidate a set of IDs, so that when the DAO is called for them again,
        // they will be regenerated.
        test.updateRandomStrings(firstChangeIds);

        final Map<Long, String> secondMap = createMap(allIds, test.getRandomStrings(allIds));

        // Firstly, make sure the new data looks differently than the old data.
        assertFalse(secondMap.equals(originalMap));

        // Loop through all the values. Make sure that any value we didn't expect to change
        // didn't, and that values we expect to be the same are.
        Thread.sleep(1000);
        for (Map.Entry<Long, String> entry : secondMap.entrySet()) {
            final Long key = entry.getKey();
            if (firstChangeIds.contains(key)) {
                assertFalse(entry.getValue().equals(originalMap.get(key)));
            } else {
                assertEquals(entry.getValue(), originalMap.get(key));
            }
        }

        // Make sure successive calls to the svc/dao all generate the same results.
        assertEquals(secondMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(secondMap, createMap(allIds, test.getRandomStrings(allIds)));

        // Invalidate yet another subset of the ids.
        test.updateRandomStringsAgain(secondChangeIds);

        final Map<Long, String> thirdMap = createMap(allIds, test.getRandomStrings(allIds));

        // Make sure this set of results is different from the last two sets
        assertFalse(thirdMap.equals(originalMap));
        assertFalse(thirdMap.equals(secondMap));

        // Again, loop through all the individual id/value pairs, making sure they
        // have updated or not according to our expectations.
        for (Map.Entry<Long, String> entry : thirdMap.entrySet()) {
            final Long key = entry.getKey();
            if (noChangeIds.contains(key)) {
                assertEquals(entry.getValue(), originalMap.get(key));
            }
            if (firstChangeIds.contains(key)) {
                assertEquals(entry.getValue(), secondMap.get(key));
            }
            if (secondChangeIds.contains(key)) {
                assertNotSame(entry.getValue(), originalMap.get(key));
            }
        }

        // Make sure succcessive calls to the svc/dao return the same (cached) results.
        assertEquals(thirdMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(thirdMap, createMap(allIds, test.getRandomStrings(allIds)));                
    }

    Map<Long, String> createMap(final List<Long> keys, final List<String> values) {
        assertEquals(keys.size(), values.size());

        final Map<Long, String> result = new HashMap<Long, String>(keys.size());
        for (int ix = 0; ix < keys.size(); ix++) {
            result.put(keys.get(ix), values.get(ix));
        }
        return result;
    }
}
