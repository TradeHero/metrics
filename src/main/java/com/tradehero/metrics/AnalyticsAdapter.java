package com.tradehero.metrics;

/**
 * Created by thonguyen on 7/11/14.
 */
public interface AnalyticsAdapter {
    void addEvent(AnalyticsEvent analyticsEvent);

    void tagScreen(String screenName);
}
