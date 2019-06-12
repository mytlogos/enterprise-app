package com.mytlogos.enterprise.model;

public class ToDownload {
    private final boolean prohibited;
    private final Integer mediumId;
    private final Integer listId;
    private final Integer externalListId;

    public ToDownload(boolean prohibited, Integer mediumId, Integer listId, Integer externalListId) {
        this.prohibited = prohibited;

        boolean isMedium = mediumId != null && mediumId > 0;
        boolean isList = listId != null && listId > 0;
        boolean isExternalList = externalListId != null && externalListId > 0;

        if (isMedium && (isList || isExternalList) || isList && isExternalList) {
            throw new IllegalArgumentException("only one id allowed");
        }
        if (!isMedium && !isList && !isExternalList) {
            throw new IllegalArgumentException("one id is necessary!");
        }
        this.mediumId = mediumId;
        this.listId = listId;
        this.externalListId = externalListId;
    }

    public boolean isProhibited() {
        return prohibited;
    }

    public Integer getMediumId() {
        return mediumId;
    }

    public Integer getListId() {
        return listId;
    }

    public Integer getExternalListId() {
        return externalListId;
    }
}
