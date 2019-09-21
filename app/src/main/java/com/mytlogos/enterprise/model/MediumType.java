package com.mytlogos.enterprise.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MediumType {
    public static final int ALL = MediumType.ANIME | MediumType.MANGA | MediumType.NOVEL | MediumType.SERIES;
    public static final int NOVEL = 0x1;
    public static final int MANGA = 0x2;
    public static final int ANIME = 0x4;
    public static final int SERIES = 0x8;
    public static final int TEXT = 0x1;
    public static final int AUDIO = 0x2;
    public static final int VIDEO = 0x4;
    public static final int IMAGE = 0x8;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {TEXT, IMAGE, AUDIO, VIDEO})
    public @interface Medium {

    }

    public static int addMediumType(int mediumType, int toAdd) {
        return mediumType | toAdd;
    }

    public static int removeMediumType(int mediumType, int toRemove) {
        return mediumType & ~toRemove;
    }

    public static int toggleMediumType(int mediumType, int toToggle) {
        return mediumType ^ toToggle;
    }

    public static boolean is(int type, int toCheck) {
        return (type & toCheck) == toCheck;
    }
}
