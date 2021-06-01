package com.mytlogos.enterprise.ui;

public enum ActionCount {
    CURRENT("Current"),
    CURRENT_AND_PREVIOUSLY("Current and Previously"),
    CURRENT_AND_ONWARDS("Current and Onwards"),
    ALL("All");

    public final String title;

    ActionCount(String title) {
        this.title = title;
    }
}
