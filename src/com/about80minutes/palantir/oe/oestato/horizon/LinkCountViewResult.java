package com.about80minutes.palantir.oe.oestato.horizon;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Represents the result of applying a Horizon view 
 */
public class LinkCountViewResult implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Map<Long, ObjectSummary> objectSummaries = Maps.newHashMap();
}