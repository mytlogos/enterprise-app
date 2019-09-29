package com.mytlogos.enterprise.background;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public interface EditEvent {
    int getId();

    int getObjectType();

    int getEventType();

    @NonNull
    DateTime getDateTime();

    String getFirstValue();

    String getSecondValue();
}
