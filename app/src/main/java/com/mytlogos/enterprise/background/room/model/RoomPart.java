package com.mytlogos.enterprise.background.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.mytlogos.enterprise.model.Part;

import java.util.ArrayList;
import java.util.List;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = RoomMedium.class,
                        onDelete = ForeignKey.SET_NULL,
                        parentColumns = "mediumId",
                        childColumns = "mediumId"
                )
        },
        indices = {
                @Index(value = "mediumId"),
                @Index(value = "partId"),
        }
)
public class RoomPart implements Part {
    @PrimaryKey
    private int partId;
    private int mediumId;
    private String title;
    private int totalIndex;
    private int partialIndex;

    @Ignore
    private List<Integer> episodes;

    public RoomPart(int partId, int mediumId, String title, int totalIndex, int partialIndex) {
        this.partId = partId;
        this.mediumId = mediumId;
        this.title = title;
        this.totalIndex = totalIndex;
        this.partialIndex = partialIndex;
        this.episodes = new ArrayList<>();
    }


    @Override
    public int getPartId() {
        return partId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getTotalIndex() {
        return totalIndex;
    }

    @Override
    public int getPartialIndex() {
        return partialIndex;
    }

    @Override
    public List<Integer> getEpisodes() {
        return episodes;
    }

    public int getMediumId() {
        return mediumId;
    }
}
