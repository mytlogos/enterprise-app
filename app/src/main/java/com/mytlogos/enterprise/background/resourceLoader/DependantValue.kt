package com.mytlogos.enterprise.background.resourceLoader

import com.mytlogos.enterprise.background.api.model.ClientPart

class DependantValue private constructor(
    val value: Any?,
    val runnable: Runnable?,
    val intId: Int,
    val integerLoader: NetworkLoader<Int>?,
    val stringId: String?,
    val stringLoader: NetworkLoader<String>?
) {

    constructor(value: Any?, intId: Int, integerLoader: NetworkLoader<Int>) : this(
        value,
        null,
        intId,
        integerLoader,
        null,
        null
    ) {
    }

    constructor(value: Any?, stringId: String?, stringLoader: NetworkLoader<String>) : this(
        value,
        null,
        0,
        null,
        stringId,
        stringLoader
    ) {
    }

    constructor(value: Any?) : this(value, null, 0, null, null, null) {}

    @JvmOverloads
    constructor(
        value: Any?,
        runnable: Runnable?,
        intId: Int = 0,
        integerLoader: NetworkLoader<Int>? = null
    ) : this(value, runnable, intId, integerLoader, null, null) {
    }

    constructor(
        value: Any?,
        runnable: Runnable?,
        stringId: String?,
        stringLoader: NetworkLoader<String>?
    ) : this(value, runnable, 0, null, stringId, stringLoader) {
    }

    internal constructor(intId: Int, integerLoader: NetworkLoader<Int>?) : this(
        null,
        null,
        intId,
        integerLoader,
        null,
        null
    ) {
    }

    internal constructor(intId: Int) : this(null, null, intId, null, null, null) {}
    internal constructor(stringId: String?, stringLoader: NetworkLoader<String>?) : this(
        null,
        null,
        0,
        null,
        stringId,
        stringLoader
    ) {
    }

    internal constructor(stringId: String?) : this(null, null, 0, null, stringId, null) {}

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DependantValue
        if (intId != that.intId) return false
        return if (value != that.value) false else stringId == that.stringId
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + intId
        result = 31 * result + (stringId?.hashCode() ?: 0)
        return result
    }
}