package com.about80minutes.oe.oestato.horizon;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.mutable.MutableLong;

import com.palantir.api.horizon.v1.object.HComponentType;
import com.palantir.api.horizon.v1.object.HLink;
import com.palantir.api.horizon.v1.object.HLinkType;
import com.palantir.api.horizon.v1.object.HObject;
import com.palantir.api.horizon.v1.view.View;

/**
 * Implementation of the View interface, this processes the result of the
 * horizon job.
 */
public class LinkCountView implements View<LinkCountViewResult> {

	private static final long serialVersionUID = 1L;
	
	private static final String INTRINSIC_TITLE_URI = "com.palantir.property.IntrinsicTitle";

	private final LinkCountViewResult result = new LinkCountViewResult();

	/**
	 * This constructor for this view
	 */
	public LinkCountView() {
		super();

	}

	/**
	 * Implementation of the getReducedOutput required by
	 * {@link com.palantir.api.horizon.v1.view.View}, Combines results of
	 * calculations on objects from all shards in the data set.
	 * 
	 * @return a {@link com.about80minutes.oe.oestato.horizon.LinkCountViewResult}
	 * representing the result of the view
	 */
	public LinkCountViewResult getReducedOutput() {
		return result;
	}

	/**
	 * Implementation of reduce required by
	 * {@link com.palantir.api.horizon.v1.view.View}, Iterates through a subset
	 * of objects to get the results of a given calculation.
	 * 
	 * @param partialResults a {@link java.lang.Iterable} collection of
	 * {@link com.about80minutes.oe.oestato.horizon.LinkCountViewResult} objects
	 * to process during reduction
	 * 
	 * @return a {@link com.about80minutes.oe.oestato.horizon.LinkCountViewResult}
	 * containing the reduced results passed in through the iterator.
	 */
	public LinkCountViewResult reduce(Iterable<LinkCountViewResult> partialResults) {
		LinkCountViewResult reduced = new LinkCountViewResult();
		for (LinkCountViewResult preReduced : partialResults) {
			for(Entry<Long, ObjectSummary> entry : preReduced.objectSummaries.entrySet()) {
				ObjectSummary tmpSummary = reduced.objectSummaries.get(entry.getKey());
				if(tmpSummary != null) {
					tmpSummary.linkCount.add(entry.getValue().linkCount);
				} else {
					tmpSummary = entry.getValue();
				}
				reduced.objectSummaries.put(entry.getKey(), tmpSummary);
			}
		}
		return reduced;
	}

	/**
	 * Visit Summarises all objects within the Palantir
	 * 
	 * @param hObject a {@link com.palantir.api.horizon.v1.object.HObject} to
	 * process
	 */
	public void visit(HObject hObject) {
		ObjectSummary summary = new ObjectSummary();
		
		Long objectId = Long.valueOf(hObject.getId());
		String objectURI = hObject.getType().getUri();
		for(HComponentType<?> comp : hObject.getComponentTypes()) {
			if(INTRINSIC_TITLE_URI.equals(comp.getUri())) {
				Iterator<?> iter = hObject.get(comp).iterator();
				while(iter.hasNext()) {
					String intrinsicTitle = iter.next().toString();
					if(intrinsicTitle.contains(":")) {
						intrinsicTitle = intrinsicTitle.substring(0, intrinsicTitle.lastIndexOf(":"));
					}
					summary.objectName = WordUtils.capitalizeFully(intrinsicTitle);
					break;
				}
				break;
			}
		}
		
		summary.objectURI = objectURI;
		
		MutableLong linkCount = new MutableLong(0);
		
		for (HLinkType t : hObject.getLinkTypes()) {
			Iterator<HLink> iterator = hObject.get(t).iterator();
			while(iterator.hasNext()) {
				iterator.next();
				linkCount.increment();
			}
		}
		
		summary.linkCount = linkCount;
		this.result.objectSummaries.put(objectId, summary);
	}
}
