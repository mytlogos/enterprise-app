package com.mytlogos.enterprise.model

data class ToDownload(
    val isProhibited: Boolean,
    val mediumId: Int? = null,
    val listId: Int? = null,
    val externalListId: Int? = null,
) {
    init {
        val isMedium = mediumId != null && mediumId > 0
        val isList = listId != null && listId > 0
        val isExternalList = externalListId != null && externalListId > 0
        require(!(isMedium && (isList || isExternalList) || isList && isExternalList)) { "only one id allowed" }
        require(!(!isMedium && !isList && !isExternalList)) { "one id is necessary!" }
    }
}