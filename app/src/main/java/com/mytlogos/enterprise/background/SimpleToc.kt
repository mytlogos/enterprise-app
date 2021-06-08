package com.mytlogos.enterprise.background

import com.mytlogos.enterprise.model.Toc

/**
 *
 */
data class SimpleToc(
    override val mediumId: Int,
    override val link: String,
) : Toc