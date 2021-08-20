package com.mytlogos.enterprise.tools

enum class Sortings(val sortValue: Int) {
    HOST_AZ(1),
    HOST_ZA(-1),
    MEDIUM(2),
    MEDIUM_REVERSE(-2),
    TITLE_AZ(3),
    TITLE_ZA(-3),
    INDEX_ASC(4),
    INDEX_DESC(-4),
    AUTHOR_DESC(-5),
    AUTHOR_ASC(5),
    DATE_ASC(6),
    DATE_DESC(-6),
    NUMBER_EPISODE_DESC(-7),
    NUMBER_EPISODE_ASC(7),
    NUMBER_EPISODE_READ_ASC(8),
    NUMBER_EPISODE_READ_DESC(-8),
    LAST_UPDATE_DESC(-9),
    LAST_UPDATE_ASC(9),
    NUMBER_EPISODE_UNREAD_ASC(10),
    NUMBER_EPISODE_UNREAD_DESC(-10);
}