package com.about80minutes.palantir.oe.oestato.horizon;

import static org.junit.Assert.*;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

/**
 * Tests the functionality of the ObjectSummary class
 */
public class ObjectSummarytest {

	/**
	 * Defined test case, instantiates a class and checks the default value
	 * assignments then changes values and checks for new assignments.
	 */
	@Test
	public void test() {
		final String result01 = "{linkCount:0, objectName:, objectURI:}"; 
		final String result02 = "{linkCount:10, objectName:objName, objectURI:com.palantir.property.Name}";
		ObjectSummary summary = new ObjectSummary();
		
		assertEquals(result01, summary.toString());
		
		summary.linkCount = new MutableLong(10L);
		summary.objectName = "objName";
		summary.objectURI = "com.palantir.property.Name";
		
		assertEquals(result02, summary.toString());
	}
}
