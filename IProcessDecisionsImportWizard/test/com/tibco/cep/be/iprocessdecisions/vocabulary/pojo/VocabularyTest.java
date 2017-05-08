package com.tibco.cep.be.iprocessdecisions.vocabulary.pojo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class VocabularyTest {

	@Test
	public void testCompare() {
		Vocabulary a = new Vocabulary("A");
		Vocabulary b = new Vocabulary("B");
		
		assertTrue("A < B", a.compareTo(b) < 0);
		assertTrue("A < B", b.compareTo(a) > 0);
		
		a = new Vocabulary("A");
		b = new Vocabulary("A");
		
		assertTrue("A == A", a.compareTo(b) == 0);
		
		a = new Vocabulary("A");
		b = new Vocabulary("a");
		
		assertTrue("A < a", a.compareTo(b) < 0);
		assertTrue("A < a", b.compareTo(a) > 0);
		
	}
	
	@Test
	public void testEquals(){
		Vocabulary a = new Vocabulary("A");
		Vocabulary b = new Vocabulary("B");
		
		assertFalse("A != B", a.equals(b));
		assertFalse("A != B", b.equals(a));
		
		a = new Vocabulary("A");
		b = new Vocabulary("A");
		
		assertTrue("A == A", a.equals(b));
		
		a = new Vocabulary("A");
		b = new Vocabulary("a");
		
		assertFalse("A != a", a.equals(b));
		assertFalse("A != a", b.equals(a));
	}
	
	@Test
	public void testArrayContainment(){
		List<Vocabulary> list = new ArrayList<>();
		Vocabulary a = new Vocabulary("A");
		
		assertTrue("List has changed", list.add(a));
		assertTrue("List contains A", list.contains(a));
		
		Vocabulary b = new Vocabulary("B");
		assertFalse("List doesn't contains B", list.contains(b));
		
		assertTrue("List has changed", list.add(b));
		assertTrue("List contains B", list.contains(b));

		assertTrue("List has changed", list.add(b));
		assertTrue("List contains B", list.contains(b));
}

}
