package ru.vladimir.personalaccounter.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppUserTest {

	/**
	 * teste equals method
	 */
	@Test
	void testEqualsShouldBeFail() {
		AppUser theAppUser1 = new AppUser();
		theAppUser1.setUsername("pupa");
		theAppUser1.setId(1L);
		AppUser theAppUser2 = new AppUser();
		theAppUser2.setUsername("lupa");
		theAppUser2.setId(1L);
		assertFalse(theAppUser1.equals(theAppUser2));
	}
	@Test
	void testEqualsShouldBeFailId() {
		AppUser theAppUser1 = new AppUser();
		theAppUser1.setUsername("pupa");
		theAppUser1.setId(1L);
		AppUser theAppUser2 = new AppUser();
		theAppUser2.setUsername("pupa");
		theAppUser2.setId(2L);
		assertFalse(theAppUser1.equals(theAppUser2));
	}
	@Test
	void testEqualsShouldBeFailNameIsNull() {
		AppUser theAppUser1 = new AppUser();
		theAppUser1.setUsername("pupa");
		theAppUser1.setId(1L);
		AppUser theAppUser2 = new AppUser();
		theAppUser2.setId(1L);
		assertFalse(theAppUser1.equals(theAppUser2));
	}
	@Test
	void testEqualsShouldBeFailNameIsNullOther() {
		AppUser theAppUser1 = new AppUser();
		theAppUser1.setId(1L);
		AppUser theAppUser2 = new AppUser();
		theAppUser2.setUsername("pupa");
		theAppUser2.setId(1L);
		assertFalse(theAppUser1.equals(theAppUser2));
	}
	@Test
	void testEqualsShouldBeOk() {
		AppUser theAppUser1 = new AppUser();
		theAppUser1.setUsername("pupa");
		theAppUser1.setId(1L);
		AppUser theAppUser2 = new AppUser();
		theAppUser2.setUsername("pupa");
		theAppUser2.setId(1L);
		assertTrue(theAppUser1.equals(theAppUser2));
	}

}
