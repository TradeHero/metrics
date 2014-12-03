package com.tradehero.metrics;

/**
 * Created by thonguyen on 7/11/14.
 */

import android.content.Context;

import java.util.*;

public final class Analytics {
    private final Set<Action> pendingActions = new LinkedHashSet<Action>();
    private final Set<AnalyticsAdapter> analyticsAdapters;
    private final Set<String> builtinDimensions;

    public static Builder with(Context context) {
        return new Builder(context);
    }

    private Analytics(Set<AnalyticsAdapter> analyticsAdapters, Set<String> builtinDimensions) {
        this.analyticsAdapters = analyticsAdapters;
        this.builtinDimensions = builtinDimensions;
    }

    public final Analytics addEvent(AnalyticsEvent analyticsEvent) {
        pendingActions.add(new AddEventAction(analyticsEvent));
        return this;
    }

    public final Analytics tagScreen(String screenName) {
        pendingActions.add(new TagScreenAction(screenName));
        return this;
    }

    public final void fireEvent(AnalyticsEvent analyticsEvent) {
        // TODO should create a policy for deciding whether to discard or to process pending action
        discardPendingActions();

        openSession();
        doAction(new AddEventAction(analyticsEvent));
        closeSession();
    }

    public final void fireProfileEvent(AnalyticsProfileEvent analyticsProfileEvent) {
        // TODO should create a policy for deciding whether to discard or to process pending action
//        discardPendingActions();

        openSession();
        doAction(new AddProfileAction(analyticsProfileEvent));
        closeSession();
    }

    public final Analytics openSession() {
        return openSession(null);
    }

    public final Analytics openSession(Set<String> customDimensions) {
        doAction(new OpenSessionAction(customDimensions));
        return this;
    }

    public final void closeSession() {
        closeSession(null);
    }

    public final void closeSession(Set<String> customDimensions) {
        if (!pendingActions.isEmpty()) {
            doPendingActions();
        }
        doAction(new CloseSessionAction(customDimensions));
    }

    private void discardPendingActions() {
        pendingActions.clear();
    }

    private void doPendingActions() {
        for (Action action : new ArrayList<Action>(pendingActions)) {
            doAction(action);
        }
    }



    /**
     * TODO Functional programming can cure this pain *
     */
    private void doAction(Action action) {
        for (AnalyticsAdapter handler : analyticsAdapters) {
            action.setHandler(handler);
            action.process();
        }
    }

    public static class Builder {
        private final Context context;
        private Set<String> builtinDimensions = new HashSet<String>();
        private String localyticsAppKey;
        private String talkingDataKey;
        private String talkingDataTag;
        private boolean amp;

        private Builder(Context context) {
            this.context = context;
        }

        public Builder withLocalytics(String appKey) {
            return withLocalytics(appKey, false);
        }

        public Builder withLocalytics(String appKey, boolean isAmp) {
            localyticsAppKey = appKey;
            amp = isAmp;
            return this;
        }

        public Builder withTalkingData(String key, String tag) {
            talkingDataKey = key;
            talkingDataTag = tag;
            return this;
        }

        public Builder addDimension(String dimension) {
            builtinDimensions.add(dimension);
            return this;
        }

        public Builder addDimensions(String[] dimensions) {
            for (String dimension: dimensions) {
                addDimension(dimension);
            }
            return this;
        }

        public Analytics build() {
            return new Analytics(findAdapters(), builtinDimensions);
        }

        private Set<AnalyticsAdapter> findAdapters() {
            Set<AnalyticsAdapter> analyticsAdapters = new HashSet<AnalyticsAdapter>();
            if (Environment.hasLocalyticsOnClasspath() && localyticsAppKey != null) {
                analyticsAdapters.add(new LocalyticsAdapter(context, localyticsAppKey, amp));
            }

            if (Environment.hasTalkingDataOnClasspath() && talkingDataKey != null) {
                analyticsAdapters.add(new TalkingDataAdapter(context, talkingDataKey, talkingDataTag));
            }

            return analyticsAdapters;
        }
    }

    //region Action classes
    private interface Action {
        void process();

        void setHandler(AnalyticsAdapter handler);
    }

    private abstract class HandlerAction
            implements Action {
        protected AnalyticsAdapter handler;

        @Override
        public void setHandler(AnalyticsAdapter handler) {
            this.handler = handler;
        }
    }

    private final class AddEventAction extends HandlerAction {
        private final AnalyticsEvent analyticsEvent;

        public AddEventAction(AnalyticsEvent analyticsEvent) {
            this.analyticsEvent = analyticsEvent;
        }

        @Override
        public void process() {
            handler.addEvent(analyticsEvent);
        }
    }

    private final class AddProfileAction extends HandlerAction {
        private final AnalyticsProfileEvent analyticsProfileEvent;

        public AddProfileAction(AnalyticsProfileEvent analyticsProfileEvent) {
            this.analyticsProfileEvent = analyticsProfileEvent;
        }

        @Override
        public void process() {
            handler.setProfileAttribute(analyticsProfileEvent);
        }
    }

    private abstract class HandlerActionWithDimensions extends HandlerAction {
        protected final Set<String> customDimensions;

        public HandlerActionWithDimensions(Set<String> customDimensions) {
            Set<String> dimensions = new HashSet<String>(builtinDimensions);
            if (customDimensions != null) {
                dimensions.addAll(customDimensions);
            }
            this.customDimensions = dimensions;
        }
    }

    private final class OpenSessionAction extends HandlerActionWithDimensions {
        public OpenSessionAction(Set<String> customDimensions) {
            super(customDimensions);
        }

        @Override
        public void process() {
            handler.open(customDimensions);
        }
    }

    private final class CloseSessionAction extends HandlerActionWithDimensions {
        public CloseSessionAction(Set<String> customDimensions) {
            super(customDimensions);
        }

        @Override
        public void process() {
            handler.close(builtinDimensions);
        }
    }

    private class TagScreenAction extends HandlerAction {
        private final String screenName;

        public TagScreenAction(String screenName) {
            this.screenName = screenName;
        }

        @Override
        public void process() {
            handler.tagScreen(screenName);
        }
    }
    //endregion
}

