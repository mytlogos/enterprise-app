package com.mytlogos.enterprise.background

import java.util.*

/**
 * A container for synchronized Sets.
 */
class LoadData {
    val media: MutableSet<Int> = Collections.synchronizedSet(HashSet())
    val part: MutableSet<Int> = Collections.synchronizedSet(HashSet())
    val episodes: MutableSet<Int> = Collections.synchronizedSet(HashSet())
    val news: MutableSet<Int> = Collections.synchronizedSet(HashSet())
    val externalUser: MutableSet<String> = Collections.synchronizedSet(HashSet())
    val externalMediaList: MutableSet<Int> = Collections.synchronizedSet(HashSet())
    val mediaList: MutableSet<Int> = Collections.synchronizedSet(HashSet())
}