package com.mytlogos.enterprise.tools

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.*
import com.mytlogos.enterprise.background.api.NotConnectedException
import com.mytlogos.enterprise.background.api.ServerException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

fun getDomain(url: String?): String? {
    val host = URI.create(url).host ?: return null
    val matcher = Pattern.compile("(www\\.)?(.+?)/?").matcher(host)
    var domain: String?
    if (matcher.matches()) {
        domain = matcher.group(2)
        val index = domain.indexOf("/")
        if (index >= 0) {
            domain = domain.substring(0, index)
        }
    } else {
        domain = host
    }
    return domain
}

fun externalUserTypeToName(type: Int): String {
    return when (type) {
        0 -> "NovelUpdates"
        else -> throw IllegalArgumentException("unknown type")
    }
}

fun <Key : Any, Value : Any> transformFlow(
    pagingSourceFactory: () -> PagingSource<Key, Value>
): Flow<PagingData<Value>> {
    return Pager(
        PagingConfig(50),
        pagingSourceFactory = pagingSourceFactory
    ).flow
}

fun <Key : Any, Value : Any> DataSource.Factory<Key, Value>.transformFlow(): Flow<PagingData<Value>> {
    return Pager(
        PagingConfig(50),
        pagingSourceFactory = this.asPagingSourceFactory()
    ).flow
}

fun <E : Any> LiveData<MutableList<E>>.transformPaging(): LiveData<PagingData<E>> {
    return this.map { PagingData.from(it) }
}

@Throws(Exception::class)
suspend fun <E : Any> Collection<E>.doPartitionedExSuspend(
    consumer: (List<E>) -> Deferred<Boolean?>,
) {
    val list: List<E> = ArrayList(this)
    val steps = 100
    var minItem = 0
    var maxItem = minItem + steps
    do {
        if (maxItem > list.size) {
            maxItem = list.size
        }
        val subList = list.subList(minItem, maxItem)
        val retry = consumer(subList).await()

        if (retry == true) {
            continue
        } else if (retry == null) {
            break
        }
        minItem += steps
        maxItem = minItem + steps
        if (maxItem > list.size) {
            maxItem = list.size
        }
    } while (minItem < list.size && maxItem <= list.size)
}


@Throws(Exception::class)
fun <E : Any> Collection<E>.doPartitionedEx(consumer: (List<E>) -> Boolean?) {
    val list: List<E> = ArrayList(this)
    val steps = 100
    var minItem = 0
    var maxItem = minItem + steps
    do {
        if (maxItem > list.size) {
            maxItem = list.size
        }
        val subList = list.subList(minItem, maxItem)
        val result = consumer(subList)

        if (result != null && result) {
            continue
        } else if (result == null) {
            break
        }
        minItem += steps
        maxItem = minItem + steps
        if (maxItem > list.size) {
            maxItem = list.size
        }
    } while (minItem < list.size && maxItem <= list.size)
}

suspend fun <T : Any> Collection<T>.doPartitionedSuspend(
    function: (List<T>) -> Deferred<Boolean?>,
) {
    val list: List<T> = ArrayList(this)
    val steps = 100
    var minItem = 0
    var maxItem = minItem + steps
    do {
        if (maxItem > list.size) {
            maxItem = list.size
        }
        val subList = list.subList(minItem, maxItem)
        val result = function(subList).await()

        if (result != null && result) {
            continue
        } else if (result == null) {
            break
        }
        minItem += steps
        maxItem = minItem + steps
        if (maxItem > list.size) {
            maxItem = list.size
        }
    } while (minItem < list.size && maxItem <= list.size)
}

fun <E : Any> Collection<E>.doPartitioned(
    consumer: (List<E>) -> Boolean?,
) {
    val list: List<E> = ArrayList(this)
    val steps = 100
    var minItem = 0
    var maxItem = minItem + steps
    do {
        if (maxItem > list.size) {
            maxItem = list.size
        }
        val subList = list.subList(minItem, maxItem)
        val result = consumer(subList)

        if (result != null && result) {
            continue
        } else if (result == null) {
            break
        }
        minItem += steps
        maxItem = minItem + steps
        if (maxItem > list.size) {
            maxItem = list.size
        }
    } while (minItem < list.size && maxItem <= list.size)
}


@Throws(IOException::class)
fun <T : Any> Collection<T>.doPartitionedRethrow(
    functionEx: (List<T>) -> Boolean?,
) {
    try {
        this.doPartitionedEx(functionEx)
    } catch (e: NotConnectedException) {
        throw NotConnectedException(e)
    } catch (e: ServerException) {
        throw ServerException(e)
    } catch (e: IOException) {
        throw IOException(e)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}


@Throws(IOException::class)
suspend fun <T : Any> Collection<T>.doPartitionedRethrowSuspend(
    functionEx: (List<T>) -> Deferred<Boolean?>,
) {
    try {
        doPartitionedExSuspend(functionEx)
    } catch (e: NotConnectedException) {
        throw NotConnectedException(e)
    } catch (e: ServerException) {
        throw ServerException(e)
    } catch (e: IOException) {
        throw IOException(e)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}


fun <T> Collection<CompletableFuture<T>>.finishAll(): CompletableFuture<List<T>> {
    val allFuturesResult =
        CompletableFuture.allOf(*this.toTypedArray<CompletableFuture<*>>())
    return allFuturesResult.thenApply {
        this.map { obj: CompletableFuture<T> -> obj.join() }
    }
}


@Throws(IOException::class)
fun <T> Response<T>.checkAndGetBody(): T {
    val body = this.body()
    if (body == null) {
        val errorMsg = errorBody()?.string()
        throw ServerException(this.code(), errorMsg)
    }
    return body
}
