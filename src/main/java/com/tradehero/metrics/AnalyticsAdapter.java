package com.tradehero.metrics;

import java.util.Set;

/**
 * Created by thonguyen on 7/11/14.
 */
public interface AnalyticsAdapter {
    void open(Set<String> customDimensions);

    void addEvent(AnalyticsEvent analyticsEvent);

    void tagScreen(String screenName);

    /**
     * unlike Localytics, this close method will not only close active session but also upload data to tracker server *
     */
    void close(Set<String> customDimensions);
}
