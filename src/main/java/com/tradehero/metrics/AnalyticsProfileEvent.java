package com.tradehero.metrics;

import java.util.ArrayList;
import java.util.Collection;

public class AnalyticsProfileEvent {
    private final String name;
    private final Collection<String> attributes;

    public AnalyticsProfileEvent(String name) {
        this(name, new ArrayList<String>());
    }

    public AnalyticsProfileEvent(String name, Collection<String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public final String getName() {
        return name;
    }

    public Collection<String> getAttributes() {
        return attributes;
    }
}