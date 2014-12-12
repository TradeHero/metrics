package com.tradehero.metrics;

import android.content.Context;
import com.localytics.android.LocalyticsAmpSession;
import com.localytics.android.LocalyticsSession;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by thonguyen on 7/11/14.
 */
class LocalyticsAdapter implements ILocalyticsAdapter {
    private final LocalyticsSession localytics;

    LocalyticsAdapter(Context context, String appKey, boolean isAmp) {
        localytics = isAmp ? new LocalyticsAmpSession(context, appKey) :
            new LocalyticsSession(context, appKey);
        LocalyticsSession.setLoggingEnabled(true);
    }

    @Override
    public void open(Set<String> customDimensions) {
        localytics.open(new ArrayList<String>(customDimensions));
    }

    @Override
    public void addEvent(AnalyticsEvent analyticsEvent) {
        localytics.tagEvent(analyticsEvent.getName(), analyticsEvent.getAttributes());
    }

    @Override
    public void tagScreen(String screenName) {
        localytics.tagScreen(screenName);
    }

    @Override
    public void close(Set<String> customDimensions) {
        localytics.close(new ArrayList<String>(customDimensions));
        localytics.upload();
    }

    public void setProfileAttribute(AnalyticsProfileEvent analyticsProfileEvent) {
        localytics.setProfileAttribute(analyticsProfileEvent.getName(), analyticsProfileEvent.getAttributes());
    }
}
