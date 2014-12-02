package com.about80minutes.oe.oestato.horizon;

import java.io.Serializable;

import org.apache.commons.lang.mutable.MutableLong;

/**
 * Object summary, this contains summary details for an object that exists in
 * the Palantir
 */
public final class ObjectSummary implements Serializable {
	private static final long serialVersionUID = 1L;
	public MutableLong linkCount = new MutableLong(0);
	public String objectName = "";
	public String objectURI = "";
	
	/**
	 * returns a formatted String representing this class
	 * 
	 * @return a formatted {@link java.lang.String}
	 */
	public final String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("linkCount:");
		builder.append(linkCount);
		builder.append(", objectName:");
		builder.append(objectName);
		builder.append(", objectURI:");
		builder.append(objectURI);
		builder.append("}");
		return builder.toString();
	}
}
