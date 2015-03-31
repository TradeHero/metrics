package com.tradehero.metrics;

import android.app.Application;
import com.localytics.android.Localytics;
import com.localytics.android.LocalyticsActivityLifecycleCallbacks;

import java.util.Collection;

/**
 * Created by thonguyen on 7/11/14.
 */
class LocalyticsAdapter implements ILocalyticsAdapter {

    LocalyticsAdapter(Application application, String appKey) {
        application.registerActivityLifecycleCallbacks(new LocalyticsActivityLifecycleCallbacks(application, appKey));
        Localytics.setLoggingEnabled(true);
    }

    @Override
    public void addEvent(AnalyticsEvent analyticsEvent) {
        Localytics.tagEvent(analyticsEvent.getName(), analyticsEvent.getAttributes());
    }

    @Override
    public void tagScreen(String screenName) {
        Localytics.tagScreen(screenName);
    }

    public void setProfileAttribute(AnalyticsProfileEvent analyticsProfileEvent) {
        Collection<String> attributes = analyticsProfileEvent.getAttributes();
        Localytics.setProfileAttribute(analyticsProfileEvent.getName(), attributes.toArray(new String[attributes.size()]));
    }
}
