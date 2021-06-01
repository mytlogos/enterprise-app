package com.mytlogos.enterprise.model

class ToDownload(val isProhibited: Boolean, mediumId: Int?, listId: Int?, externalListId: Int?) {
    val mediumId: Int?
    val listId: Int?
    val externalListId: Int?

    init {
        val isMedium = mediumId != null && mediumId > 0
        val isList = listId != null && listId > 0
        val isExternalList = externalListId != null && externalListId > 0
        require(!(isMedium && (isList || isExternalList) || isList && isExternalList)) { "only one id allowed" }
        require(!(!isMedium && !isList && !isExternalList)) { "one id is necessary!" }
        this.mediumId = mediumId
        this.listId = listId
        this.externalListId = externalListId
    }
}