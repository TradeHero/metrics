package com.tradehero.metrics;

public interface ILocalyticsAdapter extends AnalyticsAdapter {
  void setProfileAttribute(AnalyticsProfileEvent analyticsProfileEvent);
}
