package com.mytlogos.enterprise.ui;

import com.mytlogos.enterprise.model.TocEpisode;

@FunctionalInterface
public interface ItemListener {
    boolean handle(TocEpisode item, TocFragment.ActionType type, ActionCount count);
}
