package com.mytlogos.enterprise.background.api.model

import android.annotation.SuppressLint
import java.util.*

/**
 * API Model for DataStats.
 */
class ClientStat(private val media: Map<String, Map<String, Partstat>>, private val mediaStats: Map<String, ClientMediaStat>, private val lists: Map<String, List<Int>>, private val extLists: Map<String, List<Int>>, private val extUser: Map<String, List<Int>>) {
    class Partstat(val episodeCount: Long, val episodeSum: Long, val releaseCount: Long)
    class ParsedStat(val media: Map<Int, Map<Int, Partstat>>, val mediaStats: Map<Int, ClientMediaStat>, val lists: Map<Int, List<Int>>, val extLists: Map<Int, List<Int>>, val extUser: Map<String, List<Int>>)

    @SuppressLint("UseSparseArrays")
    fun parse(): ParsedStat {
        val media: MutableMap<Int, Map<Int, Partstat>> = HashMap()
        val mediaStats: MutableMap<Int, ClientMediaStat> = HashMap()
        val lists: MutableMap<Int, List<Int>> = HashMap()
        val extLists: MutableMap<Int, List<Int>> = HashMap()
        val extUser: Map<String, List<Int>> = HashMap()
        for ((key, value) in this.media) {
            val medium: MutableMap<Int, Partstat> = HashMap()
            for ((key1, value1) in value) {
                medium[key1.toInt()] = value1
            }
            media[key.toInt()] = medium
        }
        for ((key, value) in this.lists) {
            lists[key.toInt()] = value
        }
        for ((key, value) in this.extLists) {
            extLists[key.toInt()] = value
        }
        for ((key, value) in this.mediaStats) {
            mediaStats[key.toInt()] = value
        }
        return ParsedStat(media, mediaStats, lists, extLists, this.extUser)
    }
}