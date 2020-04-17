package com.mytlogos.enterprise.tools;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import com.mytlogos.enterprise.background.api.NotConnectedException;
import com.mytlogos.enterprise.background.api.ServerException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;

public class Utils {
    public static String getDomain(String url) {
        String host = URI.create(url).getHost();
        if (host == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("(www\\.)?(.+?)/?").matcher(host);

        String domain;
        if (matcher.matches()) {
            domain = matcher.group(2);
            int index = domain.indexOf("/");

            if (index >= 0) {
                domain = domain.substring(0, index);
            }
        } else {
            domain = host;
        }
        return domain;
    }

    public static String externalUserTypeToName(int type) {
        switch (type) {
            case 0:
                return "NovelUpdates";
            default:
                throw new IllegalArgumentException("unknown type");
        }
    }

    public static <E> LiveData<PagedList<E>> transform(LiveData<List<E>> listLiveData) {
        return Transformations.switchMap(
                listLiveData,
                input -> new LivePagedListBuilder<>(
                        new DataSource.Factory<Integer, E>() {
                            @NonNull
                            @Override
                            public DataSource<Integer, E> create() {
                                return new StaticDataSource<>(input);
                            }
                        }, 1000
                ).build()
        );
    }

    public static <E> void doPartitionedEx(Collection<E> collection, FunctionEx<List<E>, Boolean> consumer) throws Exception {
        List<E> list = new ArrayList<>(collection);
        int steps = 100;
        int minItem = 0;
        int maxItem = minItem + steps;

        do {
            if (maxItem > list.size()) {
                maxItem = list.size();
            }

            List<E> subList = list.subList(minItem, maxItem);

            Boolean result = consumer.apply(subList);

            if (result != null && result) {
                continue;
            } else if (result == null) {
                break;
            }

            minItem = minItem + steps;
            maxItem = minItem + steps;

            if (maxItem > list.size()) {
                maxItem = list.size();
            }
        } while (minItem < list.size() && maxItem <= list.size());
    }

    public static <E> void doPartitioned(Collection<E> collection, Function<List<E>, Boolean> consumer) {
        List<E> list = new ArrayList<>(collection);
        int steps = 100;
        int minItem = 0;
        int maxItem = minItem + steps;

        do {
            if (maxItem > list.size()) {
                maxItem = list.size();
            }

            List<E> subList = list.subList(minItem, maxItem);

            Boolean result = consumer.apply(subList);

            if (result != null && result) {
                continue;
            } else if (result == null) {
                break;
            }

            minItem = minItem + steps;
            maxItem = minItem + steps;

            if (maxItem > list.size()) {
                maxItem = list.size();
            }
        } while (minItem < list.size() && maxItem <= list.size());
    }

    public static <T> void doPartitionedRethrow(Collection<T> collection, FunctionEx<List<T>, Boolean> functionEx) throws IOException {
        try {
            doPartitionedEx(collection, functionEx);
        } catch (NotConnectedException e) {
            throw new NotConnectedException(e);
        } catch (ServerException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T checkAndGetBody(Response<T> response) throws IOException {
        T body = response.body();

        if (body == null) {
            String errorMsg = response.errorBody() != null ? response.errorBody().string() : null;
            throw new ServerException(response.code(), errorMsg);
        }
        return body;
    }

    private static class StaticDataSource<E> extends PageKeyedDataSource<Integer, E> {
        private final List<E> data;

        private StaticDataSource(List<E> data) {
            this.data = data;
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, E> callback) {
            callback.onResult(data, null, null);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, E> callback) {

        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, E> callback) {

        }
    }
}
