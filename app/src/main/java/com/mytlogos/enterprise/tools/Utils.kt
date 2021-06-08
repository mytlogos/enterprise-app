package com.mytlogos.enterprise.tools

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import androidx.paging.*
import com.mytlogos.enterprise.background.api.NotConnectedException
import com.mytlogos.enterprise.background.api.ServerException
import kotlinx.coroutines.Deferred
import retrofit2.Response
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import java.util.stream.Collectors

fun getDomain(url: String?): String? {
    val host = URI.create(url).host ?: return null
    val matcher = Pattern.compile("(www\\.)?(.+?)/?").matcher(host)
    var domain: String
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


@Deprecated(
    "PagedList is deprecated",
    replaceWith = ReplaceWith("transformPaging")
)
fun <E : Any> transform(listLiveData: LiveData<MutableList<E>>): LiveData<PagedList<E>> {
    return Transformations.switchMap(
        listLiveData
    ) { input: List<E> ->
        LivePagedListBuilder(
            object : DataSource.Factory<Int, E>() {
                override fun create(): DataSource<Int, E> {
                    return StaticDataSource(input)
                }
            }, 1000
        ).build()
    }
}


fun <E : Any> transformPaging(listLiveData: LiveData<MutableList<E>>): LiveData<PagingData<E>> {
    return listLiveData.map { PagingData.from(it) }
}


@Throws(Exception::class)
suspend fun <E : Any> doPartitionedExSuspend(
    collection: Collection<E>,
    consumer: (List<E>) -> Deferred<Boolean?>,
) {
    val list: List<E> = ArrayList(collection)
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
fun <E : Any> doPartitionedEx(collection: Collection<E>?, consumer: (List<E>) -> Boolean?) {
    val list: List<E> = ArrayList(collection)
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


fun <E : Any> doPartitioned(
    collection: Collection<E>?,
    consumer: (List<E>) -> Boolean?,
) {
    val list: List<E> = ArrayList(collection)
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
fun <T : Any> doPartitionedRethrow(
    collection: Collection<T>?,
    functionEx: (List<T>) -> Boolean?,
) {
    try {
        doPartitionedEx(collection, functionEx)
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
suspend fun <T : Any> doPartitionedRethrowSuspend(
    collection: Collection<T>,
    functionEx: (List<T>) -> Deferred<Boolean?>,
) {
    try {
        doPartitionedExSuspend(collection, functionEx)
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


fun <T> finishAll(futuresList: Collection<CompletableFuture<T>>): CompletableFuture<List<T>> {
    val allFuturesResult =
        CompletableFuture.allOf(*futuresList.toTypedArray<CompletableFuture<*>>())
    return allFuturesResult.thenApply { futuresList.map { obj: CompletableFuture<T> -> obj.join() }
    }
}


@Throws(IOException::class)
fun <T> checkAndGetBody(response: Response<T>): T {
    val body = response.body()
    if (body == null) {
        val errorMsg = if (response.errorBody() != null) response.errorBody()!!
            .string() else null
        throw ServerException(response.code(), errorMsg)
    }
    return body
}


private class StaticDataSource<E : Any>(
    private val data: List<E>,
) : PageKeyedDataSource<Int, E>() {
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, E>,
    ) {
        callback.onResult(data, null, null)
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, E>) {}
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, E>) {}
}