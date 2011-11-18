package net.nelz.simplesm.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import net.nelz.simplesm.test.entity.AppUser;
import net.nelz.simplesm.test.svc.AppUserService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Copyright (c) 2010, 2011 Jakub Białek
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * @author Jakub Białek
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath*:META-INF/test-context.xml", "classpath*:simplesm-context.xml" })
public class ComplexTest {
	
	@Autowired
	private AppUserService appUserService;

    @Test
    public void test() {
    	for (int i = 1; i <= 20; i++) {
    		for (int j = 1; j <= i; j++) {
    			appUserService.enableAppForUser(i, j, false);
    		}
    	}
    	
    	List<Integer> userIds = appUserService.getUserIdsList(1, Arrays.asList(1,2,3,4,5,88,66,55,44,33,11,12,13,14));
    	assertEquals(userIds.size(), 9);
    	
    	userIds = appUserService.getUserIdsList(6, Arrays.asList(3,4,5,1,2,88,66,55,44,33,13,14,15,11,12));
    	assertEquals(userIds.size(), 5);
    
    	List<AppUser> appUsers = appUserService.getList(20, true);
    	assertEquals(20, appUsers.size());
    	
    	appUsers = appUserService.getList(20, false);
    	assertEquals(0, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(20, Arrays.asList(1,5,7,8,100,2,3,4,6,11,55,77,88));
    	assertEquals(9, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(20, Arrays.asList(17,1,5,16,12,13,7,8,100,2,3,4,15,6,11,55,18,77,88));
    	assertEquals(15, appUsers.size());
    	
    	for (int i = 1; i <= 20; i++) {
    		assertEquals(i, appUserService.get(20, i).getApplicationId());;
    	}
    	
    	appUsers = appUserService.getInstalledList(15, Arrays.asList(1,2,8,9,100,13,14,55,77,88));
    	assertEquals(6, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(15, Arrays.asList(1,2,8,9,13,14));
    	assertEquals(6, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(15, Arrays.asList(5,7,1,2,8,15,3,4,9,13,14));
    	assertEquals(11, appUsers.size());
    	
    	for (int i = 1; i <= 15; i++) {
    		assertEquals(i, appUserService.get(15, i).getApplicationId());;
    	}
    	
    	appUsers = appUserService.getInstalledList(10, Arrays.asList(4,5,1));
    	assertEquals(3, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(10, Arrays.asList(2,9,4,5,1,7));
    	assertEquals(6, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(10, Arrays.asList(1,2,3,4,5,6,7,8,9,10));
    	assertEquals(10, appUsers.size());
    	
    	for (int i = 1; i <= 10; i++) {
    		assertEquals(i, appUserService.get(10, i).getApplicationId());;
    	}
    	
    	
    	for (int i = 1; i <= 20; i++) {
    		for (int j = 1; j < i; j++) {
    			appUserService.disableAppForUser(i, j);
    		}
    	}
    	
    	appUsers = appUserService.getInstalledList(10, Arrays.asList(4,5,1));
    	assertEquals(0, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(10, Arrays.asList(2,9,4,5,1,7));
    	assertEquals(0, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(10, Arrays.asList(2,9,33,44,55,4,5,3,1,77,22,7));
    	assertEquals(0, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(10, Arrays.asList(1,2,3,4,5,6,7,8,9,10));
    	assertEquals(1, appUsers.size());
    	
    	for (int i = 1; i <= 10; i++) {
    		assertEquals(i, appUserService.get(10, i).getApplicationId());;
    	}
      	
      	userIds = appUserService.getUserIdsList(1, Arrays.asList(1,2,3,4,5,88,66,55,44,33,11,12,13,14));
    	assertEquals(userIds.size(), 1);
    	
    	userIds = appUserService.getUserIdsList(2, Arrays.asList(1,2,3,4,5,88,66,55,44,33,11,12,13,14));
    	assertEquals(1, userIds.size());
    	
    	userIds = appUserService.getUserIdsList(6, Arrays.asList(3,4,5,1,2,88,66,55,44,33,13,14,15,11,12));
    	assertEquals(0, userIds.size());
    
    	appUsers = appUserService.getList(20, true);
    	assertEquals(1, appUsers.size());
    	
       	appUsers = appUserService.getList(20, false);
    	assertEquals(19, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(20, Arrays.asList(1,5,7,8,100,2,3,4,6,11,55,77,88));
    	assertEquals(0, appUsers.size());
    	
    	
    	for (int i = 1; i <= 20; i++) {
    		for (int j = 1; j < i; j++) {
    			appUserService.enableAppForUser(i, j, false);
    		}
    	}
    	
    	userIds = appUserService.getUserIdsList(1, Arrays.asList(1,2,3,4,5,88,66,55,44,33,11,12,13,14));
    	assertEquals(userIds.size(), 9);
    	
    	userIds = appUserService.getUserIdsList(6, Arrays.asList(3,4,5,1,2,88,66,55,44,33,13,14,15,11,12));
    	assertEquals(userIds.size(), 5);
    
    	appUsers = appUserService.getList(20, true);
    	assertEquals(20, appUsers.size());
    	
    	appUsers = appUserService.getList(20, false);
    	assertEquals(0, appUsers.size());
    	
    	appUsers = appUserService.getInstalledList(20, Arrays.asList(1,5,7,8,100,2,3,4,6,11,55,77,88));
    	assertEquals(9, appUsers.size());    		
	}
    
}
