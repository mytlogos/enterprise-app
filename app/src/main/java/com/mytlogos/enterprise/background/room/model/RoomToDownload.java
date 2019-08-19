package com.mytlogos.enterprise.background.room.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = RoomMedium.class,
                        onDelete = ForeignKey.CASCADE,
                        parentColumns = "mediumId",
                        childColumns = "mediumId"
                ),
                @ForeignKey(
                        entity = RoomMediaList.class,
                        onDelete = ForeignKey.CASCADE,
                        parentColumns = "listId",
                        childColumns = "listId"
                ),
                @ForeignKey(
                        entity = RoomExternalMediaList.class,
                        onDelete = ForeignKey.CASCADE,
                        parentColumns = "externalListId",
                        childColumns = "externalListId"
                )
        },
        indices = {
                @Index(value = {"mediumId"}, unique = true),
                @Index(value = {"listId"}, unique = true),
                @Index(value = {"externalListId"}, unique = true)
        }
)
public class RoomToDownload {
    @PrimaryKey(autoGenerate = true)
    private final int toDownloadId;
    private final boolean prohibited;
    private final Integer mediumId;
    private final Integer listId;
    private final Integer externalListId;

    public RoomToDownload(int toDownloadId, boolean prohibited, Integer mediumId, Integer listId, Integer externalListId) {
        this.toDownloadId = toDownloadId;
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

    public int getToDownloadId() {
        return toDownloadId;
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
