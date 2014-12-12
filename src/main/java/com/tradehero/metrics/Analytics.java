package com.tradehero.metrics;

/**
 * Created by thonguyen on 7/11/14.
 */
import android.content.Context;
import com.google.auto.value.AutoValue;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Analytics {
    private static int counter = 0;
    static final int LOCALYTICS = ++counter;
    static final int TALKING_DATA = ++counter;

    private final List<ActionPair<?>> pendingActions = new LinkedList<ActionPair<?>>();
    private final Map<Integer, AnalyticsAdapter> analyticsAdapters;
    private final boolean discardOnFireSingleEvent;
    private final Func<Set<String>> addBuiltInDimensionsAction;

    public static Builder with(Context context) {
        return new Builder(context);
    }

    private Analytics(Map<Integer, AnalyticsAdapter> adapters, final Set<String> builtinDimensions,
        boolean discardOnFireSingleEvent) {
        this.analyticsAdapters = adapters;
        this.discardOnFireSingleEvent = discardOnFireSingleEvent;
        this.addBuiltInDimensionsAction = new Func<Set<String>>() {

            @Override public Set<String> call(Set<String> dimensions) {
                if (dimensions == null) {
                    return builtinDimensions;
                } else {
                    Set<String> result =
                        new LinkedHashSet<String>(builtinDimensions.size() + dimensions.size());
                    result.addAll(builtinDimensions);
                    result.addAll(dimensions);
                    return Collections.unmodifiableSet(result);
                }
            }
        };
    }

    public final ILocalyticsAdapter localytics() {
        AnalyticsAdapter adapter = analyticsAdapters.get(LOCALYTICS);
        if (adapter instanceof ILocalyticsAdapter) {
            return (ILocalyticsAdapter) adapter;
        } else {
            throw new IllegalStateException("Localytics was not setup properly!");
        }
    }

    public final Analytics addEvent(AnalyticsEvent analyticsEvent) {
        pendingActions.add(ActionPair.create(analyticsEvent, AddEventAction));
        return this;
    }

    public final Analytics tagScreen(String screenName) {
        pendingActions.add(ActionPair.create(screenName, TagScreenAction));
        return this;
    }

    public final Analytics openSession() {
        return openSession(null);
    }

    public final Analytics openSession(Set<String> customDimensions) {
        pendingActions.add(ActionPair.create(customDimensions,
            ActionComposite.create(OpenSessionAction, addBuiltInDimensionsAction)));
        return this;
    }

    public final void closeSession() {
        closeSession(null);
    }

    public final void closeSession(Set<String> customDimensions) {
        if (!pendingActions.isEmpty()) {
            doPendingActions();
        }

        ActionComposite<Set<String>> closeActionWithBuiltInDimensions =
            ActionComposite.create(CloseSessionAction, addBuiltInDimensionsAction);
        for (AnalyticsAdapter adapter : analyticsAdapters.values()) {
            closeActionWithBuiltInDimensions.call(adapter, customDimensions);
        }
    }

    public final void fireEvent(AnalyticsEvent analyticsEvent) {
        if (discardOnFireSingleEvent) {
            discardPendingActions();
        }

        openSession();
        for (AnalyticsAdapter adapter: analyticsAdapters.values()) {
            adapter.addEvent(analyticsEvent);
        }
        closeSession();
    }

    private void discardPendingActions() {
        pendingActions.clear();
    }

    @SuppressWarnings("unchecked")
    private void doPendingActions() {
        for (AnalyticsAdapter adapter : analyticsAdapters.values()) {
            for (ActionPair actionPair : pendingActions) {
                actionPair.action().call(adapter, actionPair.data());
            }
        }
    }

    static interface Func1<T1, R> {
        public R call(T1 t1);
    }

    static interface Func<T> extends Func1<T, T> {}

    static interface Action2<T1, T2> {
        public void call(T1 t1, T2 t2);
    }

    static interface Action<T> extends Action2<AnalyticsAdapter, T> {
        @Override void call(AnalyticsAdapter analyticsAdapter, T data);
    }

    private static final Action<String> TagScreenAction = new Action<String>() {
        @Override public void call(AnalyticsAdapter analyticsAdapter, String screenName) {
            analyticsAdapter.tagScreen(screenName);
        }
    };

    private static final Action<AnalyticsEvent> AddEventAction = new Action<AnalyticsEvent>() {
        @Override public void call(AnalyticsAdapter analyticsAdapter, AnalyticsEvent data) {
            analyticsAdapter.addEvent(data);
        }
    };

    private static final Action<Set<String>> OpenSessionAction = new Action<Set<String>>() {
        @Override public void call(AnalyticsAdapter analyticsAdapter, Set<String> data) {
            analyticsAdapter.open(data);
        }
    };

    private static final Action<Set<String>> CloseSessionAction = new Action<Set<String>>() {
        @Override public void call(AnalyticsAdapter analyticsAdapter, Set<String> data) {
            analyticsAdapter.close(data);
        }
    };

    @AutoValue
    abstract static class ActionComposite<T> implements Action<T>, Serializable {
        abstract Action<T> g();
        abstract Func<T> f();

        public static <S> ActionComposite<S> create(Action<S> g, Func<S> f) {
            return new AutoValue_Analytics_ActionComposite<S>(g, f);
        }

        @Override public void call(AnalyticsAdapter analyticsAdapter, T data) {
            g().call(analyticsAdapter, f().call(data));
        }
    }

    @AutoValue
    static abstract class ActionPair<T> {
        abstract T data();
        abstract Action<T> action();

        @SuppressWarnings("unchecked")
        public static <S> ActionPair create(S data, Action<S> action) {
            return new AutoValue_Analytics_ActionPair(data, action);
        }
    }

    public static class Builder {
        private final Context context;
        private Set<String> builtinDimensions = new HashSet<String>();
        private String localyticsAppKey;
        private String talkingDataKey;
        private String talkingDataTag;
        private boolean amp;
        private boolean discardOnFireSingleEvent = true;

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

        public Builder discardOnFireSingleEvent(boolean discardOnFireSingleEvent) {
            this.discardOnFireSingleEvent = discardOnFireSingleEvent;
            return this;
        }

        public Analytics build() {
            return new Analytics(findAdapters(), Collections.unmodifiableSet(builtinDimensions),
                discardOnFireSingleEvent);
        }

        private Map<Integer, AnalyticsAdapter> findAdapters() {
            Map<Integer, AnalyticsAdapter> adapters =
                new HashMap<Integer, AnalyticsAdapter>();
            if (localyticsAppKey != null && Environment.hasLocalyticsOnClasspath()) {
                adapters.put(LOCALYTICS, new LocalyticsAdapter(context, localyticsAppKey, amp));
            }

            if (talkingDataKey != null && Environment.hasTalkingDataOnClasspath()) {
                adapters.put(TALKING_DATA,
                    new TalkingDataAdapter(context, talkingDataKey, talkingDataTag));
            }

            return adapters;
        }
    }
}

