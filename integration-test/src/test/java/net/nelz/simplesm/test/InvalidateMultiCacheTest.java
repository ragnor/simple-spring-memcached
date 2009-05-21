package net.nelz.simplesm.test;

import static org.testng.AssertJUnit.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.apache.commons.lang.math.RandomUtils;
import net.nelz.simplesm.test.svc.TestSvc;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

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
public class InvalidateMultiCacheTest {
    private ApplicationContext context;

    @BeforeClass
    public void beforeClass() {
        context = new ClassPathXmlApplicationContext("/test-context.xml");
    }

    @Test
    public void test() {
        final TestSvc test = (TestSvc) context.getBean("testSvc");

        final List<Long> allIds = new ArrayList<Long>();
        final List<Long> noChangeIds = new ArrayList<Long>();
        final List<Long> firstChangeIds = new ArrayList<Long>();
        final List<Long> secondChangeIds = new ArrayList<Long>();

        final Long base = RandomUtils.nextLong();
        for (int ix = 0; ix < 30; ix++) {
            final Long key = base + (ix * 100);
            allIds.add(key);
            if (ix % 3 == 0) { noChangeIds.add(key); }
            if (ix % 3 == 1) { firstChangeIds.add(key); }
            if (ix % 3 == 2) { secondChangeIds.add(key); }
        }

        final Map<Long, String> firstMap = createMap(allIds, test.getRandomStrings(allIds));
        assertEquals(firstMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(firstMap, createMap(allIds, test.getRandomStrings(allIds)));

        test.updateRandomStrings(firstChangeIds);

        final Map<Long, String> secondMap = createMap(allIds, test.getRandomStrings(allIds));
        for (Map.Entry<Long, String> entry : secondMap.entrySet()) {
            final Long key = entry.getKey();
            if (firstChangeIds.contains(key)) {
                assertNotSame(entry.getValue(), firstMap.get(key));
            } else {
                assertEquals(entry.getValue(), firstMap.get(key));
            }
        }
        assertEquals(secondMap, createMap(allIds, test.getRandomStrings(allIds)));
        assertEquals(secondMap, createMap(allIds, test.getRandomStrings(allIds)));

        test.updateRandomStringsAgain(secondChangeIds);
        final Map<Long, String> thirdMap = createMap(allIds, test.getRandomStrings(allIds));
        for (Map.Entry<Long, String> entry : thirdMap.entrySet()) {
            final Long key = entry.getKey();
            if (noChangeIds.contains(key)) {
                assertEquals(entry.getValue(), firstMap.get(key));
            }
            if (firstChangeIds.contains(key)) {
                assertEquals(entry.getValue(), secondMap.get(key));
            }
            if (secondChangeIds.contains(key)) {
                assertNotSame(entry.getValue(), firstMap.get(key));
            }
        }
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
