package com.mytlogos.enterprise.background.api.model

/**
 * API Model for NewData.
 * TODO: missing properties: tocs: FullMediumToc[]
 */
data class ClientChangedEntities(
    val media: MutableList<ClientSimpleMedium>,
    val releases: MutableList<ClientRelease>,
    val episodes: MutableList<ClientEpisodePure>,
    val parts: MutableList<ClientPartPure>,
    val lists: MutableList<ClientUserList>,
    val extLists: MutableList<ClientExternalMediaListPure>,
    val extUser: MutableList<ClientExternalUserPure>,
    val mediaInWait: MutableList<ClientMediumInWait>,
    val news: MutableList<ClientNews>,
)