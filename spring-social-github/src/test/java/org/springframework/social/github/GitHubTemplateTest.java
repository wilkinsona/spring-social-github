/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.github;

import static java.util.Calendar.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestOperations;

/**
 * @author Craig Walls
 */
public class GitHubTemplateTest {
	private GitHubTemplate github;
	private RestOperations restOperations;

	@Before
	public void setup() {
		github = new GitHubTemplate("ACCESS_TOKEN");
		restOperations = mock(RestOperations.class);
		github.restOperations = restOperations;
	}

	@Test
	public void getProfileId() {
		Map<String, Map<String, ?>> restResponse = new HashMap<String, Map<String, ?>>();
		Map<String, Object> userData = new HashMap<String, Object>();
		restResponse.put("user", userData);
		userData.put("id", 123L);
		userData.put("name", "Keith Donald");
		userData.put("login", "kdonald");
		userData.put("location", "Melbourne, Florida");
		userData.put("company", "SpringSource");
		userData.put("blog", "http://blog.springsource.com/author/keithd");
		userData.put("email", "keith.donald at springsource.com");
		userData.put("created_at", "2001/11/30 12:24:19 -0700");
		when(restOperations.getForObject(GitHubTemplate.PROFILE_URL, Map.class, "ACCESS_TOKEN")).thenReturn(
				restResponse);

		GitHubUserProfile profile = github.getUserProfile();
		assertEquals(123L, profile.getId());
		assertEquals("Keith Donald", profile.getName());
		assertEquals("kdonald", profile.getUsername());
		assertEquals("Melbourne, Florida", profile.getLocation());
		assertEquals("SpringSource", profile.getCompany());
		assertEquals("http://blog.springsource.com/author/keithd", profile.getBlog());
		assertEquals("keith.donald at springsource.com", profile.getEmail());

		// This passes every time locally, but fails every time on Bamboo.
		// java.lang.AssertionError: expected:<10> but was:<11> when asserting
		// the month
		// It's as if the get(MONTH) is returning 1-based results, contrary to
		// the JavaDoc and everything I know about Calendar
		// Is it a timezone thing?

		Date createdDate = profile.getCreatedDate();
		Calendar createdDateCal = Calendar.getInstance();
		createdDateCal.set(Calendar.ZONE_OFFSET, -23);
		createdDateCal.setTime(createdDate);
		assertEquals(NOVEMBER, createdDateCal.get(MONTH));
		assertEquals(30, createdDateCal.get(DAY_OF_MONTH));
		assertEquals(2001, createdDateCal.get(YEAR));
	}
}
