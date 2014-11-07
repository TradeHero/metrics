package com.tradehero.metrics;

import android.content.Context;
import com.tendcloud.tenddata.TCAgent;

import java.util.Set;

/**
 * Created by thonguyen on 7/11/14.
 */

public class TalkingDataAdapter implements AnalyticsAdapter {
    private final Context context;

    public TalkingDataAdapter(Context context, String key, String tag) {
        this.context = context;
        TCAgent.init(context, key, tag);
    }

    @Override
    public void open(Set<String> customDimensions) {
        // TODO
    }

    @Override
    public void close(Set<String> customDimensions) {
        // TODO
    }

    @Override
    public void addEvent(AnalyticsEvent analyticsEvent) {
        /** TODO Second string is a tag **/
        TCAgent.onEvent(context, analyticsEvent.getName(), "", analyticsEvent.getAttributes());
    }

    @Override
    public void tagScreen(String screenName) {
        // TODO
    }
}
