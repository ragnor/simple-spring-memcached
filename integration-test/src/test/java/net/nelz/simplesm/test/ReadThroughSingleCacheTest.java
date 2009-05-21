package net.nelz.simplesm.test;

import net.nelz.simplesm.test.svc.*;
import org.springframework.context.*;
import org.springframework.context.support.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

import java.util.*;

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
public class ReadThroughSingleCacheTest {

	private ApplicationContext context;

	@BeforeClass
	public void beforeClass() {
		context = new ClassPathXmlApplicationContext("/test-context.xml");
	}

	@Test
	public void test() {
		final TestSvc test = (TestSvc) context.getBean("testSvc");
		final String currentKey = "TestKey-" + new Date().getTime();
		System.out.println("Key -> " + currentKey);
		final String s1 = test.getDateString(currentKey);
		for (int ix = 0; ix < 10; ix++) {
			assertEquals(String.format("Cache didn't seem to bring back [%s] as expectd.", s1), s1, test.getDateString(currentKey));
		}
	}
}
